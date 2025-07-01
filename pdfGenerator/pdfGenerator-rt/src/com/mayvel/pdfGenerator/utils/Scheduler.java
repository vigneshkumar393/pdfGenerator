package com.mayvel.pdfGenerator.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public static void schedule(Runnable task, int initialDelay, int duration, TimeUnit unit) {
        executor.scheduleAtFixedRate(task, initialDelay, duration, unit);
    }
}
