package com.gmail.sintinium.peacekeeper.db.tables;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.db.utils.SQLTableUtils;
import com.gmail.sintinium.peacekeeper.utils.ArrayHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PlayerBanTable extends BaseTable {

    // BanID, Time, PlayerID, IP, Reason, AdminID, Length, Type, RecordID

    // Is used for priority of a ban. 0 being lower and 1+ is higher
    public static final int PLAYER = 0, IP = 1;

    public PlayerBanTable(@Nonnull Peacekeeper peacekeeper) {
        super(peacekeeper, "Bans");
        String tableSet = SQLTableUtils.getTableSet(
                new String[]{"BanID", "Time", "PlayerID", "IP", "Reason", "AdminID", "Length", "Type", "RecordID"},
                new String[]{SQLTableUtils.INTEGER + " PRIMARY KEY", SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.VARCHAR + "(30)",
                        SQLTableUtils.TEXT, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER}
        );
        init(tableSet);
    }

    public int banUser(int playerID, @Nonnull BanData banData) {
        if (banData.bannedUser != null && doesValueExist("PlayerID", banData.bannedUser)) {
            deleteRow("PlayerID", banData.bannedUser);
        }
        return insert(
                new Object[]{"Time", "PlayerID", "IP", "Reason", "AdminID", "Length", "Type", "RecordID"},
                new Object[]{banData.banTime, playerID, banData.ip, banData.reason, banData.adminId, banData.banLength, banData.type, banData.recordId}
        );
    }

    public Integer banIP(@Nonnull BanData banData) {
        if (banData.ip != null && doesValueExist("IP", banData.ip)) {
            deleteRow("IP", banData.ip);
        }
        return insert(
                new Object[]{"Time", "PlayerID", "IP", "Reason", "AdminID", "Length", "Type", "RecordID"},
                new Object[]{banData.banTime, null, banData.ip, banData.reason, banData.adminId, banData.banLength, banData.type, banData.recordId}
        );
    }

    public BanData getBanData(int banID) {
        try {
            ResultSet set = getStarSet(banID);
            if (!set.next()) {
                set.close();
                return null;
            }
            BanData banData = getDataFromStarSet(set);
            set.close();
            return banData;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int[] getPlayersBans(int playerID, String ip) {
        List<Integer> bans = new ArrayList<>();
        try {
            ResultSet set = db.query("SELECT BanID FROM " + tableName + " WHERE playerID=" + playerID + " OR " + "IP='" + ip + "';");
            while (set.next()) {
                int banID = set.getInt("BanID");
                if (set.wasNull()) continue;
                bans.add(banID);
            }
            set.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (bans.isEmpty()) return new int[0];
        return ArrayHelper.convertIntegers(bans);
    }

    public boolean isPlayerBanned(int playerID) {
        return doesValueExist("PlayerID", playerID);
    }

    public boolean isIPBanned(String ip) {
        return doesValueExist("IP", ip);
    }

    public void deleteBan(int banID) {
        deleteRow("BanID", banID);
    }

    public void unbanPlayer(int playerID) {
        deleteRow("PlayerID", playerID);
    }

    public void unbanIP(String ip) {
        deleteRow("IP", ip);
    }

    @Nullable
    public Integer getPlayerID(int banID) {
        return getInt("PlayerID", "BanID", banID);
    }

    public Long getBanTime(int banID) {
        return getLong("Time", "BanID", banID);
    }

    @Nullable
    public String getIP(int banID) {
        return getString("IP", "BanID", banID);
    }

    public String getReason(int banID) {
        return getString("Reason", "BanID", banID);
    }

    public Integer getAdminID(int banID) {
        return getInt("AdminID", "BanID", banID);
    }

    public Long getLength(int banID) {
        return getLong("Length", "BanID", banID);
    }

    public Integer getType(int banID) {
        return getInt("Type", "BanID", banID);
    }

    public Integer getRecordID(int banID) {
        return getInt("RecordID", "BanID", banID);
    }

    private BanData getDataFromStarSet(ResultSet set) throws SQLException {
        if (set == null) return null;
        Long time = set.getLong("Time");
        if (set.wasNull()) time = null;
        Integer playerID = set.getInt("PlayerID");
        if (set.wasNull()) playerID = null;
        Integer adminID = set.getInt("AdminID");
        if (set.wasNull()) adminID = null;
        String length = set.getString("Length");
        if (set.wasNull() || length.equals("null")) length = null;
        Long finalLength = null;
        if (length != null && !length.equals("null")) finalLength = Long.parseLong(length);
        return new BanData(set.getInt("BanID"), time, playerID, set.getString("IP"), set.getString("Reason"), adminID, finalLength, set.getInt("Type"), set.getInt("RecordID"));
    }

}
