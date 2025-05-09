package com.mayvel.myHistoryDB.component;
import com.mayvel.myHistoryDB.utils.Generic;
import com.mayvel.myHistoryDB.utils.Logger;
import com.tridium.alarm.BConsoleRecipient;

import javax.baja.alarm.AlarmDbConnection;
import javax.baja.alarm.BAlarmRecord;
import javax.baja.alarm.BAlarmService;
import javax.baja.sys.BAbsTime;
import javax.baja.sys.Cursor;
import javax.baja.sys.Sys;
import javax.baja.util.BUuid;
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

    public static Map<String, String> convertToSyncallMap(BAlarmRecord alarm) {
        Map<String, String> mapData = new HashMap<>();

        // Parsing alarm data
        String alarmDataString = alarm.getAlarmData().toString();
        Map<String, String> alarmDataMap = parseData(alarmDataString);
        mapData.putAll(alarmDataMap);
        mapData.put("timeStampData", timeStampData);
        mapData.put("normalTime", normalTimeStampData);
        mapData.put("alarmClass", alarmClassData);
        mapData.put("uuid", uUidData);
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

    public static Map<String, Object> GetAllHistory(String StartTime, String EndTime, String limit, String offset) {

        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        List<Map<String, String>> resultList = new ArrayList<>();
        int totalAlarms = 0;
        int lim = Integer.parseInt(limit);
        int off = Integer.parseInt(offset);
        try {
            BAlarmService alarmService = (BAlarmService) Sys.getService(BAlarmService.TYPE);
            AlarmDbConnection conn = alarmService.getAlarmDb().getDbConnection(null);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy hh:mm:ss a");
            LocalDateTime startDateTime = LocalDateTime.parse(StartTime, formatter);
            LocalDateTime endDateTime = LocalDateTime.parse(EndTime, formatter);
            Cursor<BAlarmRecord> cursor = conn.scan();
            System.out.println("the start time: "+StartTime);
            Logger.Log("This GetAllHistory is triggered");
            while (cursor.next()) {
                try {
                    BAlarmRecord alarm = cursor.get();
                    BAlarmRecord alarmRecord = conn.getRecord(BUuid.make(alarm.getUuid().toString()));
                    String formattedTimeStamp = Generic.ConvertDateToStringWithPattern(alarm.getTimestamp(), null);
                    timeStampData=formattedTimeStamp;
                    String formattedNormalStamp = Generic.ConvertDateToStringWithPattern(alarm.getNormalTime(), null);
                    String uUidDatas = alarm.getUuid().toString();
                    uUidData=uUidDatas;
                    String normalTimeStampDatas = Generic.isValidDate(formattedNormalStamp) ? "null" : formattedNormalStamp;
                    normalTimeStampData=normalTimeStampDatas;
                    String alarmClass = alarmRecord.getAlarmClass();
                    alarmClassData = alarmClass;
                    BAbsTime timeStamp = alarmRecord.getTimestamp();
                    String formattedTimeStampValue = Generic.formatTimeStamp(timeStamp);;
                    LocalDateTime alarmDateTime = LocalDateTime.parse(formattedTimeStampValue, formatter);
                    if ((alarmDateTime.isAfter(startDateTime) || alarmDateTime.equals(startDateTime))
                            && (alarmDateTime.isBefore(endDateTime) || alarmDateTime.equals(endDateTime))) {
                        totalAlarms++;
                        if (totalAlarms > off && resultList.size() < lim){
                            resultList.add(convertToSyncallMap(alarm));
                        }
                    }
                } catch (Exception e) {
                    Logger.Error(e.getMessage());
                }
            }
            conn.close();
        } catch (Exception e) {
            Logger.Error(e.getMessage());
        }
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("totalAlarms", totalAlarms);
        responseMap.put("alarms", resultList);
        return responseMap;
    }

    public static Map<String, Object> GetAllHistoryFromDB(String StartTime, String EndTime,String limit,String offset) {
        Map<String, Map<String, String>> data = new LinkedHashMap<>();
        List<Map<String, String>> resultList = new ArrayList<>();
        int totalAlarms = 0;
        int lim = Integer.parseInt(limit);
        int off = Integer.parseInt(offset);
        try {
            Logger.Log("This GetAllHistoryFromDB is triggered");
            BAlarmService alarmService = (BAlarmService) Sys.getService(BAlarmService.TYPE);
            AlarmDbConnection conn = alarmService.getAlarmDb().getDbConnection(null);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy hh:mm:ss a");
            LocalDateTime endDateTime = LocalDateTime.parse(EndTime, formatter);
            Cursor<BAlarmRecord> cursor = conn.scan();
            while (cursor.next()) {
                try {
                    BAlarmRecord alarm = cursor.get();
                    BAlarmRecord alarmRecord = conn.getRecord(BUuid.make(alarm.getUuid().toString()));
                    String formattedTimeStamp = Generic.ConvertDateToStringWithPattern(alarm.getTimestamp(), null);
                    timeStampData=formattedTimeStamp;
                    String formattedNormalStamp = Generic.ConvertDateToStringWithPattern(alarm.getNormalTime(), null);
                    String uUidDatas = alarm.getUuid().toString();
                    uUidData=uUidDatas;
                    String normalTimeStampDatas = Generic.isValidDate(formattedNormalStamp) ? "null" : formattedNormalStamp;
                    normalTimeStampData=normalTimeStampDatas;
                    String alarmClass = alarmRecord.getAlarmClass();
                    alarmClassData = alarmClass;
                    BAbsTime timeStamp = alarmRecord.getTimestamp();
                    String formattedTimeStampValue = Generic.formatTimeStamp(timeStamp);;
                    LocalDateTime alarmDateTime = LocalDateTime.parse(formattedTimeStampValue, formatter);
                    if ((alarmDateTime.isBefore(endDateTime) || alarmDateTime.equals(endDateTime))) {
                        totalAlarms++;
                        if (totalAlarms > off && resultList.size() < lim) {
                            resultList.add(convertToSyncallMap(alarm));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();                }
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("totalAlarms", totalAlarms);
        responseMap.put("alarms", resultList);
        return responseMap;
    }
}
