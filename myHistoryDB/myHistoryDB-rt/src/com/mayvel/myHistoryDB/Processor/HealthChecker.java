package com.mayvel.myHistoryDB.Processor;


import com.mayvel.myHistoryDB.Const.Consts;
import com.mayvel.myHistoryDB.utils.Generic;
import com.mayvel.myHistoryDB.utils.Scheduler;

import java.util.concurrent.TimeUnit;
public class HealthChecker {
    public static void scheduleHealthMonitor(){
        StringBuilder jsonString = new StringBuilder();
        jsonString.append("{");
        Generic.appendField(jsonString, "status", "ok");
        if (jsonString.length() > 1) {
            jsonString.setLength(jsonString.length() - 1);
        }
        jsonString.append("}");

        Runnable task = new Runnable() {
            @Override
            public void run() {
                WebSocketClient.clientSideWebsocketRealtime(Consts.host,jsonString.toString(),String.valueOf(Consts.port),Consts.sync_route);
            }
        };
        Scheduler.schedule(task, 0, 1, TimeUnit.MINUTES);
    }
}
