package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.MuteData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerMuteTable;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MuteListener implements Listener {

    private Peacekeeper peacekeeper;

    public MuteListener(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Integer playerID = peacekeeper.userTable.getPlayerIDFromUUID(event.getPlayer().getUniqueId().toString());
        if (playerID == null) return;
        if (peacekeeper.muteTable.isPlayerMuted(playerID)) {
            Integer muteID = peacekeeper.muteTable.getMuteIDFromPlayerID(playerID);
            if (muteID != null)
                peacekeeper.muteTable.mutedPlayers.put(event.getPlayer().getUniqueId(), peacekeeper.muteTable.muteData(muteID));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        peacekeeper.muteTable.mutedPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void playerChatEvent(AsyncPlayerChatEvent event) {
        if (peacekeeper.muteTable.mutedPlayers.isEmpty()) return;
        MuteData muteData = isMuted(event.getPlayer(), peacekeeper.muteTable.mutedPlayers.get(event.getPlayer().getUniqueId()).mutedUser);
        if (muteData == null) return;
        event.setCancelled(true);
        if (muteData.muteLength == null) {
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are permanently muted by: " + ChatColor.YELLOW + muteData.adminName);
            if (Peacekeeper.appealUrl != null)
                event.getPlayer().sendMessage(ChatColor.YELLOW + "Appeal here: " + Peacekeeper.appealUrl);
            return;
        }
        if (System.currentTimeMillis() - muteData.lastTime > 30 * 1000) { //To prevent players from spamming chat and causing lag it will only allow them to see their time left after 30 seconds.
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are currently muted for: " + ChatColor.YELLOW + TimeUtils.millsToString((muteData.muteTime + muteData.muteLength) - System.currentTimeMillis()));
            muteData.lastTime = System.currentTimeMillis();
        } else {
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You are currently muted");
        }
    }

    public MuteData isMuted(Player player, int playerID) {
        PlayerMuteTable muteTable = peacekeeper.muteTable;

        MuteData muteData = muteTable.mutedPlayers.get(player.getUniqueId());
        if ((muteData.muteTime + muteData.muteLength) - System.currentTimeMillis() <= 0) {
            muteTable.unmutePlayer(playerID);
            peacekeeper.muteTable.mutedPlayers.remove(player.getUniqueId());
            return null;
        }
        return muteData;
    }

}
