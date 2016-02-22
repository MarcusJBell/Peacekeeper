package com.gmail.sintinium.peacekeeper.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatUtils {

    // Clears chat for the given player/sender
    public static void clearChat(CommandSender sender) {
        for (int i = 0; i < 100; i++) {
            sender.sendMessage("");
        }
    }

    // Creates message that will be displayed on ban
    public static void banPlayerMessage(CommandSender sender, String playerName, Long time, String reason) {
        String message;
        if (time != null) {
            message = ChatColor.RED + sender.getName() + ChatColor.DARK_RED + " suspended - " + playerName + " for " + TimeUtils.millsToString(time) + ChatColor.YELLOW + " Reason: \"" + reason + "\"";
        } else
            message = ChatColor.RED + sender.getName() + ChatColor.DARK_RED + " permanently banned - " + playerName + " Reason: \"" + reason + "\"";
        broadcast(message);
    }

    // Creates message that will be displayed on IP ban
    public static void banIPMessage(CommandSender sender, String ip, Long time, String reason) {
        String message;
        if (time != null)
            message = ChatColor.RED + sender.getName() + ChatColor.DARK_RED + " IP banned - " + ip + " for " + TimeUtils.millsToString(time) + ChatColor.YELLOW + " Reason: \"" + reason + "\"";
        else
            message = ChatColor.RED + sender.getName() + ChatColor.DARK_RED + " permanently IP banned - " + ip + ChatColor.YELLOW + " Reason: \"" + reason + "\"";
        broadcast(message);
    }

    // Creates message that will be displayed on mute
    public static void muteMessage(CommandSender sender, String playerName, Long time, String reason) {
        String message;
        if (time != null)
            message = ChatColor.RED + sender.getName() + ChatColor.DARK_RED + " muted - " + playerName + " for " + TimeUtils.millsToString(time) + ChatColor.YELLOW + " Reason: \"" + reason + "\"";
        else
            message = ChatColor.RED + sender.getName() + ChatColor.DARK_RED + " permanently muted - " + playerName + ChatColor.YELLOW + " Reason: \"" + reason + "\"";
        broadcast(message);
    }

    // Creates message that will be displayed when an IP is released from a ban
    public static void releaseIPMessage(CommandSender sender, String ip) {
        broadcast(ChatColor.DARK_RED + sender.getName() + ChatColor.YELLOW + " has released the ip: " + ChatColor.RED + ip);
    }

    // Creates message that will be displayed when a player is released from a punishment
    public static void releaseMessage(CommandSender sender, String playerName) {
        broadcast(ChatColor.DARK_RED + sender.getName() + ChatColor.YELLOW + " has released player: " + ChatColor.RED + playerName);
    }

    // Broadcasts messages on mute/ban to other admins with permission to see the mute/ban
    public static void broadcast(String message) {
        Bukkit.getConsoleSender().sendMessage(message);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("peacekeeper.release.notify")) {
                p.sendMessage(message);
            }
        }
    }

    public static void playerNotFoundMessage(CommandSender sender, String name) {
        sender.sendMessage(ChatColor.DARK_RED + "Player " + name + " was not found in the database");
    }
}
