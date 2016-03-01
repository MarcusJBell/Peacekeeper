package com.gmail.sintinium.peacekeeper.data;

public class ReportData {

    public int reportID;
    public int playerID;
    public String message;
    public long time;
    public String categories;

    public ReportData(int reportID, int playerID, String message, long time, String categories) {
        this.reportID = reportID;
        this.playerID = playerID;
        this.message = message;
        this.time = time;
        this.categories = categories;
    }
}
