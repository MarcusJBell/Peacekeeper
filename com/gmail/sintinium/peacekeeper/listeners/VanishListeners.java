package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VanishListeners implements Listener {

    private Peacekeeper peacekeeper;

    public VanishListeners(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    public static void hidePlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.hidePlayer(player);
        }
    }

    public static void unhidePlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(player);
        }
    }

    @EventHandler
    public void tabComplete(PlayerChatTabCompleteEvent event) {
        for (String s : event.getTabCompletions()) {
            if (event.getTabCompletions().contains(s)) {
                event.getTabCompletions().remove(s);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        if (peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.contains(event.getPlayer().getName())) {
            event.setJoinMessage(null);
        }
        peacekeeper.scoreboardStatsHook.updateScoreboard();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        if (peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.contains(event.getPlayer().getName())) {
            event.setQuitMessage(null);
        }
        peacekeeper.scoreboardStatsHook.updateScoreboard();
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

}
