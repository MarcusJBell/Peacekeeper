package com.gmail.sintinium.peacekeeper.db.tables;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.RecordData;
import com.gmail.sintinium.peacekeeper.db.utils.SQLTableUtils;
import com.gmail.sintinium.peacekeeper.db.utils.SQLUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerRecordTable extends BaseTable {

    // (RecordID, @Nullable PlayerID, @Nullable IP, Type, @Nullable Length, Reason, @Nullable Admin, @Nullable Severity)

    public static final int WARNING = 0, KICK = 1, MUTE = 2, BAN = 3, IP = 4;

    public PlayerRecordTable(Peacekeeper peacekeeper) {
        super(peacekeeper, "PlayerRecords");
        db = peacekeeper.database;
        String tableSet = SQLTableUtils.getTableSet(
                new String[]{"RecordID", "PlayerID", "IP", "Type", "Length", "Reason", "Admin", "Severity"},
                new String[]{SQLTableUtils.INTEGER + " PRIMARY KEY", SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.TEXT, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER}
        );
        init(tableSet);
    }

    public int addRecord(@Nullable Integer PlayerID, @Nullable Integer adminID, int type, @Nullable Long length, String reason, @Nullable Integer severity) {
        return insert(
                new String[]{"PlayerID", "Type", "Length", "Reason", "Admin", "Severity"},
                new String[]{String.valueOf(PlayerID), String.valueOf(type), String.valueOf(length), reason, String.valueOf(adminID), String.valueOf(severity)}
        );
    }

    public void removeRecord(int recordID) {
        deleteRow("RecordID", recordID);
    }

    public void updateRecord(RecordData data) {
        String[] colList = SQLUtils.getAsSQLArray(new String[]{"PlayerID", "Type", "Length", "Reason", "Admin"});
        String[] valueList = SQLUtils.getAsSQLArray(new Object[]{data.playerID, data.type, data.length, data.reason, data.adminID});
        String set = SQLUtils.getAsSQLSet(colList, valueList);
        updateValue(set, "RecordID", String.valueOf(data.recordID));
    }

    public RecordData getRecordData(int recordID) {
        return new RecordData(recordID, getUser(recordID), getType(recordID), getLength(recordID), getReason(recordID), getAdmin(recordID), getSeverity(recordID));
    }

    public int[] getRecordsForUser(int PlayerID) {
        int[] result;
        try {
            Integer recordCount = valueCount("PlayerID", String.valueOf(PlayerID));
            if (recordCount == null) return null;
            ResultSet set = getSet("RecordID", "PlayerID", String.valueOf(PlayerID));
            result = new int[recordCount];
            for (int i = 0; i < recordCount; i++) {
                result[i] = set.getInt(i);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    @Nullable
    public Integer recordCountByType(int playerID, int recordType) {
        if (recordCount(playerID) == 0) return null;
        try {
            ResultSet set = db.query("SELECT COUNT(*) FROM " + tableName + " WHERE PlayerID=" + playerID + " AND Type=" + recordType + ";");
            return set.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer recordCount(int playerID) {
        return valueCount("PlayerID", playerID);
    }

    @Nullable
    public Integer getUser(int recordID) {
        return getInt("PlayerID", "RecordID", recordID);
    }

    public int getType(int recordID) {
        return getInt("Type", "RecordID", recordID);
    }

    @Nullable
    public Long getLength(int recordID) {
        return getLong("Length", "RecordID", recordID);
    }

    public String getReason(int recordID) {
        return getString("Reason", "RecordID", recordID);
    }

    @Nullable
    public Integer getAdmin(int recordID) {
        return getInt("Admin", "RecordID", recordID);
    }

    @Nullable
    public Integer getSeverity(int recordID) {
        return getInt("Severity", "RecordID", recordID);
    }

}
