package com.gmail.sintinium.peacekeeper.data;

public class RecordData {

    public int recordID;
    public Integer playerID;
    public int type;
    public Long time, length;
    public String reason;
    public Integer adminID;
    public Integer severity;

    public RecordData(int recordID, Integer playerID, int type, Long time, Long length, String reason, Integer adminID, Integer severity) {
        this.recordID = recordID;
        this.playerID = playerID;
        this.type = type;
        this.time = time;
        this.length = length;
        this.reason = reason;
        this.adminID = adminID;
        this.severity = severity;
    }

}
