package com.mayvel.myHistoryDB.component;
import com.mayvel.myHistoryDB.utils.Generic;
import com.mayvel.myHistoryDB.utils.Logger;
import com.tridium.alarm.BConsoleRecipient;

import javax.baja.alarm.BAlarmRecord;
import javax.baja.history.*;
import javax.baja.history.db.BHistoryDatabase;
import javax.baja.history.db.HistoryDatabaseConnection;
import javax.baja.naming.BOrd;
import javax.baja.sys.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;


public class HistoryDBHelper {
    private static Map<String, BiConsumer<BConsoleRecipient, BAlarmRecord>> listeners = new HashMap<>();

    public static String alarmClassData;
    public static String timeStampData;
    public static String normalTimeStampData;
    public static String uUidData;

    public static Map<String, String> convertToSyncallMap(BHistoryRecord record, String filterValues) {
        Map<String, String> mapData = new HashMap<>();

        // Convert comma-separated filterValues to a Set
        Set<String> filterKeys = new HashSet<>();
        if (filterValues != null && !filterValues.trim().isEmpty()) {
            for (String key : filterValues.split(",")) {
                filterKeys.add(key.trim());
            }
        }

        // Parsing alarm data
        BHistorySchema schema = record.getSchema();
        String schemaStr = schema.toString();
        // Split the schema to get field names
        String[] fields = schemaStr.split(";");
        for (String field : fields) {
            String[] keyValue = field.split(",", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String type = keyValue[1].trim();

                // Filter logic
                if (!filterKeys.isEmpty() && !filterKeys.contains(key)) {
                    continue; // skip fields not in the filter
                }

                try {
                    BValue value = record.get(key);
                    if (value != null) {
                        mapData.put(key, value.toString());
                        Logger.Log("10 Field: " + key + " => Type: " + type + " => Value: " + value.toString());
                    } else {
                        mapData.put(key, "null");
                        Logger.Log("10 Field: " + key + " => Type: " + type + " => Value: null");
                    }
                } catch (Exception e) {
                    Logger.Log("Error reading value for field '" + key + "': " + e.getMessage());
                }
            } else {
                Logger.Log("Malformed schema field: " + field);
            }
        }
        return mapData;
    }

    public static Map<String, String> convertToMap(BAlarmRecord alarm) {
        Map<String, String> mapData = new HashMap<>();

        // Parsing alarm data
        String alarmDataString = alarm.getAlarmData().toString();
        Map<String, String> alarmDataMap = parseData(alarmDataString);
        mapData.putAll(alarmDataMap);
        return mapData;
    }

    public static Map<String, String> parseData(String data) {
        String[] pairs = data.split(",");
        Map<String, String> map = new HashMap<>();

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                map.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return map;
    }

    public static Map<String, Object> GetAllHistory(String StartTime, String EndTime, String limit, String offset, String historySource, String filterValues) {
        Map<String, Object> responseMap = new HashMap<>();
        List<Map<String, String>> resultList = new ArrayList<>();
        int lim = Integer.parseInt(limit);
        int off = Integer.parseInt(offset);
        int totalRecords = 0;
        StringBuilder historyGrp = new StringBuilder();
        try {
            BHistoryService historyService = (BHistoryService) Sys.getService(BHistoryService.TYPE);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy hh:mm:ss a");
            LocalDateTime startDateTime = LocalDateTime.parse(StartTime, formatter);
            LocalDateTime endDateTime = LocalDateTime.parse(EndTime, formatter);

            if (historyService == null) {
                Logger.Error("History service not available");
                responseMap.put("error", "History service not available");
                return responseMap;
            }

            BHistoryDatabase db = historyService.getDatabase();
            String path = historySource;
            BOrd ord = BOrd.make(path);

            BIHistory history = (BIHistory) ord.resolve().get();
            Logger.Log("2  History resolved successfully");
            BHistoryConfig config = history.getConfig();
            db.setConfig(config);
            BHistoryId historyId = config.getId();
            db.getSystemTable(String.valueOf(historyId));
            Logger.Log("3  History resolved successfully");
            BIHistory[] tablesRecord = db.getHistories();

            for (int i = 0; i < tablesRecord.length; i++) {
                BIHistory hist = tablesRecord[i];
                historyGrp.append(hist.getId().toString());
                if (i < tablesRecord.length - 1) {
                    historyGrp.append(", ");
                }

                // Check for the /NetCool/TestPoint01 table
                if (hist.getId().toString().equals(extractPath(historySource))) {
                    HistoryDatabaseConnection conn = historyService.getDatabase().getDbConnection(null);
                    Cursor<BHistoryRecord> cursor = conn.scan(hist);
                    while (cursor.next()) {
                        try {
                            BHistoryRecord record = cursor.get();
                            BAbsTime timeStamp = record.getTimestamp();
                            String formattedTimeStampValue = Generic.formatTimeStamp(timeStamp);;
                            LocalDateTime alarmDateTime = LocalDateTime.parse(formattedTimeStampValue, formatter);
                            if ((alarmDateTime.isAfter(startDateTime) || alarmDateTime.equals(startDateTime))
                                    && (alarmDateTime.isBefore(endDateTime) || alarmDateTime.equals(endDateTime))) {
                                totalRecords++;
                                if (totalRecords > off && resultList.size() < lim){
                                    resultList.add(convertToSyncallMap(record,filterValues));
                                }
                            }
                        } catch (Exception e) {
                            Logger.Error(e.getMessage());
                        }
                    }
                    conn.close();
                }
            }

        } catch (Exception e) {
            Logger.Error("Error in GetAllHistory: " + e.getMessage());
            responseMap.put("error", e.getMessage());
            return responseMap;
        }

        responseMap.put("totalHistoryRecords", totalRecords);
        responseMap.put("historyRecords", resultList);
        responseMap.put("historySourcePath", historySource);
        responseMap.put("filterValues", filterValues);
        return responseMap;
    }

    private static String extractPath(String input) {
        if (input == null || !input.contains(":")) {
            return input; // or return null;
        }

        // Split the string by colon
        String[] parts = input.split(":", 2);
        return parts[1]; // This will be "/NetCool/TestPoint01"
    }

    public static Map<String, Object> GetAllHistoryFromDB(String StartTime, String EndTime, String limit, String offset, String historySource, String filterValues) {
        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        List<Map<String, String>> resultList = new ArrayList<>();
        Map<String, Object> responseMap = new HashMap<>();
        int totalRecords = 0;
        int lim = Integer.parseInt(limit);
        int off = Integer.parseInt(offset);
        StringBuilder historyGrp = new StringBuilder();
        try {
            BHistoryService historyService = (BHistoryService) Sys.getService(BHistoryService.TYPE);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy hh:mm:ss a");
            LocalDateTime endDateTime = LocalDateTime.parse(EndTime, formatter);

            if (historyService == null) {
                Logger.Error("History service not available");
                responseMap.put("error", "History service not available");
                return responseMap;
            }

            BHistoryDatabase db = historyService.getDatabase();
            String path = historySource;
            BOrd ord = BOrd.make(path);

            BIHistory history = (BIHistory) ord.resolve().get();
            Logger.Log("2  History resolved successfully");
            BHistoryConfig config = history.getConfig();
            db.setConfig(config);
            BHistoryId historyId = config.getId();
            db.getSystemTable(String.valueOf(historyId));
            Logger.Log("3  History resolved successfully");
            BIHistory[] tablesRecord = db.getHistories();

            for (int i = 0; i < tablesRecord.length; i++) {
                BIHistory hist = tablesRecord[i];
                historyGrp.append(hist.getId().toString());
                if (i < tablesRecord.length - 1) {
                    historyGrp.append(", ");
                }

                // Check for the /NetCool/TestPoint01 table
                if (hist.getId().toString().equals(extractPath(historySource))) {
                    HistoryDatabaseConnection conn = historyService.getDatabase().getDbConnection(null);
                    Cursor<BHistoryRecord> cursor = conn.scan(hist);
                    while (cursor.next()) {
                        try {
                            BHistoryRecord record = cursor.get();
                            BAbsTime timeStamp = record.getTimestamp();
                            String formattedTimeStampValue = Generic.formatTimeStamp(timeStamp);;
                            LocalDateTime alarmDateTime = LocalDateTime.parse(formattedTimeStampValue, formatter);
                            if ((alarmDateTime.isBefore(endDateTime) || alarmDateTime.equals(endDateTime))) {
                                totalRecords++;
                                if (totalRecords > off && resultList.size() < lim){
                                    resultList.add(convertToSyncallMap(record,filterValues));
                                }
                            }
                        } catch (Exception e) {
                            Logger.Error(e.getMessage());
                        }
                    }
                    conn.close();
                }
            }

        } catch (Exception e) {
            Logger.Error("Error in GetAllHistory: " + e.getMessage());
            responseMap.put("error", e.getMessage());
            return responseMap;
        }

        responseMap.put("totalHistoryRecords", totalRecords);
        responseMap.put("historyRecords", resultList);
        responseMap.put("historySourcePath", historySource);
        responseMap.put("filterValues", filterValues);
        return responseMap;
    }
}
