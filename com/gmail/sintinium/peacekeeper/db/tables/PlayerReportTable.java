package com.gmail.sintinium.peacekeeper.db.tables;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.ReportData;
import com.gmail.sintinium.peacekeeper.db.utils.SQLTableUtils;

import javax.annotation.Nonnull;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerReportTable extends BaseTable {

    public PlayerReportTable(@Nonnull Peacekeeper peacekeeper, @Nonnull String tableName) {
        super(peacekeeper, tableName);

        String tableSet = SQLTableUtils.getTableSet(
                new String[]{"ReportID", "PlayerID", "Offender", "Message"},
                new String[]{SQLTableUtils.INTEGER + " PRIMARY KEY", SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.TEXT}
        );
        init(tableSet);
    }

    public int addReport(int playerID, Integer offender, String message) {
        return insert(
                new Object[]{"PlayerID", "Offender", "Message"},
                new Object[]{playerID, offender, message}
        );
    }

    public void deleteReport(int reportID) {
        deleteRow("ReportID", reportID);
    }

    public ReportData getReport(int reportID) {
        try {
            ResultSet set = getStarSet(reportID);
            ReportData data = reportDataFromStarSet(set);
            set.close();
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ReportData> getAllRecords() {
        try {
            List<ReportData> datas = new ArrayList<>();
            ResultSet set = db.query("SELECT * FROM " + tableName + ";");
            while (set.next()) {
                datas.add(reportDataFromStarSet(set));
            }
            set.close();
            return datas;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ReportData reportDataFromStarSet(ResultSet set) throws SQLException {
        Integer offenderID = set.getInt("Offender");
        if (set.wasNull()) offenderID = null;
        return new ReportData(set.getInt("ReportID"), set.getInt("PlayerID"), offenderID, set.getString("Message"));
    }

}
