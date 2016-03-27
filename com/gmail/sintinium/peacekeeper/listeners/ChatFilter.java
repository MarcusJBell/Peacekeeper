package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerBanTable;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ArrayHelper;
import com.gmail.sintinium.peacekeeper.utils.BanUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class ChatFilter implements Listener {

    private Peacekeeper peacekeeper;
    private Set<String> tempSentence;

    public ChatFilter(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        tempSentence = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatMessage(AsyncPlayerChatEvent event) {
        long time = System.currentTimeMillis();
        if (checkFilter(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
            broadcastFilter(event.getPlayer(), event.getMessage());
        } else {
            for (Map.Entry<String, String> e : peacekeeper.chatFilter.replacedWords.entrySet()) {
                if (event.getMessage().contains(e.getKey())) {
                    event.setMessage(event.getMessage().replace(e.getKey(), e.getValue()));
                }
            }
        }
        Bukkit.getConsoleSender().sendMessage(System.currentTimeMillis() - time + "");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandProcess(PlayerCommandPreprocessEvent event) {
        String split[] = event.getMessage().split("\\s+");
        if (split.length <= 0) return;
        String m = split[0].toLowerCase();
        if (m.equalsIgnoreCase("/r") || m.equalsIgnoreCase("/msg") || m.equalsIgnoreCase("/tell") || m.equalsIgnoreCase("/me") || m.equalsIgnoreCase("/say") || m.equalsIgnoreCase("/afk") || m.equalsIgnoreCase("/m") || m.equalsIgnoreCase("/whisper")) {
            if (checkFilter(event.getPlayer(), event.getMessage())) {
                event.setCancelled(true);
                broadcastFilter(event.getPlayer(), event.getMessage());
            }
        }
    }

    private boolean checkFilter(Player player, String message) {
//        final String originalMessage = message;
        if (CommandUtils.containsNumber(message, 6) || message.contains("server") || message.contains("craft") || message.contains(".ws") || message.contains(".no")) {
            if (checkStrictIP(player, message)) {
                return true;
            }
        } else {
            for (Map.Entry<String, Long> e : peacekeeper.chatFilter.strict.entrySet()) {
                if (message.contains(e.getKey())) {
                    handleBan(player, e.getValue());
                    return true;
                }
            }
        }

        String clipped = message;
        char last = clipped.charAt(clipped.length() - 1);
        if (last == '.' || last == ',' || last == '!' || last == '?') {
            clipped = clipped.substring(0, clipped.length() - 1);
        }
        Bukkit.getConsoleSender().sendMessage(clipped);

        Pattern pattern = Pattern.compile(CommandUtils.CONTAINS_SPECIAL_CHAR);
        String wildcarded = pattern.matcher(ChatColor.stripColor(clipped)).replaceAll("[A-Za-z0-9]");
        String flatWildcard = wildcarded.replaceAll("\\s+", "");
//        String wildcarded = clipped.replace(, "/w");

        for (String s : peacekeeper.chatFilter.blockedWords) {
            if (s.matches("\\b(?i)" + flatWildcard + "\\b") || message.contains(s)) {
                return true;
            }
        }

//        for (final String s : peacekeeper.chatFilter.semiblocked) {
//            for (String e : peacekeeper.chatFilter.exceptions) {
//                if (!CommandUtils.matchAll(e, flatWildcard) && CommandUtils.matchAll(s, flatWildcard)) {
//                    return true;
//                }
//            }
//        }

        String[] split = wildcarded.split(" ");
        for (String sp : split) {

            for (final String s : peacekeeper.chatFilter.wholeOnly) {
                if (s.equalsIgnoreCase(sp)) {
                    return true;
                }
            }

            for (final String s : peacekeeper.chatFilter.blockedWords) {
                if (CommandUtils.matchAll(s, sp)) {
                    return true;
                }
            }

            for (final String s : peacekeeper.chatFilter.semiblocked) {
                for (String e : peacekeeper.chatFilter.exceptions) {
                    if (!CommandUtils.matchAll(e, sp) && CommandUtils.matchAll(s, sp)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    private boolean checkStrictIP(Player player, String message) {
        if (CommandUtils.isIP(message)) {
            if (messageContainsBlockedIP(player, message)) {
                return true;
            }
        } else {
            if (advancedIPChecker(player, message)) {
                return true;
            }
        }
        return false;
    }

    private boolean messageContainsBlockedIP(Player player, String message) {
        for (Map.Entry<String, Long> e : peacekeeper.chatFilter.strict.entrySet()) {
            if (e.getKey().contains(message)) {
                handleBan(player, e.getValue());
                return true;
            }
        }
        return advancedIPChecker(player, message);
    }

    private boolean advancedIPChecker(Player player, String message) {
        for (Map.Entry<Character[], Long> e : peacekeeper.chatFilter.strictChars.entrySet()) {
            char[] c = removeChar(ArrayHelper.covertCharArray(e.getKey()), '.');

            for (char ee : c) {
                Bukkit.getConsoleSender().sendMessage(ee + "");
            }

            int index = 0;
            for (char sc : message.toCharArray()) {
                if (sc == c[index]) {
                    index++;
                }
            }

            if ((double) (index) / (double) (12) >= .75d) {
                handleBan(player, e.getValue());
                return true;
            }
        }
        return false;
    }

    private void handleBan(final Player player, final long length) {
        Bukkit.getConsoleSender().sendMessage(length + "");
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                int playerID = peacekeeper.userTable.getPlayerIDFromUUID(player.getUniqueId().toString());
                Long finalLength = length == -999 ? null : length;
                int recordID = peacekeeper.recordTable.addRecord(playerID, null, null, PlayerRecordTable.BAN, finalLength, "Advertising", null);
                BanData banData = new BanData(null, System.currentTimeMillis(), playerID, null, "Advertising", null, finalLength, PlayerBanTable
                        .PLAYER, recordID);
                peacekeeper.banTable.banUser(playerID, banData);
                final String message = BanUtils.generateBanMessage(peacekeeper, banData);
                Bukkit.getScheduler().scheduleSyncDelayedTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        player.kickPlayer(message);
                    }
                });
            }
        });
    }

    private void broadcastFilter(Player player, String message) {
        String m = ChatColor.DARK_RED + player.getName() + ChatColor.RED + " used blocked word(s): " + message;
        player.sendMessage(m);
        Bukkit.getConsoleSender().sendMessage(m);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;
            if (p.hasPermission("peacekeeper.command.mute")) {
                p.sendMessage(m);
            }
        }
    }

    private char[] removeChar(char[] chars, char remove) {
        return ArrayUtils.removeElement(chars, remove);
    }

}
