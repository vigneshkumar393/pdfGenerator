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
}
