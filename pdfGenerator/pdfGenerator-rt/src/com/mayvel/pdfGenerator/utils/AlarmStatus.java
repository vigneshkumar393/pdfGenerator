package com.mayvel.pdfGenerator.utils;

public class AlarmStatus {
    private boolean alarmIsNormal;
    private boolean alarmIsAcknowledged;

    public AlarmStatus(boolean isNormal, boolean isAcknowledged) {
        alarmIsNormal = isNormal;
        alarmIsAcknowledged = isAcknowledged;
    }

    public boolean isNormal() {return this.alarmIsNormal;}

    public boolean isAcked() {return this.alarmIsAcknowledged;}

    public void setNormal(boolean isNormal) {this.alarmIsNormal = isNormal;}

    public void setAcked(boolean isAcknowledged) {this.alarmIsAcknowledged = isAcknowledged;}
}
