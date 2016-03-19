package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class WarnListener implements Listener {

    private Peacekeeper peacekeeper;

    public WarnListener(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(peacekeeper, new Runnable() {
            @Override
            public void run() {
                peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
                    @Override
                    public void runTask() {
                        if (peacekeeper.warnTable.doesValueExist("PlayerID", peacekeeper.userTable.getPlayerIDFromUUID(event.getPlayer().getUniqueId().toString()))) {
                            peacekeeper.commandManager.warnCommand.warnPlayer(event.getPlayer());
                        }
                    }
                });
            }
        }, 60L);
    }

}
