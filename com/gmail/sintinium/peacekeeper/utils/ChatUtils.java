package com.gmail.sintinium.peacekeeper.utils;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.SortedMap;

public class ChatUtils {

    public static void sendTellRaw(Peacekeeper peacekeeper, Player player, String message) {
        peacekeeper.jsonChat.tellRawMessage(player, message);
    }

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

    public static void warnMessage(CommandSender sender, String playerName) {
        broadcast(ChatColor.GOLD + sender.getName() + ChatColor.YELLOW + " has warned player: " + ChatColor.RED + playerName);
    }

    // Broadcasts messages on mute/ban to other admins with permission to see the mute/ban
    public static void broadcast(String message) {
        Peacekeeper.logFile.logToFile(ChatColor.stripColor(message));
        Bukkit.getConsoleSender().sendMessage(message);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("peacekeeper.broadcast")) {
                p.sendMessage(message);
            }
        }
    }

    public static void playerNotFoundMessage(CommandSender sender, String name) {
        sender.sendMessage(ChatColor.DARK_RED + "Player " + ChatColor.RED + name.trim() + ChatColor.DARK_RED + " was not found in the database");
        sender.sendMessage(ChatColor.YELLOW + "Tip: " + "Use % to approximate results. Ex: b% is Bob! %bby is Bobby!");
    }

    public static void noPermission(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use this command");
    }

    public static void autoModerator(Player p, String message) {
        p.sendMessage(ChatColor.AQUA + "[" + ChatColor.DARK_AQUA + "Peacekeeper" + ChatColor.AQUA + "]: " + ChatColor.DARK_AQUA + message);
    }

    /**
     * Util to make pages for chat since I can't find any good documentation on bukkit's paginator util
     *
     * @param sender     Who to send page to
     * @param map        Sorted map of page item index and the string for that index
     * @param page       current page number
     * @param pageLength max lines per page
     */
    public static void paginate(CommandSender sender, SortedMap<Integer, String> map, int page, int pageLength, String... endPageMessage) {
        sender.sendMessage(ChatColor.DARK_AQUA + "---- Page: " + page + "/" + (((map.size() - 1) / pageLength) + 1) + " ----");
        int i = 0, k = 0;
        page--;
        for (Map.Entry<Integer, String> e : map.entrySet()) {
            k++;
            if ((((page * pageLength) + i + 1) == k) && (k != ((page * pageLength) + pageLength + 1))) {
                i++;
                sender.sendMessage(ChatColor.DARK_AQUA + " - " + e.getValue());
            }
        }

        if ((page + 1) * pageLength > map.size() - 1) {
            sender.sendMessage(ChatColor.DARK_AQUA + "---- Last page ----");
            return;
        }
        for (String s : endPageMessage) {
            sender.sendMessage(ChatColor.DARK_AQUA + s);
        }
    }
}
