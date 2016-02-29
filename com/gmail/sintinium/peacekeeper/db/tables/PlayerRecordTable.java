package com.gmail.sintinium.peacekeeper.db.tables;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.RecordData;
import com.gmail.sintinium.peacekeeper.db.utils.SQLTableUtils;
import com.gmail.sintinium.peacekeeper.db.utils.SQLUtils;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerRecordTable extends BaseTable {

    // (RecordID, @Nullable PlayerID, @Nullable IP, Type, Time, @Nullable Length, Reason, @Nullable Admin, @Nullable Severity)

    public static final int WARNING = 0, KICK = 1, MUTE = 2, BAN = 3, IP = 4;

    public PlayerRecordTable(Peacekeeper peacekeeper) {
        super(peacekeeper, "PlayerRecords");
        db = peacekeeper.database;
        String tableSet = SQLTableUtils.getTableSet(
                new String[]{"RecordID", "PlayerID", "IP", "Type", "Time", "Length", "Reason", "Admin", "Category"},
                new String[]{SQLTableUtils.INTEGER + " PRIMARY KEY", SQLTableUtils.INTEGER, SQLTableUtils.TEXT, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.TEXT}
        );
        init(tableSet);
    }

    public int addRecord(@Nullable Integer PlayerID, @Nullable String ip, @Nullable Integer adminID, int type, @Nullable Long length, String reason, @Nullable String category) {
        return insert(
                new String[]{"PlayerID", "IP", "Type", "Time", "Length", "Reason", "Admin", "Category"},
                new String[]{String.valueOf(PlayerID), ip, String.valueOf(type), String.valueOf(System.currentTimeMillis()), String.valueOf(length), reason, String.valueOf(adminID), category}
        );
    }

    public void removeRecord(int recordID) {
        deleteRow("RecordID", recordID);
    }

    public void clearPlayersRecords(int playerID) {
        deleteRow("PlayerID", playerID);
    }

    public void updateRecord(RecordData data) {
        String[] colList = SQLUtils.getAsSQLArray(new String[]{"PlayerID", "IP", "Type", "Length", "Reason", "Admin", "Category"});
        String[] valueList = SQLUtils.getAsSQLArray(new Object[]{data.playerID, data.type, data.length, data.reason, data.adminID, data.category});
        String set = SQLUtils.getAsSQLSet(colList, valueList);
        updateValue(set, "RecordID", String.valueOf(data.recordID));
    }

    public RecordData getRecordData(int recordID) {
        try {
            ResultSet set = getStarSet(recordID);
            if (!set.next()) {
                set.close();
                return null;
            }
            RecordData data = getDataFromStarSet(set);
            set.close();
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public List<RecordData> getRecordsForUser(int playerID, Long time) {
        List<RecordData> result = new ArrayList<>();
        try {
            Integer recordCount = recordCount(playerID);
            if (recordCount == null) return null;
            ResultSet set = getStarSet(playerID);
            while (set.next()) {
                result.add(getDataFromStarSet(set));
            }
            set.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    @Nullable
    public List<RecordData> getRecordsForUser(int playerID) {
        return getRecordsForUser(playerID, null);
    }

    @Nullable
    public List<RecordData> getRecordsByTypeWithinTime(int playerID, int recordType, Long time) {
        List<RecordData> result = new ArrayList<>();
        try {
            Integer recordCount = recordCount(playerID);
            if (recordCount == null) return null;
            if (recordCount == 0) return null;
            ResultSet set;
            if (time != null)
                set = db.query("SELECT * FROM " + tableName + " WHERE playerID=" + playerID + " AND Type=" + recordType + " AND " + " Time>=" + time + " ORDER BY rowid DESC;");
            else
                set = db.query("SELECT * FROM " + tableName + " WHERE playerID=" + playerID + " AND Type=" + recordType + " ORDER BY rowid DESC;");
            while (set.next()) {
                result.add(getDataFromStarSet(set));
            }
            set.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    @Nullable
    public List<RecordData> getRecordsByType(int playerID, int recordType) {
        return getRecordsByTypeWithinTime(playerID, recordType, null);
    }

    @Nullable
    public List<RecordData> getPlayerRecords(int playerID) {
        List<RecordData> result = new ArrayList<>();
        try {
            Integer recordCount = recordCount(playerID);
            if (recordCount == null) return null;
            if (recordCount == 0) return null;
            ResultSet set;
            set = db.query("SELECT * FROM " + tableName + " WHERE playerID=" + playerID + ";");
            while (set.next()) {
                result.add(getDataFromStarSet(set));
            }
            set.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    @Nullable
    public List<RecordData> getIPRecords(String ip) {
        List<RecordData> result = new ArrayList<>();
        ip = "'" + ip + "'";
        try {
            ResultSet set;
            set = db.query("SELECT * FROM " + tableName + " WHERE IP=" + ip + ";");
            while (set.next()) {
                result.add(getDataFromStarSet(set));
            }
            set.close();
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
            int count = set.getInt(1);
            set.close();
            return count;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer recordCount(int playerID) {
        return valueCount("PlayerID", playerID);
    }

    public RecordData getDataFromStarSet(ResultSet set) throws SQLException {
        Integer playerID = set.getInt("PlayerID");
        if (set.wasNull()) playerID = null;
        Long length = set.getLong("Time");
        if (set.wasNull()) length = null;
        Integer adminID = set.getInt("Admin");
        if (set.wasNull()) adminID = null;
        return new RecordData(set.getInt("RecordID"), playerID, set.getString("IP"), set.getInt("Type"), set.getLong("Time"), length, set.getString("Reason"), adminID, set.getString("Category"));
    }

}
