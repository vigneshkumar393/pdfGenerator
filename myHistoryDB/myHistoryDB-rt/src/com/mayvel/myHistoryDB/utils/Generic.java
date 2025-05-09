package com.mayvel.myHistoryDB.utils;

import com.mayvel.myHistoryDB.Const.Consts;
import com.mayvel.myHistoryDB.component.HistoryDBHelper;

import javax.baja.alarm.BAlarmRecord;
import javax.baja.sys.BAbsTime;
import javax.net.ssl.SSLSocket;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Generic {
    public static boolean isWithinMinutes(Date alarmDate, Date currentDate, double duration) {
        long differenceMillis = currentDate.getTime() - alarmDate.getTime();
        long threeMinutesMillis = (long) duration * 60 * 1000;
        return differenceMillis <= threeMinutesMillis;
    }

    public static String ConvertDateToStringWithPattern(BAbsTime absTime, String pattern) {
        if(pattern == null) pattern = Consts.default_pattern;
        Date timeStampDate = new Date(absTime.getMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(timeStampDate);
    }

    public static String formatTimeStamp(BAbsTime alarmTimeStamp){
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yy h:mm a", Locale.ENGLISH);
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yy hh:mm:ss a", Locale.ENGLISH);

        try{
            Date parsedDate = inputFormat.parse(alarmTimeStamp.toString());
            return outputFormat.format(parsedDate);
        }catch (Exception e){
            Logger.Error("Error parsing timestamp: "+e.getMessage());
            return "";
        }
    }

    public static Date convertStringToDate(String Date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy hh:mm:ss a zzz");
        Date date = null;
        try {
            date = dateFormat.parse(Date);
        } catch (Exception e) {
            System.out.println("Error Parsing date: " + e.getMessage());
        }
        return date;
    }

    public static boolean isValidDate(String formattedNormalStamp) {
        return formattedNormalStamp.contains("-70");
    }

    public static void appendField(StringBuilder jsonString, String key, Object value) {
        jsonString.append("\"").append(key).append("\":");

        if (value instanceof String) {
            jsonString.append("\"").append(value).append("\"");
        } else {
            jsonString.append(value);
        }

        jsonString.append(",");
    }


    public static PrintWriter buildHeader(SSLSocket socket, boolean autoFlush, String host, String port, String endpoint) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), autoFlush);
            String websocketKey = generateRandomWebSocketKey();
            out.println("GET " + endpoint + " HTTP/1.1");
            out.println("Host: " + host + ":" + port);
            out.println("Upgrade: websocket");
            out.println("Connection: Upgrade");
            out.println(Consts.web_key_header + ": " + websocketKey);
            out.println("Sec-WebSocket-Version: 13");
            out.println("");
            return out;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public static String buildRecordToString(BAlarmRecord record) {
        Map<String, String> mapData = HistoryDBHelper.convertToMap(record);
        String alarmClass =  record.getAlarmClass();
        Boolean ackState = record.isAcknowledged();
        String formattedAckTimeStamp = Generic.ConvertDateToStringWithPattern(record.getAckTime(),null);
        String formattedTimeStamp = Generic.ConvertDateToStringWithPattern(record.getTimestamp(), null);
        String formattedNormalStamp = Generic.ConvertDateToStringWithPattern(record.getNormalTime(), null);
        String uUidData = record.getUuid().toString();
        String normalTimeStampData = Generic.isValidDate(formattedNormalStamp) ? "null" : formattedNormalStamp;
        String ackTimeStampData = Generic.isValidDate(formattedAckTimeStamp)?"null":formattedAckTimeStamp;

        return ToJsonString(mapData,alarmClass,formattedTimeStamp,normalTimeStampData,uUidData,ackTimeStampData,ackState);
    }


    private static String generateRandomWebSocketKey() {
        byte[] nonce = new byte[16];
        new Random().nextBytes(nonce);
        return Base64.getEncoder().encodeToString(nonce);
    }

    public static String ToJsonString(Map<String, String> data,String alarmClassValue,String timeStampValue,String normalTime,String uUid,String acknowledgeTime,boolean Ackstate) {
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
//        System.out.println("The normal time is : "+normalTime);

        if(!data.containsKey("topics")){
            Generic.appendField(jsonString,"alarmClass",alarmClassValue);
            Generic.appendField(jsonString,"timeStamp",timeStampValue);
            Generic.appendField(jsonString,"normalTime",normalTime);
            Generic.appendField(jsonString,"UUid",uUid);
            Generic.appendField(jsonString,"ACKTime",acknowledgeTime);
            Generic.appendField(jsonString,"AckState",Ackstate);
        }
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!entry.getKey().equals("notes")) { // Exclude "notes"
                Generic.appendField(jsonString, entry.getKey(), entry.getValue());
            }
        }

        if (jsonString.length() > 1) {
            jsonString.setLength(jsonString.length() - 1);
        }

        jsonString.append("}");
        return jsonString.toString();
    }

    public static String convertToExpectedFormat(String input) throws ParseException{
        SimpleDateFormat inputFormat = new SimpleDateFormat("yy-MM-dd hh:mm:ss a");
        Date date = inputFormat.parse(input);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        outputFormat.setTimeZone(TimeZone.getTimeZone("IST"));
        String bAbsTimeFormat = outputFormat.format(date);
        return bAbsTimeFormat;
    }
}
