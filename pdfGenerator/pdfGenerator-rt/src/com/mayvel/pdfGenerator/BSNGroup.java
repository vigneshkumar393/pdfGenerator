package com.mayvel.pdfGenerator;

import com.lowagie.text.*;
import com.lowagie.text.Font;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.lowagie.text.html.WebColors;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.mayvel.pdfGenerator.component.DataRow;
import com.mayvel.pdfGenerator.utils.HttpHistoryPost;
import com.mayvel.pdfGenerator.utils.Logger;
import com.tridium.json.JSONArray;
import com.tridium.json.JSONObject;
import javafx.scene.control.Hyperlink;

import javax.baja.history.*;
import javax.baja.history.db.BHistoryDatabase;
import javax.baja.history.db.HistoryDatabaseConnection;
import javax.baja.naming.BOrd;
import javax.baja.nre.annotations.Facet;
import javax.baja.nre.annotations.NiagaraAction;
import javax.baja.nre.annotations.NiagaraProperty;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.*;
import javax.baja.sys.Cursor;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mayvel.pdfGenerator.component.HistoryDBHelper.extractPath;
import static com.mayvel.pdfGenerator.component.HistoryDBHelper.getHistoryPathByName;


@NiagaraType

@NiagaraProperty(name = "historySource",
        type = "String",
        defaultValue = "",
        flags = Flags.SUMMARY
)

@NiagaraProperty(
        name = "resultOut",
        type = "String",
        defaultValue = "",
        flags = Flags.SUMMARY | Flags.READONLY,
        facets = {
                @Facet(name = "BFacets.MULTI_LINE", value = "BBoolean.TRUE"),
        }
)

@NiagaraAction(name = "generatePdf")
public class BSNGroup  extends BComponent {
//region /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
//@formatter:off
/*@ $com.mayvel.pdfGenerator.BSNGroup(1415011862)1.0$ @*/
/* Generated Fri Jun 27 14:46:00 IST 2025 by Slot-o-Matic (c) Tridium, Inc. 2012-2025 */

  //region Property "historySource"

  /**
   * Slot for the {@code historySource} property.
   * @see #getHistorySource
   * @see #setHistorySource
   */
  public static final Property historySource = newProperty(Flags.SUMMARY, "", null);

  /**
   * Get the {@code historySource} property.
   * @see #historySource
   */
  public String getHistorySource() { return getString(historySource); }

  /**
   * Set the {@code historySource} property.
   * @see #historySource
   */
  public void setHistorySource(String v) { setString(historySource, v, null); }

  //endregion Property "historySource"

  //region Property "resultOut"

  /**
   * Slot for the {@code resultOut} property.
   * @see #getResultOut
   * @see #setResultOut
   */
  public static final Property resultOut = newProperty(Flags.SUMMARY | Flags.READONLY, "", BFacets.make(BFacets.MULTI_LINE, BBoolean.TRUE));

  /**
   * Get the {@code resultOut} property.
   * @see #resultOut
   */
  public String getResultOut() { return getString(resultOut); }

  /**
   * Set the {@code resultOut} property.
   * @see #resultOut
   */
  public void setResultOut(String v) { setString(resultOut, v, null); }

  //endregion Property "resultOut"

  //region Action "generatePdf"

  /**
   * Slot for the {@code generatePdf} action.
   * @see #generatePdf()
   */
  public static final Action generatePdf = newAction(0, null);

  /**
   * Invoke the {@code generatePdf} action.
   * @see #generatePdf
   */
  public void generatePdf() { invoke(generatePdf, null, null); }

  //endregion Action "generatePdf"

  //region Type

  @Override
  public Type getType() { return TYPE; }
  public static final Type TYPE = Sys.loadType(BSNGroup.class);

  //endregion Type

//@formatter:on
//endregion /*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/


    public void started() throws Exception {
        super.started();

        try {
            HttpHistoryPost.start();
            checkHistoryUpdates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkHistoryUpdates() {
        try {
            Logger.Log("1 Resolving HttpClientRequestHistory...");
            BHistoryService historyService = (BHistoryService) Sys.getService(BHistoryService.TYPE);

            if (historyService == null) {
                Logger.Error("BHistoryService not available.");
                return;
            }

            BHistoryDatabase db = historyService.getDatabase();

            if (db != null) {

                db.addHistoryEventListener(new HistoryEventListener() {
                    @Override
                    public void historyEvent(BHistoryEvent bHistoryEvent) {
                        String path = getHistorySource();
                        BOrd ord = BOrd.make(path);

                        BIHistory history = (BIHistory) ord.resolve().get();
                        Logger.Log("2  History resolved successfully");
                        BHistoryConfig config = history.getConfig();

                        BHistoryId historyId = config.getId();

                        if (!bHistoryEvent.getHistoryId().equals(historyId) || getHistorySource().isEmpty()) return;
                        Logger.Log("3  History resolved successfully");
                        String rawRecord = bHistoryEvent.getRecordSet().getLastRecord().toString();
                        try {
                            setResultOut(rawRecord);
                            Logger.Log("5 Table fetched: " + rawRecord);
                            Logger.Log("6 Table fetched: " + bHistoryEvent.getRecordSet().getLastRecord());
                            Logger.Log("7 getName: " + bHistoryEvent.getRecordSet().getLastRecord().getName());
                            Logger.Log("8 getType: " + bHistoryEvent.getRecordSet().getLastRecord().getType());
                            Logger.Log("9 getType: " + bHistoryEvent.getRecordSet().getLastRecord().getSchema());

                            BHistoryRecord record = bHistoryEvent.getRecordSet().getLastRecord();
                            BHistorySchema schema = record.getSchema();
                            String schemaStr = schema.toString();
                            Logger.Log("9 Schema: " + schemaStr);
                            // Create a JSON object to store the schema and values
                            JSONObject jsonObject = new JSONObject();

                            // Split the schema to get field names
                            String[] fields = schemaStr.split(";");
                            for (String field : fields) {
                                String[] keyValue = field.split(",", 2);
                                if (keyValue.length == 2) {
                                    String key = keyValue[0].trim();
                                    String type = keyValue[1].trim();

                                    try {
                                        BValue value = record.get(key);
                                        if (value != null) {
                                            jsonObject.put(key, value.toString());  // Store key-value pair in JSON object
                                            Logger.Log("10 Field: " + key + " => Type: " + type + " => Value: " + value.toString());
                                        } else {
                                            jsonObject.put(key, "null");  // If value is null, store "null"
                                            Logger.Log("10 Field: " + key + " => Type: " + type + " => Value: null");
                                        }
                                    } catch (Exception e) {
                                        Logger.Log("Error reading value for field '" + key + "': " + e.getMessage());
                                    }
                                } else {
                                    Logger.Log("Malformed schema field: " + field);
                                }
                            }

                            // Convert the JSON object to string and set it as the result
                            setResultOut(jsonObject.toString(2));  // Pretty print with indentation level 2
                            Logger.Log("11 Final JSON Object: " + jsonObject.toString(2));  // Log the final JSON object
                        } catch (Exception e) {
                            setResultOut("Error parsing history event: \" + e.getMessage()");
                            Logger.Log("Error parsing history event: " + e.getMessage());
                        }
                    }
                });
            } else {
                setResultOut("db not available");
                Logger.Error("db not available");
            }
        } catch (Exception e) {
            setResultOut("History read failed: " + e.getMessage());
            Logger.Error("History read failed: " + e.getMessage());
        }
    }
    private int inputCounter = 0;

    public void doGeneratePdf() {
        try {
            // 1. Fetch history records
            Map<String, Object> records = GetAllHistory(getHistorySource());

            // 2. Prepare data list
            List<DataRow> data = new ArrayList<>();

            // 3. Extract "histories" map
            Map<String, List<Map<String, String>>> histories =
                    (Map<String, List<Map<String, String>>>) records.get("histories");

            if (histories != null && !histories.isEmpty()) {
                for (Map.Entry<String, List<Map<String, String>>> entry : histories.entrySet()) {
                    for (Map<String, String> item : entry.getValue()) {
                        String timestamp = item.get("timestamp");
                        String opening = item.get("openingReading");
                        String closing = item.get("closingReading");
                        String consumption = item.get("consumption");

                        data.add(new DataRow(timestamp, opening, closing, consumption));
                    }
                }
            }

            // 4. Extract dynamic date range
            String startDate = (String) records.getOrDefault("startDate", "NA");
            String endDate = (String) records.getOrDefault("endDate", "NA");

            String startTime = "00:00:00";
            String endTime = "12:00:00";
            String interval = "1 day";

            // 5. Generate PDF
            generateAlarmPdf(data, startDate, startTime, endDate, endTime, interval);

            System.out.println("✅ PDF generated with " + data.size() + " records.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void generateAlarmPdf(List<DataRow> rows, String startDate, String startTime, String endDate, String endTime, String interval) {
        try {
            File pdfDir;
            File stationHome = Sys.getStationHome();
            pdfDir = new File(stationHome, "pdf");

         // Generate unique name using current timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File pdfFile = new File(pdfDir, "alarm_output_" +getHistorySource()+"_"+ timestamp + ".pdf");

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
            addTableHeader(table, "Timestamp", "Opening Reading", "Closing Reading", getHistorySource());

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
            setResultOut(("✅ PDF created at: " + pdfDir));
        } catch (Exception e) {
            setResultOut("❌ PDF generation failed: " + e.getMessage());
            System.err.println("❌ PDF generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Map<String, Object> GetAllHistory(String historySource) {
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

    private PdfPCell getCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setPadding(5);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private void addTableHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h));
            cell.setBackgroundColor(WebColors.getRGBColor("#00FFFF")); // CYAN
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    @Override
    public void stopped() throws Exception {
        super.stopped();
        HttpHistoryPost.stop();
    }

    public static String[] getAllHistoryNames() {
        List<String> historyNames = new ArrayList<>();

        try {
            BHistoryService historyService = (BHistoryService) Sys.getService(BHistoryService.TYPE);
            if (historyService == null) {
                Logger.Error("History service not available.");
                return new String[0];
            }

            BHistoryDatabase db = historyService.getDatabase();
            BIHistory[] histories = db.getHistories();

            for (BIHistory history : histories) {
                try {
                    String fullPath = history.getConfig().getId().toString(); // e.g. "/NetCool/JAL_SCO_L00_MR01_IR_CMD"
                    String[] parts = fullPath.split("/"); // split by "/"
                    if (parts.length > 0) {
                        String finalName = parts[parts.length - 1]; // get last segment
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

        return historyNames.toArray(new String[0]);
    }


}
