package com.gmail.sintinium.peacekeeper.db.tables;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.WarnData;
import com.gmail.sintinium.peacekeeper.db.utils.SQLTableUtils;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerWarnTable extends BaseTable {

    public PlayerWarnTable(Peacekeeper peacekeeper) {
        super(peacekeeper, "Warnings");
        String tableSet = SQLTableUtils.getTableSet(
                new String[]{"WarnID", "WarnTime", "PlayerID", "Reason", "AdminID", "RecordID"},
                new String[]{SQLTableUtils.INTEGER + " PRIMARY KEY", SQLTableUtils.INTEGER, SQLTableUtils.INTEGER + " UNIQUE", SQLTableUtils.TEXT, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER}
        );
        init(tableSet);
    }

    public int warnPlayer(int playerID, String reason, Integer adminID, int recordID) {
        return insert(
                new Object[]{"WarnTime", "PlayerID", "Reason", "AdminID", "RecordID"},
                new Object[]{System.currentTimeMillis(), playerID, reason, adminID, recordID}
        );
    }

    public WarnData warnData(int warnID) {
        try {
            ResultSet set = getStarSet(warnID);
            WarnData data = getWarnDataFromStarSet(set);
            set.close();
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer getWarnIDFromPlayerID(int playerID) {
        return getInt("WarnID", "PlayerID", playerID);
    }

    public void deleteWarning(int warnID) {
        deleteRow("WarnID", warnID);
    }

    private WarnData getWarnDataFromStarSet(ResultSet set) throws SQLException {
        Integer adminID = set.getInt("AdminID");
        if (set.wasNull()) adminID = null;
        return new WarnData(set.getInt("WarnID"), set.getLong("WarnTime"), set.getInt("PlayerID"), set.getString("Reason"), adminID, set.getInt("RecordID"));
    }

}
