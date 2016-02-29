package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Iterator;

public class VanishListeners implements Listener {

    private Peacekeeper peacekeeper;

    public VanishListeners(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    public static void hidePlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("peacekeeper.command.vanish.cansee")) continue;
            p.hidePlayer(player);
        }
    }

    public static void superHidePlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getUniqueId().toString().equals("108c89bc-ab51-4609-a9d5-13bb8808df98") || p.getUniqueId().toString().equals("bb55301c-d10e-4368-bdbd-9563c2b79d35")) continue;
            p.hidePlayer(player);
        }
    }

    public static void unhidePlayer(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.showPlayer(player);
        }
    }

    public void hideToPlayer(Player player) {
        for (String s : peacekeeper.commandManager.superVanishCommand.superVanishedPlayers) {
            Player p = Peacekeeper.getPlayer(s);
            if (p != null)
                if (!p.getUniqueId().toString().equals("108c89bc-ab51-4609-a9d5-13bb8808df98") || !p.getUniqueId().toString().equals("bb55301c-d10e-4368-bdbd-9563c2b79d35")) {
                    player.hidePlayer(p);
                }
        }

        for (String s : peacekeeper.commandManager.vanishCommand.vanishedPlayers) {
            if (player.hasPermission("peacekeeper.command.vanish.cansee")) return;
            Player p = Peacekeeper.getPlayer(s);
            if (p != null) {
                player.hidePlayer(p);
            }
        }
    }

    @EventHandler
    public void onServerPing(ServerListPingEvent event) {
        Iterator iterator = event.iterator();
        while (iterator.hasNext()) {
            Player p = (Player) iterator.next();
            if (peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.contains(p.getName())) {
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void tabComplete(PlayerChatTabCompleteEvent event) {
        for (String s : peacekeeper.commandManager.superVanishCommand.superVanishedPlayers) {
            if (event.getTabCompletions().contains(s) && !event.getPlayer().canSee(Peacekeeper.getExactPlayer(s)))
                event.getTabCompletions().remove(s);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {
        if (peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.contains(event.getPlayer().getName())) {
            event.setJoinMessage(null);
            superHidePlayer(event.getPlayer());
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false));
        } else if (peacekeeper.commandManager.vanishCommand.vanishedPlayers.contains(event.getPlayer().getName())) {
            hidePlayer(event.getPlayer());
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2, false, false));
        }
        hideToPlayer(event.getPlayer());
        peacekeeper.scoreboardStatsHook.updateScoreboard();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        if (peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.contains(event.getPlayer().getName())) {
            event.setQuitMessage(null);
            peacekeeper.scoreboardStatsHook.updateScoreboard();
        }
        if (peacekeeper.commandManager.vanishCommand.vanishedPlayers.contains(event.getPlayer().getName())) {
            removeEffects(event.getPlayer());
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.contains(event.getPlayer().getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void presurePlate(PlayerInteractEvent event) {
        if (peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.contains(event.getPlayer().getName()) && event.getAction() == Action.PHYSICAL) {
            if (event.getClickedBlock().getType() == Material.GOLD_PLATE || event.getClickedBlock().getType() == Material.IRON_PLATE || event.getClickedBlock().getType() == Material.STONE_PLATE || event.getClickedBlock().getType() == Material.WOOD_PLATE) {
                event.setCancelled(true);
            }
        }
    }

    public void removeEffects(Player player) {
        for (PotionEffect p : player.getActivePotionEffects()) {
            if (p.getType() == PotionEffectType.INVISIBILITY && p.getAmplifier() > 1) {
                player.getActivePotionEffects().remove(p);
            }
        }
    }

}
