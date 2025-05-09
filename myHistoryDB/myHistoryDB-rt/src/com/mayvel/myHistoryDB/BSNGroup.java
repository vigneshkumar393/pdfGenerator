package com.mayvel.myHistoryDB;

import com.mayvel.myHistoryDB.component.SNBaseComponent;
import com.mayvel.myHistoryDB.utils.Generic;
import com.mayvel.myHistoryDB.utils.Logger;
import com.tridium.json.JSONObject;

import javax.baja.history.*;
import javax.baja.history.db.BHistoryDatabase;
import javax.baja.naming.BOrd;
import javax.baja.nre.annotations.NiagaraProperty;
import javax.baja.nre.annotations.NiagaraType;
import javax.baja.sys.*;


@NiagaraType
@NiagaraProperty(name = "historySource", type = "String", defaultValue = "")

public class BSNGroup extends BComponent implements SNBaseComponent {
    public String URL = null;
    public String PORT = null;
    String id = null;
//region /*+ ------------ BEGIN BAJA AUTO GENERATED CODE ------------ +*/
//@formatter:off
    /*@ $com.mayvel.myAlarmDB.BSNGroup(2009474822)1.0$ @*/
    /* Generated Tue May 06 22:45:33 IST 2025 by Slot-o-Matic (c) Tridium, Inc. 2012-2025 */

//region Property "historySource"
    /**
     * Slot for the {@code historySource} property.
     *
     * @see #getHistorySource
     * @see #setHistorySource
     */
    public static final Property historySource = newProperty(0, "e.g. history:/NetCool/LogHistory", null);

    /**
     * Get the {@code postApiUrl} property.
     *
     * @see #historySource
     */
    public String getHistorySource() {
        return getString(historySource);
    }

    /**
     * Set the {@code postApiUrl} property.
     *
     * @see #historySource
     */
    public void setHistorySource(String v) {
        setString(historySource, v, null);
    }

    public static final Property out = newProperty(Flags.SUMMARY | Flags.READONLY, "", BFacets.make(BFacets.MULTI_LINE, BBoolean.TRUE));
    private volatile boolean running = true;
    private long lastTimestamp = -1;

    public String getOut() {
        return getString(out);
    }

    public void setOut(String v) {
        setString(out, v, null);
    }

    @Override
    public Type getType() {
        return TYPE;
    }

    public static final Type TYPE = Sys.loadType(BSNGroup.class);

    //endregion Type

//@formatter:on
//endregion /*+ ------------ END BAJA AUTO GENERATED CODE -------------- +*/


    public void changed(Property property, Context context) {
        super.changed(property, context);
        if (property.equals(historySource)) {
            Logger.Log("History source changed. Clearing out property.");
            setOut(""); // Clear the out property
        } // Clear the out property
    }

  @Override
  public void started() throws Exception {
    super.started();
    Logger.Log("BSNGroup started");
    checkHistoryUpdates();
  }

  @Override
    public void stopped() throws Exception {
        Logger.Log("BSNGroup stopped");
        running = false;
        super.stopped();
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
                            setOut(rawRecord);
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
                            setOut(jsonObject.toString(2));  // Pretty print with indentation level 2
                            Logger.Log("11 Final JSON Object: " + jsonObject.toString(2));  // Log the final JSON object
                        } catch (Exception e) {
                            setOut("Error parsing history event: \" + e.getMessage()");
                            Logger.Log("Error parsing history event: " + e.getMessage());
                        }
                    }
                });
            } else {
                setOut("db not available");
                Logger.Error("db not available");
            }
        } catch (Exception e) {
            setOut("History read failed: " + e.getMessage());
            Logger.Error("History read failed: " + e.getMessage());
        }
    }

    public void doSyncAll() {
        try {
            Logger.Log("Starting doSyncAll...");
            BMyHistoryDBNetwork[] networks = getChildren(BMyHistoryDBNetwork.class);
            for (BMyHistoryDBNetwork network : networks) {
                Logger.Log("Syncing network: " + network.getNetworkName());
                network.doSync();
            }
        } catch (Exception e) {
            Logger.Error("doSyncAll error: " + e.getMessage());
        }
    }

    @Override
    public boolean hasvalidLicense() {
        return false;
    }

    public void checkValidLicense() {

    }

    public String getId() {
        if (id == null) id = String.valueOf(hashCode());
        return id;
    }


}
