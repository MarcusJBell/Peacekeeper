package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.MutablePair;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.FilterUtils;
import javafx.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ChatSpamFilterListener implements Listener {

    //Types: blocking, filtering, off
    public String capType = "blocking", spamType = "blocking", excessiveCharType = "blocking";
    //Percentage threshold for filter to kick in
    public float caps = .90f, spam = .75f;
    //Amount of repeated characters for filter to pick up
    public int excessiveCharCount = 6, spamCount = 10;

    private Peacekeeper peacekeeper;

    private ArrayList<String> categories = new ArrayList<>();

    public ChatSpamFilterListener(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    public boolean allDisabled() {
        return capType.equalsIgnoreCase("off") || capType.equalsIgnoreCase("off") || excessiveCharType.equalsIgnoreCase("off");
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChatEvent(AsyncPlayerChatEvent event) {
        if (allDisabled()) return;
        boolean filtered = false;
        if (!categories.isEmpty()) categories.clear();
        final String originalMessage = event.getMessage();
        if (handleExcessives(event)) filtered = true;
        if (handleCaps(event)) filtered = true;
        if (handleSpam(event)) filtered = true;

        if (filtered && !event.isCancelled()) {
            ChatUtils.autoModerator(event.getPlayer(), ChatColor.YELLOW + "Your message was filtered to prevent spam. If you believe this is a false positive please report it as a bug.");
        } else if (event.isCancelled() && !categories.isEmpty()) {
            broadcastBlocked(event.getPlayer(), originalMessage, categories.toString());
        }
    }

    private boolean handleCaps(AsyncPlayerChatEvent event) {
        if (capType.equalsIgnoreCase("filtering")) {
            return filterCaps(event);
        } else if (capType.equalsIgnoreCase("blocking")) {
            blockCaps(event);
            return false;
        }
        return false;
    }

    private boolean filterCaps(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        int capCount = FilterUtils.upperCaseCount(message.replaceAll("[^a-zA-Z0-9]", ""));
        if (capCount >= spamCount && (float) capCount / (float) message.length() >= caps) {
            message = FilterUtils.filterCaps(message);
            event.setMessage(message);
            return true;
        }
        return false;
    }

    // Blocks message if contains too many caps
    private void blockCaps(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        int capCount = FilterUtils.upperCaseCount(message.replaceAll("[^a-zA-Z0-9]", ""));
        if (capCount >= spamCount && (float) capCount / (float) message.length() >= caps) {
            event.setCancelled(true);
            categories.add("EXCESSIVE CAPS");
        }
    }

    private boolean handleSpam(AsyncPlayerChatEvent event) {
        if (spamType.equalsIgnoreCase("blocking")) {
            blockSpam(event);
        }
        return false;
    }

    private void blockSpam(AsyncPlayerChatEvent event) {

    }

    private boolean handleExcessives(AsyncPlayerChatEvent event) {
        if (excessiveCharType.equalsIgnoreCase("filtering")) {
            return filterExcessives(event);
        } else if (excessiveCharType.equalsIgnoreCase("blocking")) {
            blockExcessives(event);
            return false;
        }
        return false;
    }

    private boolean filterExcessives(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        ArrayList<MutablePair<Character, Integer>> duplicates = FilterUtils.duplicateCharatersCount(message, true);
        boolean filtered = false;
        for (MutablePair<Character, Integer> p : duplicates) {
            if (p.value >= excessiveCharCount) {
                Pattern pattern = Pattern.compile(FilterUtils.charCountToString(p.key, p.value));
                message = pattern.matcher(message).replaceAll(FilterUtils.charCountToString(p.key, 1));
                filtered = true;
            }
        }
        event.setMessage(message);
        return filtered;
    }

    // Blocks messages  if contains excessive chars
    private void blockExcessives(AsyncPlayerChatEvent event) {
        Pair<Character, Integer> p = FilterUtils.highestRepeatedCharacterCount(event.getMessage(), true);
        if (p == null) return;
        if (p.getValue() >= excessiveCharCount) {
            event.setCancelled(true);
            categories.add("Excessive Char(s)");
        }
    }

    public void broadcastBlocked(Player player, String message, String category) {
        String m = ChatColor.DARK_RED + player.getName() + "'s" + ChatColor.RED + " messaged was blocked:\n \"" + message + "\"";
        m += "\n" + ChatColor.DARK_AQUA + "Category: " + ChatColor.AQUA + category;

        player.sendMessage(m);
        Bukkit.getConsoleSender().sendMessage(m);
        Peacekeeper.logFile.logBlockedChat(player.getName(), message);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(player)) continue;
            if (p.hasPermission("peacekeeper.command.mute")) {
                p.sendMessage(m);
            }
        }
    }

}
