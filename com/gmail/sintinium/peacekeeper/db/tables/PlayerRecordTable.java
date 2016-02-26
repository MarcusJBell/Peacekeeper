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
                new String[]{"RecordID", "PlayerID", "IP", "Type", "Time", "Length", "Reason", "Admin", "StockID"},
                new String[]{SQLTableUtils.INTEGER + " PRIMARY KEY", SQLTableUtils.INTEGER, SQLTableUtils.TEXT, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER}
        );
        init(tableSet);
    }

    public int addRecord(@Nullable Integer PlayerID, @Nullable String ip, @Nullable Integer adminID, int type, @Nullable Long length, String reason, @Nullable Integer stockID) {
        return insert(
                new String[]{"PlayerID", "IP", "Type", "Time", "Length", "Reason", "Admin", "StockID"},
                new String[]{String.valueOf(PlayerID), ip, String.valueOf(type), String.valueOf(System.currentTimeMillis()), String.valueOf(length), reason, String.valueOf(adminID), String.valueOf(stockID)}
        );
    }

    public void removeRecord(int recordID) {
        deleteRow("RecordID", recordID);
    }

    public void updateRecord(RecordData data) {
        String[] colList = SQLUtils.getAsSQLArray(new String[]{"PlayerID", "IP", "Type", "Length", "Reason", "Admin", "StockID"});
        String[] valueList = SQLUtils.getAsSQLArray(new Object[]{data.playerID, data.type, data.length, data.reason, data.adminID, data.stockID});
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

    @Nullable
    public Integer getUser(int recordID) {
        return getInt("PlayerID", "RecordID", recordID);
    }

    public int getType(int recordID) {
        return getInt("Type", "RecordID", recordID);
    }

    public Long getTime(int recordID) {
        return getLong("Time", "RecordID", recordID);
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

    public RecordData getDataFromStarSet(ResultSet set) throws SQLException {
        return new RecordData(set.getInt("RecordID"), set.getInt("PlayerID"), set.getString("IP"), set.getInt("Type"), set.getLong("Time"), set.getLong("Length"), set.getString("Reason"), set.getInt("Admin"), set.getInt("StockID"));
    }

}
