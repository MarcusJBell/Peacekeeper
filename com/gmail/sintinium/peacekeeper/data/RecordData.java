package com.gmail.sintinium.peacekeeper.data;

import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;

import javax.annotation.Nullable;

public class RecordData {

    public int recordID;
    public Integer playerID;
    public String ip;
    public int type;
    public Long time, length;
    public String reason;
    public Integer adminID;
    @Nullable
    public String category;

    public RecordData(int recordID, Integer playerID, String ip, int type, Long time, Long length, String reason, Integer adminID, String category) {
        this.recordID = recordID;
        this.playerID = playerID;
        this.ip = ip;
        this.type = type;
        this.time = time;
        this.length = length;
        this.reason = reason;
        this.adminID = adminID;
        this.category = category;
    }

    public String getTypeName() {
        switch (type) {
            case PlayerRecordTable.WARNING:
                return "Warning";
            case PlayerRecordTable.KICK:
                return "Kick";
            case PlayerRecordTable.MUTE:
                return "Mute";
            case PlayerRecordTable.BAN:
                if (length != null) return "Suspension";
                return "Perma-Ban";
            case PlayerRecordTable.IP:
                return "IP";
            default:
                return "ERROR/UNKNOWN";
        }
    }

}
