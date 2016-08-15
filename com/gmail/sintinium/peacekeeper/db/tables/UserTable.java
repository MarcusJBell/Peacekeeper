package com.gmail.sintinium.peacekeeper.db.tables;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.PlayerData;
import com.gmail.sintinium.peacekeeper.db.utils.SQLTableUtils;
import com.gmail.sintinium.peacekeeper.db.utils.SQLUtils;
import com.gmail.sintinium.peacekeeper.utils.UUIDUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserTable extends BaseTable {

    // USER CONTAINS : (PlayerID, Time, Username, UUID, IP)

    public UserTable(Peacekeeper peacekeeper) {
        super(peacekeeper, "Users");
        db = peacekeeper.database;
        String tableSet = SQLTableUtils.getTableSet(
                new String[]{"PlayerID", "Username", "UUID", "IP"},
                new String[]{SQLTableUtils.INTEGER + " PRIMARY KEY", SQLTableUtils.VARCHAR + "(20)", SQLTableUtils.VARCHAR + "(50) UNIQUE", SQLTableUtils.VARCHAR + "(30)"}
        );
        init(tableSet);
    }

    public void updateUser(Player player, int playerID) {
        if (player == null) return;
        String set = SQLUtils.getAsSQLSet(
                new String[]{"Username", "IP"},
                new String[]{player.getName(), player.getAddress().getAddress().getHostAddress()}
        );
        updateValue(set, "PlayerID", playerID);
    }

    public void addUser(Player player) {
        if (player == null) return;
        insert(new String[]{"Username", "UUID", "IP"},
                new String[]{player.getName(), player.getUniqueId().toString(), player.getAddress().getAddress().getHostAddress()});
    }

    public void removeUser(int playerID) {
        deleteRow("PlayerID", playerID);
    }

    public boolean doesPlayerExist(Player player) {
        return doesValueExist("UUID", player.getUniqueId().toString());
    }

    public boolean doesPlayerExist(int playerID) {
        return doesValueExist("PlayerID", playerID);
    }

    public Integer getPlayerIDFromUsername(String username) {
        try {
            ResultSet set = db.query("SELECT PlayerID FROM " + tableName + " WHERE Username LIKE '" + username + "' ORDER BY Length(" + "Username" + ") ASC;");
            if (!set.next()) {
                set.close();
                return null;
            }
            int playerID = set.getInt("PlayerID");
            set.close();
            return playerID;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Integer getPlayerIDFromUUID(String uuid) {
        return getInt("PlayerID", "UUID", uuid);
    }

    @Nullable
    public PlayerData getPlayerData(int playerID) {
        try {
            ResultSet set = getStarSet(playerID);
            PlayerData data = getDataFromStarSet(set);
            set.close();
            return data;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public PlayerData getPlayerData(CommandSender sender, String username) {
        try {
            ResultSet set = db.query("SELECT * FROM " + tableName + " WHERE Username LIKE '" + username + "' ORDER BY LENGTH(Username) ASC;");
            List<PlayerData> datas = new ArrayList<>();
            while (set.next()) {
                datas.add(getDataFromStarSet(set));
            }
            set.close();
            if (datas.size() > 1 && valueCount("Username", "'" + datas.get(0).username + "'") > 1) {
                handleDuplicates(sender, datas);
            }
            if (datas.isEmpty())
                return null;
            return datas.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void handleDuplicates(CommandSender sender, List<PlayerData> playerDatas) {
        sender.sendMessage(ChatColor.YELLOW + "Updating duplicate usernames...");
        sender.sendMessage(ChatColor.YELLOW + "This will take ~" + playerDatas.size() + " seconds");
        for (PlayerData p : playerDatas) {
            // 1 second delay to prevent spamming the api server
            // This method will rarely need to be called but on the occasion it does it will take ~1 second per username
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String oldName = p.username;
            p.username = UUIDUtils.getChangedUsernameFromUUID(p.uuid);
            updateName(p.uuid, p);
            sender.sendMessage(ChatColor.YELLOW + "Updating duplicate names for: " + oldName + " to " + p.username);
        }
    }

    private void updateName(UUID uuid, PlayerData playerData) {
        if (uuid == null || playerData == null) return;
        String set = SQLUtils.getAsSQLSet(
                new String[]{"Username"},
                new String[]{playerData.username}
        );
        updateValue(set, "PlayerID", playerData.playerID);
    }

    public List<String> getUUIDSFromIP(String ip) {
        try {
            List<String> uuids = new ArrayList<>();
            ResultSet set = db.query("SELECT UUID FROM " + tableName + " WHERE IP='" + ip + "';");
            while (set.next()) {
                uuids.add(set.getString("UUID"));
            }
            set.close();
            return uuids;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    public String getUsername(Integer id) {
        if (id == null) return "AutoModerator";
        String username = getString("Username", "PlayerID", id);
        return username == null || username.equals("null") ? "AutoModerator" : username;
    }

    public String getUserUUID(int id) {
        return getString("UUID", "PlayerID", id);
    }

    public String getIP(int id) {
        return getString("IP", "PlayerID", id);
    }

    private PlayerData getDataFromStarSet(ResultSet set) throws SQLException {
        return new PlayerData(set.getInt("PlayerID"), set.getString("Username"), UUID.fromString(set.getString("UUID")), set.getString("IP"));
    }

}
