package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.MutablePair;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import com.gmail.sintinium.peacekeeper.utils.FilterUtils;
import javafx.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class ChatSpamFilterListener implements Listener {

    //Types: blocking, filtering, off
    public String capType = "blocking", spamType = "blocking", excessiveCharType = "blocking", specialType = "blocking";
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
    public void onChatEvent(AsyncPlayerChatEvent chatEvent) {
        FilterEvent event = new FilterEvent(chatEvent.getPlayer(), chatEvent.getMessage());
        filterEvent(event);

        if (event.isCancelled()) chatEvent.setCancelled(true);
        chatEvent.setMessage(event.getMessage());
        if (chatEvent.getMessage().isEmpty()) chatEvent.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent commandEvent) {
        String split[] = commandEvent.getMessage().split("\\s+");
        if (split.length <= 0) return;
        String m = split[0].toLowerCase();
        if (m.equalsIgnoreCase("/r") || m.equalsIgnoreCase("/msg") || m.equalsIgnoreCase("/tell") || m.equalsIgnoreCase("/me") || m.equalsIgnoreCase("/say") || m.equalsIgnoreCase("/m") || m.equalsIgnoreCase("/whisper")) {
            FilterEvent event = new FilterEvent(commandEvent.getPlayer(), CommandUtils.argsToReason(commandEvent.getMessage().split(" "), 1));
            filterEvent(event);

//            if (event.getMessage().isEmpty()) commandEvent.setCancelled(true);
            if (event.isCancelled()) commandEvent.setCancelled(true);
            commandEvent.setMessage(m + " " + event.getMessage());
        }
    }

    public void filterEvent(FilterEvent event) {
        if (peacekeeper.conversationListener.conversations.containsKey(event.getPlayer()) || event.getPlayer().hasPermission("peacekeeper.filter.bypass")) return;
        if (allDisabled()) return;
        boolean filtered = false;
        if (!categories.isEmpty()) categories.clear();
        final String originalMessage = event.getMessage();

        if (handleSpecials(event)) filtered = true;
        if (handleCaps(event)) filtered = true;
        if (handleExcessives(event)) filtered = true;
        if (handleSpam(event)) filtered = true;

        if (filtered && !event.isCancelled()) {
            ChatUtils.autoModerator(event.getPlayer(), ChatColor.YELLOW + "Your message was filtered to prevent spam. If you believe this is a false positive please report it as a bug.");
        } else if (event.isCancelled() && !categories.isEmpty()) {
            broadcastBlocked(event.getPlayer(), originalMessage, categories.toString());
        }
    }

    private boolean handleSpecials(FilterEvent event) {
        if (specialType.equalsIgnoreCase("filtering")) {
            return filterSpecial(event);
        } else if (specialType.equalsIgnoreCase("blocking")) {
            blockSpecials(event);
            return false;
        }
        return false;
    }

    private boolean filterSpecial(FilterEvent event) {
        if (FilterUtils.isSpecial(event.getMessage())) {
            Pattern pattern = Pattern.compile("[^~`!@#$%^&*()_+-=\\\\|\\[\\]{};':\"/?.>,<a-zA-Z0-9 ]");
            event.setMessage(pattern.matcher(event.getMessage()).replaceAll(""));
            return true;
        }
        return false;
    }

    private void blockSpecials(FilterEvent event) {
        Pattern pattern = Pattern.compile("[^~`!@#$%^&*()_+-=\\\\|\\[\\]{};':\"/?.>,<a-zA-Z0-9 ]");
        if (pattern.matcher(event.getMessage()).find()) {
            event.setCancelled();
            categories.add("Special char(s)");
        }
    }

    private boolean handleCaps(FilterEvent event) {
        if (capType.equalsIgnoreCase("filtering")) {
            return filterCaps(event);
        } else if (capType.equalsIgnoreCase("blocking")) {
            blockCaps(event);
            return false;
        }
        return false;
    }

    private boolean filterCaps(FilterEvent event) {
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
    private void blockCaps(FilterEvent event) {
        String message = event.getMessage();
        int capCount = FilterUtils.upperCaseCount(message.replaceAll("[^a-zA-Z0-9]", ""));
        if (capCount >= spamCount && (float) capCount / (float) message.length() >= caps) {
            event.setCancelled();
            categories.add("EXCESSIVE CAPS");
        }
    }

    private boolean handleSpam(FilterEvent event) {
        if (spamType.equalsIgnoreCase("blocking")) {
            blockSpam(event);
        }
        return false;
    }

    private void blockSpam(FilterEvent event) {

    }

    private boolean handleExcessives(FilterEvent event) {
        if (excessiveCharType.equalsIgnoreCase("filtering")) {
            return filterExcessives(event);
        } else if (excessiveCharType.equalsIgnoreCase("blocking")) {
            blockExcessives(event);
            return false;
        }
        return false;
    }

    private boolean filterExcessives(FilterEvent event) {
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
    private void blockExcessives(FilterEvent event) {
        Pair<Character, Integer> p = FilterUtils.highestRepeatedCharacterCount(event.getMessage(), true);
        if (p == null) return;
        if (p.getValue() >= excessiveCharCount) {
            event.setCancelled();
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
            if (p.hasPermission("peacekeeper.filter.broadcast")) {
                p.sendMessage(m);
            }
        }
    }

    public class FilterEvent {

        Player player;
        boolean cancelled = false;
        String message;

        public FilterEvent(Player player, String message) {
            this.player = player;
            this.message = message;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled() {
            this.cancelled = true;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Player getPlayer() {
            return player;
        }

    }

}
