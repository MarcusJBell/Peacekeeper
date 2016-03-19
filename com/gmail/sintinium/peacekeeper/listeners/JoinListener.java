package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.db.tables.UserTable;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    Peacekeeper peacekeeper;

    public JoinListener(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    @EventHandler(priority = EventPriority.LOWEST)
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
