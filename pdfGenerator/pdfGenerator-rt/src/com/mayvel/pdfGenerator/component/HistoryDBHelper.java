package com.mayvel.pdfGenerator.component;

import com.lowagie.text.*;
import com.lowagie.text.html.WebColors;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mayvel.pdfGenerator.utils.Generic;
import com.mayvel.pdfGenerator.utils.Logger;
import com.tridium.alarm.BConsoleRecipient;
import com.tridium.json.JSONArray;
import com.tridium.json.JSONObject;

import javax.baja.alarm.BAlarmRecord;
import javax.baja.history.*;
import javax.baja.history.db.BHistoryDatabase;
import javax.baja.history.db.HistoryDatabaseConnection;
import javax.baja.naming.BOrd;
import javax.baja.sys.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


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

    public static Map<String, Object> GetAllHistory(String StartTime, String EndTime, String limit, String offset, String historySource, String filterValues, boolean firstAndLastOnly) {
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, List<Map<String, String>>> historyRecordsMap = new LinkedHashMap<>();
        int totalRecords = 0;
        int totalScanned = 0;

        try {
            BHistoryService historyService = (BHistoryService) Sys.getService(BHistoryService.TYPE);
            if (historyService == null) {
                Logger.Error("History service not available");
                responseMap.put("error", "History service not available");
                return responseMap;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy hh:mm:ss a");
            LocalDateTime startDateTime = LocalDateTime.parse(StartTime, formatter);
            LocalDateTime endDateTime = LocalDateTime.parse(EndTime, formatter);

            String[] historyNames = historySource.split(",");
            for (String historyName : historyNames) {
                historyName = historyName.trim();
                String path = getHistoryPathByName(historyName);
                if (path == null) {
                    Logger.Log("History not found for: " + historyName);
                    continue;
                }

                BOrd ord = BOrd.make(path);
                BIHistory history = (BIHistory) ord.resolve().get();
                BHistoryConfig config = history.getConfig();
                BHistoryDatabase db = historyService.getDatabase();
                db.setConfig(config);
                BHistoryId historyId = config.getId();
                db.getSystemTable(String.valueOf(historyId));

                BIHistory[] tablesRecord = db.getHistories();
                for (BIHistory hist : tablesRecord) {
                    if (hist.getId().toString().equals(extractPath(path))) {
                        HistoryDatabaseConnection conn = db.getDbConnection(null);
                        Cursor<BHistoryRecord> cursor = conn.scan(hist);

                        List<Map<String, String>> resultList = new ArrayList<>();
                        List<Map<String, String>> tempFiltered = new ArrayList<>();

                        while (cursor.next()) {
                            try {
                                totalScanned++;
                                BHistoryRecord record = cursor.get();
                                String formattedTime = Generic.formatTimeStamp(record.getTimestamp());
                                LocalDateTime recordTime = LocalDateTime.parse(formattedTime, formatter);

                                if ((recordTime.isAfter(startDateTime) || recordTime.equals(startDateTime)) &&
                                        (recordTime.isBefore(endDateTime) || recordTime.equals(endDateTime))) {

                                    totalRecords++;
                                    Map<String, String> recordMap = convertToSyncallMap(record, filterValues);
                                    if (firstAndLastOnly) {
                                        tempFiltered.add(recordMap);
                                    } else {
                                        resultList.add(recordMap);
                                    }
                                }
                            } catch (Exception e) {
                                Logger.Error("Record parse error: " + e.getMessage());
                            }
                        }

                        if (firstAndLastOnly && !tempFiltered.isEmpty()) {
                            resultList.add(tempFiltered.get(0));
                            if (tempFiltered.size() > 1) {
                                resultList.add(tempFiltered.get(tempFiltered.size() - 1));
                            }
                        }

                        conn.close();
                        historyRecordsMap.put(historyName, resultList);
                    }
                }
            }
        } catch (Exception e) {
            Logger.Error("Error in GetAllHistory: " + e.getMessage());
            responseMap.put("error", e.getMessage());
            return responseMap;
        }

        responseMap.put("filteredRecordCount", totalRecords);
        responseMap.put("totalDatabaseRecordCount", totalScanned);
        responseMap.put("filterValues", filterValues);
        responseMap.put("histories", historyRecordsMap);
        return responseMap;
    }

    public static String extractPath(String input) {
        if (input == null || !input.contains(":")) {
            return input; // or return null;
        }

        // Split the string by colon
        String[] parts = input.split(":", 2);
        return parts[1]; // This will be "/NetCool/TestPoint01"
    }

    public static Map<String, Object> GetAllHistoryFromDB(String StartTime, String EndTime, String limit, String offset, String historySource, String filterValues, boolean firstAndLastOnly) {
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, List<Map<String, String>>> historyData = new LinkedHashMap<>();
        int totalFiltered = 0;
        int totalScanned = 0;

        try {
            BHistoryService historyService = (BHistoryService) Sys.getService(BHistoryService.TYPE);
            if (historyService == null) {
                responseMap.put("error", "History service not available");
                return responseMap;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yy hh:mm:ss a");
            LocalDateTime endDateTime = LocalDateTime.parse(EndTime, formatter);

            String[] sourceNames = historySource.split(",");
            for (String name : sourceNames) {
                name = name.trim();
                String path = getHistoryPathByName(name);
                if (path == null) {
                    Logger.Log("❌ History not found: " + name);
                    continue;
                }

                BOrd ord = BOrd.make("history:" + path);  // Ensure prefix `history:` is added
                BIHistory history = (BIHistory) ord.resolve().get();
                BHistoryConfig config = history.getConfig();

                HistoryDatabaseConnection conn = historyService.getDatabase().getDbConnection(null);
                Cursor<BHistoryRecord> cursor = conn.scan(history);
                List<Map<String, String>> records = new ArrayList<>();
                List<Map<String, String>> tempFiltered = new ArrayList<>();

                while (cursor.next()) {
                    try {
                        totalScanned++;
                        BHistoryRecord record = cursor.get();
                        String formattedTimeStamp = Generic.formatTimeStamp(record.getTimestamp());
                        LocalDateTime recordTime = LocalDateTime.parse(formattedTimeStamp, formatter);

                        if ((recordTime.isBefore(endDateTime) || recordTime.equals(endDateTime))) {
                            totalFiltered++;
                            Map<String, String> recordMap = convertToSyncallMap(record, filterValues);
                            if (firstAndLastOnly) {
                                tempFiltered.add(recordMap);
                            } else {
                                records.add(recordMap);
                            }
                        }
                    } catch (Exception e) {
                        Logger.Error("Error reading record: " + e.getMessage());
                    }
                }

                if (firstAndLastOnly && !tempFiltered.isEmpty()) {
                    records.add(tempFiltered.get(0));
                    if (tempFiltered.size() > 1) {
                        records.add(tempFiltered.get(tempFiltered.size() - 1));
                    }
                }

                conn.close();
                historyData.put(name, records);
            }

        } catch (Exception e) {
            responseMap.put("error", e.getMessage());
            return responseMap;
        }

        responseMap.put("filterValues", filterValues);
        responseMap.put("totalDatabaseRecordCount", totalScanned);
        responseMap.put("filteredRecordCount", totalFiltered);
        responseMap.put("histories", historyData);
        return responseMap;
    }


    public static String[] getAllHistoryCount() {
        try {
            BHistoryService historyService = (BHistoryService) Sys.getService(BHistoryService.TYPE);
            if (historyService == null) {
                Logger.Error("❌ BHistoryService not available.");
                return new String[0];
            }

            BHistoryDatabase db = historyService.getDatabase();
            if (db == null) {
                Logger.Error("❌ BHistoryDatabase not available.");
                return new String[0];
            }

            BIHistory[] histories = db.getHistories();
            if (histories == null || histories.length == 0) {
                Logger.Log("ℹ️ No histories found in the station.");
                return new String[0];
            }

            String[] historyNames = new String[histories.length];

            for (int i = 0; i < histories.length; i++) {
                try {
                    BHistoryConfig config = histories[i].getConfig();
                    historyNames[i] = config.getId().toString(); // Store name in array
                } catch (Exception e) {
                    Logger.Log("⚠️ Error reading a history config: " + e.getMessage());
                    historyNames[i] = "error";
                }
            }

            return historyNames;

        } catch (Exception e) {
            Logger.Error("❌ Error fetching history names: " + e.getMessage());
            return new String[0];
        }
    }

    public static String getHistoryPathByName(String name) {
        String[] allPaths = getAllHistoryCount(); // This returns paths like "/NetCool/..."

        for (String path : allPaths) {
            if (path != null && path.endsWith("/" + name)) {
                return "history:" + path;
            }
        }
        return null; // or throw an exception if preferred
    }

    public static String getAllHistoryNames() {
        List<String> historyNames = new ArrayList<>();

        try {
            BHistoryService historyService = (BHistoryService) Sys.getService(BHistoryService.TYPE);
            if (historyService == null) {
                Logger.Error("History service not available.");
                return "{\"data\":[]}";
            }

            BHistoryDatabase db = historyService.getDatabase();
            BIHistory[] histories = db.getHistories();

            for (BIHistory history : histories) {
                try {
                    String fullPath = history.getConfig().getId().toString();
                    String[] parts = fullPath.split("/");
                    if (parts.length > 0) {
                        String finalName = parts[parts.length - 1];
                        historyNames.add(finalName);
                        Logger.Log("Found history: " + finalName);
                    }
                } catch (Exception e) {
                    Logger.Error("Failed to read history name: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            Logger.Error("Error retrieving history names: " + e.getMessage());
        }

        String jsonArray = historyNames.stream()
                .map(name -> "\"" + name + "\"")
                .collect(Collectors.joining(","));

        return "{\"data\":[" + jsonArray + "]}";
    }

    public static JSONObject generatePdf(String[] data) {
        JSONObject result = new JSONObject();
        try {
            List<DataRow> allRows = new ArrayList<>();
            JSONArray generatedFiles = new JSONArray();

            for (String historyName : data) {
                Map<String, Object> records = GetAllHistoryRecord(historyName);

                Map<String, List<Map<String, String>>> histories =
                        (Map<String, List<Map<String, String>>>) records.get("histories");

                if (histories != null && !histories.isEmpty()) {
                    for (Map.Entry<String, List<Map<String, String>>> entry : histories.entrySet()) {
                        for (Map<String, String> item : entry.getValue()) {
                            String timestamp = item.get("timestamp");
                            String opening = item.get("openingReading");
                            String closing = item.get("closingReading");
                            String consumption = item.get("consumption");
                            allRows.add(new DataRow(timestamp, opening, closing, consumption));
                        }
                    }
                }

                String startDate = "NA";
                String endDate = "NA";
                if (records != null) {
                    startDate = (String) records.getOrDefault("startDate", "NA");
                    endDate = (String) records.getOrDefault("endDate", "NA");
                }

                generateAlarmPdf(historyName, allRows, startDate, "00:00:00", endDate, "12:00:00", "1 day");
                generatedFiles.put(historyName);
            }

            result.put("result", true);
            result.put("message", "PDF(s) Generated Successfully");
            result.put("files", generatedFiles);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("result", false);
            result.put("message", e.getMessage());
        }
        return result;
    }


    public static Map<String, Object> GetAllHistoryRecord(String historySource) {
        Map<String, Object> responseMap = new HashMap<>();
        Map<String, List<Map<String, String>>> historyRecordsMap = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> dailyGroupedRawRecords = new LinkedHashMap<>();
        int totalRecords = 0;
        int totalScanned = 0;

        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd-MMM-yy hh:mm:ss a");
        DateTimeFormatter dateKeyFormatter = DateTimeFormatter.ofPattern("dd-MMM-yy");
        ZoneId systemZone = ZoneId.systemDefault();

        try {
            BHistoryService historyService = (BHistoryService) Sys.getService(BHistoryService.TYPE);
            if (historyService == null) {
                Logger.Error("History service not available");
                responseMap.put("error", "History service not available");
                return responseMap;
            }

            String historyName = historySource.trim();
            String path = getHistoryPathByName(historyName);
            if (path == null) {
                Logger.Log("History not found for: " + historyName);
                responseMap.put("error", "History path not found");
                return responseMap;
            }

            BOrd ord = BOrd.make(path);
            BIHistory history = (BIHistory) ord.resolve().get();
            BHistoryConfig config = history.getConfig();
            BHistoryDatabase db = historyService.getDatabase();
            db.setConfig(config);

            BIHistory[] tablesRecord = db.getHistories();
            for (BIHistory hist : tablesRecord) {
                if (hist.getId().toString().equals(extractPath(path))) {
                    HistoryDatabaseConnection conn = db.getDbConnection(null);
                    Cursor<BHistoryRecord> cursor = conn.scan(hist);

                    Map<String, List<BHistoryRecord>> groupedByDate = new TreeMap<>();

                    while (cursor.next()) {
                        try {
                            totalScanned++;
                            BHistoryRecord record = cursor.get();
                            BAbsTime absTime = record.getTimestamp();
                            ZonedDateTime recordTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(absTime.getMillis()), systemZone);



                            String dateKey = recordTime.format(dateKeyFormatter); // e.g., "03-Jun-25"
                            groupedByDate.putIfAbsent(dateKey, new ArrayList<>());
                            groupedByDate.get(dateKey).add(record);

                            // Optional: Add raw timestamp + value per date (for verification)
                            Map<String, String> rawMap = new LinkedHashMap<>();
                            rawMap.put("timestamp", recordTime.format(displayFormatter));
                            rawMap.put("value", record.get("value").toString());
                            dailyGroupedRawRecords.putIfAbsent(dateKey, new ArrayList<>());
                            dailyGroupedRawRecords.get(dateKey).add(rawMap);

                            totalRecords++;
                        } catch (Exception e) {
                            Logger.Error("Record parse error: " + e.getMessage());
                        }
                    }

                    List<Map<String, String>> finalDailyResults = new ArrayList<>();
                    for (Map.Entry<String, List<Map<String, String>>> entry : dailyGroupedRawRecords.entrySet()) {
                        List<Map<String, String>> records = entry.getValue();
                        if (records.size() < 1) continue;

                        try {
                            double opening = Double.parseDouble(records.get(0).get("value"));
                            double closing = Double.parseDouble(records.get(records.size() - 1).get("value"));
                            double consumption = closing - opening;

                            Map<String, String> dailyMap = new LinkedHashMap<>();
                            dailyMap.put("timestamp", entry.getKey() + " 00:00:00");
                            dailyMap.put("openingReading", opening + "kW-hr");
                            dailyMap.put("closingReading", closing + "kW-hr");
                            dailyMap.put("consumption", consumption + "kW-hr");

                            finalDailyResults.add(dailyMap);
                            Logger.Log("✅ " + entry.getKey() + ": " + opening + " ➜ " + closing + " = " + consumption);
                        } catch (Exception e) {
                            Logger.Error("Value parse error on " + entry.getKey() + ": " + e.getMessage());
                        }
                    }


                    conn.close();
                    // Add startDate and endDate from timestamps
                    if (!finalDailyResults.isEmpty()) {
                        String startDate = finalDailyResults.get(0).get("timestamp").split(" ")[0];
                        String endDate = finalDailyResults.get(finalDailyResults.size() - 1).get("timestamp").split(" ")[0];
                        responseMap.put("startDate", startDate);
                        responseMap.put("endDate", endDate);
                    }
                    historyRecordsMap.put(historyName, finalDailyResults);
                }
            }

        } catch (Exception e) {
            Logger.Error("Error in GetAllHistory: " + e.getMessage());
            responseMap.put("error", e.getMessage());
            return responseMap;
        }

        responseMap.put("filteredRecordCount", totalRecords);
        responseMap.put("totalDatabaseRecordCount", totalScanned);
        responseMap.put("histories", historyRecordsMap);

        return responseMap;
    }
    public static void generateAlarmPdf(String historyName, List<DataRow> rows, String startDate, String startTime, String endDate, String endTime, String interval) {
        try {
            File stationHome = Sys.getStationHome();
            File pdfDir = new File(stationHome, "pdf");

// Check if "pdf" directory exists, if not, create it
            if (!pdfDir.exists()) {
                boolean created = pdfDir.mkdirs();
                if (!created) {
                    throw new IOException("Failed to create pdf directory: " + pdfDir.getAbsolutePath());
                }
            }


// Format: yyyy_MM_dd_hh_mm_a (12-hour format with AM/PM)
            String timestamp = new SimpleDateFormat("yyyy_MM_dd_hh_mm_a").format(new Date());

            String fileName = historyName + "_" + timestamp + ".pdf";
            File pdfFile = new File(pdfDir, fileName);

            // Create document
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            com.mayvel.pdfGenerator.PageNumberEvent event = new com.mayvel.pdfGenerator.PageNumberEvent();
            writer.setPageEvent(event);

            document.open();

            // Title
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Paragraph title = new Paragraph("Data Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Metadata Table (2-column)
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.addCell(getCell("Start Date: " + startDate, PdfPCell.ALIGN_LEFT));
            metaTable.addCell(getCell("End Date: " + endDate, PdfPCell.ALIGN_RIGHT));
            metaTable.addCell(getCell("Start Time: " + startTime, PdfPCell.ALIGN_LEFT));
            metaTable.addCell(getCell("End Time: " + endTime, PdfPCell.ALIGN_RIGHT));
            metaTable.addCell(getCell("Time Interval : " + interval, PdfPCell.ALIGN_RIGHT));
            document.add(metaTable);
            document.add(Chunk.NEWLINE);

            // Data Table Header
            PdfPTable table = new PdfPTable(new float[]{3, 3, 3, 3});
            table.setWidthPercentage(100);
            addTableHeader(table, "Timestamp", "Opening Reading", "Closing Reading", historyName);

            // Data Rows
            for (DataRow row : rows) {
                table.addCell(row.timestamp);
                table.addCell(row.openingReading);
                table.addCell(row.closingReading);
                table.addCell(row.meterConsumption);
            }
            document.add(table);
            document.add(Chunk.NEWLINE);

            // Footer
            PdfPTable footer = new PdfPTable(3);
            footer.setWidthPercentage(100);
            footer.addCell(getCell("Printed By ___________", PdfPCell.ALIGN_LEFT));
            footer.addCell(getCell("", PdfPCell.ALIGN_CENTER));
            footer.addCell(getCell("Reviewed By ___________", PdfPCell.ALIGN_RIGHT));
            document.add(footer);

            document.close();
            System.out.println("✅ PDF created at: " + pdfDir);
        } catch (Exception e) {
            System.err.println("❌ PDF generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean deletePdfByName(String fileName) {
        try {
            File stationHome = Sys.getStationHome();
            File pdfDir = new File(stationHome, "pdf");

            // Ensure the filename ends with .pdf
            if (!fileName.endsWith(".pdf")) {
                fileName += ".pdf";
            }

            File targetFile = new File(pdfDir, fileName);

            if (targetFile.exists()) {
                boolean deleted = targetFile.delete();
                if (deleted) {
                    System.out.println("✅ PDF deleted successfully: " + targetFile.getAbsolutePath());
                } else {
                    System.err.println("❌ Failed to delete PDF: " + targetFile.getAbsolutePath());
                }
                return deleted;
            } else {
                System.err.println("⚠️ File not found: " + targetFile.getAbsolutePath());
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Exception while deleting PDF: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public static JSONObject getAllPdfFileNames(int limit, int offset, String filter) {
        JSONObject result = new JSONObject();
        JSONArray dataArray = new JSONArray();

        try {
            File stationHome = Sys.getStationHome();
            File pdfDir = new File(stationHome, "pdf");

            if (!pdfDir.exists() || !pdfDir.isDirectory()) {
                result.put("total", 0);
                result.put("data", dataArray);
                return result;
            }

            File[] allPdfFiles = pdfDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
            if (allPdfFiles == null) {
                result.put("total", 0);
                result.put("data", dataArray);
                return result;
            }

            // Sort by name (you can change to lastModified if needed)
            Arrays.sort(allPdfFiles, Comparator.comparing(File::getName));

            // Filter
            List<String> filteredList = new ArrayList<>();
            for (File file : allPdfFiles) {
                String name = file.getName();
                if (filter == null || filter.isEmpty() || name.toLowerCase().contains(filter.toLowerCase())) {
                    filteredList.add(name);
                }
            }

            int total = filteredList.size();

            // Pagination (offset is page number, not array index)
            int start = offset * limit;
            int end = Math.min(start + limit, total);

            for (int i = start; i < end; i++) {
                dataArray.put(filteredList.get(i));
            }

            result.put("total", total);
            result.put("data", dataArray);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("total", 0);
            result.put("data", dataArray);
        }

        return result;
    }



    private static void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h));
            cell.setBackgroundColor(WebColors.getRGBColor("#00FFFF")); // CYAN
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private static PdfPCell getCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setPadding(5);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

}
