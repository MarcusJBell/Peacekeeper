package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.BanUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BanListener implements Listener {

    public Map<UUID, BanData> cachedBans;
    public Map<UUID, Boolean> pendingPlayers;
    Peacekeeper peacekeeper;

    public BanListener(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        this.cachedBans = new ConcurrentHashMap<>();
        this.pendingPlayers = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(final PlayerLoginEvent event) {
        pendingPlayers.put(event.getPlayer().getUniqueId(), false);
        // Handle cached bans so player can't join after first time
        if (cachedBans.containsKey(event.getPlayer().getUniqueId())) {
            BanData banData = cachedBans.get(event.getPlayer().getUniqueId());
            if ((banData.banTime + banData.banLength) - System.currentTimeMillis() > 0) {
                event.disallow(PlayerLoginEvent.Result.KICK_BANNED, BanUtils.generateSyncedBanMessage(banData, banData.adminUsername));
            }
        }

        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                final BanData highestBan = peacekeeper.handleBan(event.getPlayer());
                if (highestBan == null) {
                    cachedBans.remove(event.getPlayer().getUniqueId());
                    if (pendingPlayers.get(event.getPlayer().getUniqueId())) {
                        Bukkit.getScheduler().runTaskLater(peacekeeper, new Runnable() {
                            @Override
                            public void run() {
                                pendingPlayers.remove(event.getPlayer().getUniqueId());
                                Bukkit.getServer().getPluginManager().callEvent(new PlayerJoinEvent(event.getPlayer(), ChatColor.YELLOW + event.getPlayer().getName() + " has joined the game"));
                            }
                        }, 20L);
                    } else {
                        pendingPlayers.remove(event.getPlayer().getUniqueId());
                    }
                    return;
                }
                cachedBans.put(event.getPlayer().getUniqueId(), highestBan);
                final String message = BanUtils.generateBanMessage(peacekeeper, highestBan);
                highestBan.adminUsername = peacekeeper.userTable.getUsername(highestBan.adminId);
                // Kick player after 1 second if banned
                Bukkit.getScheduler().runTaskLater(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        event.getPlayer().kickPlayer(message);
                    }
                }, 20L);
            }
        });
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        if (pendingPlayers.containsKey(event.getPlayer().getUniqueId())) {
            event.setJoinMessage(null);
            pendingPlayers.put(event.getPlayer().getUniqueId(), true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(final PlayerQuitEvent event) {
        if (cachedBans.containsKey(event.getPlayer().getUniqueId()))
            event.setQuitMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(final PlayerKickEvent event) {
        if (cachedBans.containsKey(event.getPlayer().getUniqueId()))
            event.setLeaveMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(final AsyncPlayerChatEvent event) {
        if (pendingPlayers.containsKey(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot chat for 1 second after joining.");
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "If you have been on the server for over 5 seconds please contact an administrator");
        }
    }
}
