package com.gmail.sintinium.peacekeeper.db.tables;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.PlayerData;
import com.gmail.sintinium.peacekeeper.db.utils.SQLTableUtils;
import com.gmail.sintinium.peacekeeper.db.utils.SQLUtils;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UserTable extends BaseTable {

    // USER CONTAINS : (PlayerID, Time, Username, UUID, IP)

    public UserTable(Peacekeeper peacekeeper) {
        super(peacekeeper, "Users");
        db = peacekeeper.database;
        String tableSet = SQLTableUtils.getTableSet(
                new String[]{"PlayerID", "Time", "Username", "UUID", "IP"},
                new String[]{SQLTableUtils.INTEGER + " PRIMARY KEY", SQLTableUtils.INTEGER, SQLTableUtils.VARCHAR + "(20)", SQLTableUtils.VARCHAR + "(50)", SQLTableUtils.VARCHAR + "(30)"}
        );
        init(tableSet);
    }

    public void updateUser(Player player, int playerID) {
        if (player == null) return;
        String set = SQLUtils.getAsSQLSet(
                new String[]{"Username", "UUID", "IP"},
                new String[]{player.getName(), player.getUniqueId().toString(), player.getAddress().getHostName()}
        );
        updateValue(set, "PlayerID", playerID);
    }

    public void addUser(Player player) {
        if (player == null) return;
        insert(new String[]{"Time", "Username", "UUID", "IP"},
                new String[]{String.valueOf(System.currentTimeMillis()), player.getName(), player.getUniqueId().toString(), player.getAddress().getHostName()});
    }

    public void removeUser(int playerID) {
        deleteRow("PlayerID", playerID);
    }

    public boolean doesPlayerExist(Player player) {
        return player != null && doesValueExist("UUID", player.getUniqueId().toString());
    }

    public boolean doesPlayerExist(int playerID) {
        return doesValueExist("PlayerID", playerID);
    }

    public String getOfflineUUID(String username) {
        if (username == null)
            return null;
        return getStringLike("UUID", "Username", username + "%");
    }

    public Integer getId(String uuid) {
        return getInt("PlayerID", "UUID", uuid);
    }

    public PlayerData getPlayerData(int id) {
        return new PlayerData(id, getUserTime(id), getUsername(id), UUID.fromString(getUserUUID(id)), getIP(id));
    }

    public long getUserTime(int id) {
        return getLong("Time", "PlayerID", id);
    }

    public String getUsername(Integer id) {
        if (id == null) return null;
        return getString("Username", "PlayerID", id);
    }

    public String getUserUUID(int id) {
        return getString("UUID", "PlayerID", id);
    }

    public String getIP(int id) {
        return getString("IP", "PlayerID", id);
    }

}
