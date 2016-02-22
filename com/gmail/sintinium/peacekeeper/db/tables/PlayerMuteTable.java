package com.gmail.sintinium.peacekeeper.db.tables;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.MuteData;
import com.gmail.sintinium.peacekeeper.db.utils.SQLTableUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

public class PlayerMuteTable extends BaseTable {

    // (MuteID, MuteTime User, Type, Length, Reason, Admin, RecordID)

    public HashMap<UUID, MuteData> mutedPlayers;

    public PlayerMuteTable(@Nonnull Peacekeeper peacekeeper) {
        super(peacekeeper, "Mutes");
        mutedPlayers = new HashMap<>();
        String tableSet = SQLTableUtils.getTableSet(
                new String[]{"MuteID", "MuteTime", "PlayerID", "Length", "Reason", "AdminID", "RecordID"},
                new String[]{SQLTableUtils.INTEGER + " PRIMARY KEY", SQLTableUtils.INTEGER, SQLTableUtils.INTEGER + " UNIQUE", SQLTableUtils.INTEGER, SQLTableUtils.TEXT, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER}
        );
        init(tableSet);
    }

    public int muteUser(int playerID, Long length, String reason, Integer adminID, int recordID) {
        return insertOrReplace(
                new Object[]{"MuteTime", "PlayerID", "Length", "Reason", "AdminID", "RecordID"},
                new Object[]{System.currentTimeMillis(), playerID, length, reason, adminID, recordID}
        );
    }

    @Nullable
    public Integer getMuteIDFromPlayerID(int playerID) {
        return getInt("MuteID", "PlayerID", playerID);
    }

    public void unmutePlayer(int playerID) {
        deleteRow("PlayerID", playerID);
    }

    public boolean isPlayerMuted(int playerID) {
        return doesValueExist("PlayerID", playerID);
    }

    public MuteData muteData(int muteID) {
        Integer adminID = getAdminID(muteID);
        String username = null;
        if (adminID != null)
            username = peacekeeper.userTable.getUsername(adminID);
        return new MuteData(username, muteID, getLength(muteID), getPlayerID(muteID), getReason(muteID), adminID, getTimeOfMute(muteID), getRecordID(muteID));
    }

    public int getRecordID(int muteID) {
        return getInt("RecordID", "MuteID", muteID);
    }

    public int getPlayerID(int muteID) {
        return getInt("PlayerID", "MuteID", muteID);
    }

    //Null if perma-mute
    @Nullable
    public Long getLength(int muteID) {
        return getLong("Length", "MuteID", muteID);
    }

    public Long getTimeOfMute(int muteID) {
        return getLong("MuteTime", "MuteID", muteID);
    }

    public String getReason(int muteID) {
        return getString("Reason", "MuteID", muteID);
    }

    //Null if muted by console, or auto-muted by filter
    @Nullable
    public Integer getAdminID(int muteID) {
        return getInt("AdminID", "MuteID", muteID);
    }


}
