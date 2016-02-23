package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.db.tables.UserTable;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.BanUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class JoinListener implements Listener {

    Peacekeeper peacekeeper;

    public JoinListener(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogin(final PlayerLoginEvent event) {
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                final BanData highestBan = peacekeeper.handleBan(event.getPlayer());
                if (highestBan == null) return;
                final String message = BanUtils.generateBanMessage(peacekeeper, highestBan);
                Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        event.getPlayer().kickPlayer(message);
                    }
                });
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final long time = System.currentTimeMillis();
        final UserTable db = peacekeeper.userTable;
        // Add player to database if not already, else update it to keep up with current usernames and IPs
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                if (!db.doesPlayerExist(event.getPlayer())) {
                    db.addUser(event.getPlayer());
                    peacekeeper.getLogger().info("Added user " + event.getPlayer().getName() + " to database. (" + (System.currentTimeMillis() - time) + "ms)");
                } else {
                    db.updateUser(event.getPlayer(), db.getPlayerIDFromUUID(event.getPlayer().getUniqueId().toString()));
                }
            }
        });
    }

}
