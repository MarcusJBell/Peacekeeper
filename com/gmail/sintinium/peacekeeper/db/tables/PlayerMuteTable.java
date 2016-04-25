package com.gmail.sintinium.peacekeeper.db.tables;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.MuteData;
import com.gmail.sintinium.peacekeeper.db.utils.SQLTableUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMuteTable extends BaseTable {

    // (MuteID, MuteTime User, Type, Length, Reason, Admin, RecordID)

    public ConcurrentHashMap<UUID, MuteData> mutedPlayers;

    public PlayerMuteTable(@Nonnull final Peacekeeper peacekeeper) {
        super(peacekeeper, "Mutes");
        mutedPlayers = new ConcurrentHashMap<>();
        String tableSet = SQLTableUtils.getTableSet(
                new String[]{"MuteID", "MuteTime", "PlayerID", "Length", "Reason", "AdminID", "RecordID"},
                new String[]{SQLTableUtils.INTEGER + " PRIMARY KEY", SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER, SQLTableUtils.TEXT, SQLTableUtils.INTEGER, SQLTableUtils.INTEGER}
        );
        init(tableSet);

        //Check every 15 seconds if a player's mute has expired. If so unmute them
        Bukkit.getScheduler().runTaskTimerAsynchronously(peacekeeper, new Runnable() {
            @Override
            public void run() {
                if (mutedPlayers.isEmpty()) return;
                for (Map.Entry<UUID, MuteData> entry : mutedPlayers.entrySet()) {
                    Player player = Bukkit.getPlayer(entry.getKey());
                    if (player == null) continue;
                    peacekeeper.muteListener.isMuted(player, entry.getValue().muteID);
                }
            }
        }, 10L, 15L * 20L);
    }

    public int muteUser(int playerID, Long length, String reason, Integer adminID, int recordID) {
        if (doesValueExist("PlayerID", playerID)) {
            deleteRow("PlayerID", playerID);
        }
        return insert(
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
        if (username == null) {
            username = "Console";
        }
        try {
            ResultSet set = getStarSet(muteID);
            MuteData data = getMuteDataFromStarSet(username, set);
            set.close();
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Null if muted by console, or auto-muted by filter
    @Nullable
    public Integer getAdminID(int muteID) {
        return getInt("AdminID", "MuteID", muteID);
    }

    public MuteData getMuteDataFromStarSet(String username, ResultSet set) throws SQLException {
        Long length = set.getLong("Length");
        if (set.wasNull()) length = null;
        Integer adminID = set.getInt("AdminID");
        if (set.wasNull()) adminID = null;
        return new MuteData(username, set.getInt("MuteID"), set.getLong("MuteTime"), set.getInt("PlayerID"), set.getString("Reason"), adminID, length, set.getInt("RecordID"));
    }


}
