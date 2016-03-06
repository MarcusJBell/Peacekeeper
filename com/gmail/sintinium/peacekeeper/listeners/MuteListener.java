package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.MuteData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerMuteTable;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class MuteListener implements Listener {

    private Peacekeeper peacekeeper;

    public MuteListener(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                Integer playerID = peacekeeper.userTable.getPlayerIDFromUUID(event.getPlayer().getUniqueId().toString());
                if (playerID == null) return;
                if (peacekeeper.muteTable.isPlayerMuted(playerID)) {
                    final Integer muteID = peacekeeper.muteTable.getMuteIDFromPlayerID(playerID);
                    if (muteID != null) {
                        final MuteData muteData = peacekeeper.muteTable.muteData(muteID);
                        // Run on main thread to prevent concurrentmoddifcationexception
                        Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                            @Override
                            public void run() {
                                peacekeeper.muteTable.mutedPlayers.put(event.getPlayer().getUniqueId(), muteData);
                            }
                        });
                    }
                }
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        peacekeeper.muteTable.mutedPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void playerChatEvent(AsyncPlayerChatEvent event) {
        if (peacekeeper.muteTable.mutedPlayers.isEmpty()) return;
        MuteData muteDataFromMap = peacekeeper.muteTable.mutedPlayers.get(event.getPlayer().getUniqueId());
        if (muteDataFromMap == null) return;
        Integer playerID = muteDataFromMap.mutedUser;
        if (playerID == null) return;
        MuteData muteData = isMuted(event.getPlayer(), playerID);
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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void commandPreProcess(PlayerCommandPreprocessEvent event) {
        if (peacekeeper.muteTable == null || !peacekeeper.muteTable.mutedPlayers.containsKey(event.getPlayer().getUniqueId())) return;
        String split[] = event.getMessage().split("\\s+");
        if (split.length <= 0) return;
        String m = split[0].toLowerCase();
        Bukkit.getConsoleSender().sendMessage(m);
        if (m.equalsIgnoreCase("/r") || m.equalsIgnoreCase("/msg") || m.equalsIgnoreCase("/tell") || m.equalsIgnoreCase("/me") || m.equalsIgnoreCase("/say") || m.equalsIgnoreCase("/afk") || m.equalsIgnoreCase("/m") || m.equalsIgnoreCase("/whisper")) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot use that command while muted.");
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        peacekeeper.commandManager.powerToolCommand.onInteract(event);
    }

    public MuteData isMuted(final Player player, final int playerID) {
        final PlayerMuteTable muteTable = peacekeeper.muteTable;

        MuteData muteData = muteTable.mutedPlayers.get(player.getUniqueId());
        if (muteData.muteLength != null && (muteData.muteTime + muteData.muteLength) - System.currentTimeMillis() <= 0) {
            peacekeeper.muteTable.mutedPlayers.remove(player.getUniqueId());

            peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
                @Override
                public void runTask() {
                    muteTable.unmutePlayer(playerID);
                    player.sendMessage(ChatColor.YELLOW + "You have been unmuted.");
                }
            });
            return null;
        }
        return muteData;
    }

}
