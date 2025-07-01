package com.mayvel.pdfGenerator.component;

public class DataRow {
    public String timestamp;
    public String openingReading;
    public String closingReading;
    public String meterConsumption;

    public DataRow(String timestamp, String openingReading, String closingReading, String meterConsumption) {
        this.timestamp = timestamp;
        this.openingReading = openingReading;
        this.closingReading = closingReading;
        this.meterConsumption = meterConsumption;
    }
}
