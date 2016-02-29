package com.gmail.sintinium.peacekeeper.data;

import javax.annotation.Nullable;

public class ReportData {

    public int reportID;
    public int playerID;
    public Integer offender;
    public String message;

    public ReportData(int reportID, int playerID, @Nullable Integer offender, String message) {
        this.reportID = reportID;
        this.playerID = playerID;
        this.offender = offender;
        this.message = message;
    }
}
