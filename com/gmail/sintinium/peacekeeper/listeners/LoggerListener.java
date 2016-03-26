package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class LoggerListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChatEvent(AsyncPlayerChatEvent event) {
        Peacekeeper.logFile.logChat(event.getPlayer().getName(), ChatColor.stripColor(event.getMessage()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String split[] = event.getMessage().split("\\s+");
        if (split.length <= 0) return;
        String m = split[0].toLowerCase();
        if (m.equalsIgnoreCase("/r") || m.equalsIgnoreCase("/msg") || m.equalsIgnoreCase("/tell") || m.equalsIgnoreCase("/me") || m.equalsIgnoreCase("/say") || m.equalsIgnoreCase("/afk") || m.equalsIgnoreCase("/m") || m.equalsIgnoreCase("/whisper")) {
            Peacekeeper.logFile.logChat(event.getPlayer().getName(), ChatColor.stripColor(event.getMessage()));
        }
    }

}
