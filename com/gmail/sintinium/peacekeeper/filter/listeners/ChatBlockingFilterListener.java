package com.gmail.sintinium.peacekeeper.filter.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerBanTable;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.*;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;
import java.util.regex.Pattern;

public class ChatBlockingFilterListener implements Listener {

    public boolean filterChat = true, filterCommands = true, filterBook = true, filterSign = true, filterItems = true;
    public int leniency = 1;
    private Peacekeeper peacekeeper;

    private Set<String> blockedWords;
    private Set<String> foundBlocked;


    public ChatBlockingFilterListener(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        blockedWords = new HashSet<>();
        foundBlocked = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChatMessage(AsyncPlayerChatEvent event) {
        if (!filterChat) return;
        if (event.getPlayer().hasPermission("peacekeeper.filter.bypass")) return;

        if (checkFilter(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
            broadcastFilter(event.getPlayer(), event.getMessage(), 0);
        } else {
            for (Map.Entry<String, String> e : peacekeeper.chatFilter.replacedWords.entrySet()) {
                if (event.getMessage().contains(e.getKey())) {
                    event.setMessage(event.getMessage().replace(e.getKey(), e.getValue()));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommandProcess(PlayerCommandPreprocessEvent event) {
        if (!filterCommands || event.getMessage().isEmpty()) return;
        if (event.getPlayer().hasPermission("peacekeeper.filter.bypass")) return;
        String split[] = event.getMessage().trim().split("\\s+");
        if (split.length <= 0) return;
        String m = split[0].toLowerCase();
//        if (m.equalsIgnoreCase("/r") || m.equalsIgnoreCase("/msg") || m.equalsIgnoreCase("/tell") || m.equalsIgnoreCase("/me") || m.equalsIgnoreCase("/say") || m.equalsIgnoreCase("/m") || m.equalsIgnoreCase("/whisper")) {
        if (peacekeeper.chatFilter.checkedCommands.contains(m.replaceAll("/", ""))) {
            if (checkFilter(event.getPlayer(), event.getMessage())) {
                event.setCancelled(true);
                broadcastFilter(event.getPlayer(), event.getMessage(), 1);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBookDropEvent(PlayerDropItemEvent event) {
        if (!filterBook) return;
        BookMeta bookMeta;
        if (event.getItemDrop().getItemStack().getType() == Material.BOOK_AND_QUILL || event.getItemDrop().getItemStack().getType() == Material.WRITTEN_BOOK) {
            bookMeta = (BookMeta) event.getItemDrop().getItemStack().getItemMeta();
        } else {
            return;
        }

        if (handleBook(event.getPlayer(), bookMeta)) {
            event.setCancelled(true);
            event.getPlayer().getInventory().remove(event.getItemDrop().getItemStack().getType());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBookMove(InventoryClickEvent event) {
        if (!filterBook) return;
        if (!(event.getWhoClicked() instanceof Player) || event.getCurrentItem() == null) return;
        BookMeta bookMeta;
        if (event.getCurrentItem().getType() == Material.BOOK_AND_QUILL || event.getCurrentItem().getType() == Material.WRITTEN_BOOK) {
            bookMeta = (BookMeta) event.getCurrentItem().getItemMeta();
        } else {
            return;
        }

        if (handleBook((Player) event.getWhoClicked(), bookMeta)) {
            event.setCancelled(true);
            event.getWhoClicked().getInventory().remove(event.getCurrentItem());
        }
    }

    private boolean handleBook(Player player, BookMeta bookMeta) {
        String book;
        StringBuilder builder = new StringBuilder();
        for (String s : bookMeta.getPages()) {
            builder.append(s).append(" ");
        }
        book = ChatColor.stripColor(builder.toString());
        Peacekeeper.logFile.logBook(player.getName(), book);

        if (checkFilter(player, book, true)) {
            broadcastFilter(player, book, 2);
            return true;
        }
        return false;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSignEdit(SignChangeEvent event) {
        if (!filterSign) return;
        if (event.getPlayer().hasPermission("peacekeeper.filter.bypass")) return;

        String sign;
        StringBuilder builder = new StringBuilder();
        for (String s : event.getLines()) {
            builder.append(s).append(" ");
        }
        sign = builder.toString();
        Peacekeeper.logFile.logSign(event.getPlayer().getName(), sign);
        if (checkFilter(event.getPlayer(), sign)) {
            event.setCancelled(true);
            broadcastFilter(event.getPlayer(), sign, 3);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onRename(InventoryClickEvent event) {
        if (!filterItems) return;
        if (event.getWhoClicked().hasPermission("peacekeeper.filter.bypass")) return;

        if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.ANVIL) {
            if (event.getRawSlot() == 2) {
                Player p = Bukkit.getPlayer(event.getWhoClicked().getUniqueId());
                if (p == null || event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR || event.getCurrentItem().getItemMeta() == null || event.getCurrentItem().getItemMeta().getDisplayName() == null)
                    return;
                if (checkFilter(p, event.getCurrentItem().getItemMeta().getDisplayName())) {
                    event.setCancelled(true);
                    broadcastFilter(p, event.getCurrentItem().getItemMeta().getDisplayName(), 4);
                }
            }
        }
    }

    private boolean checkFilter(Player player, String message) {
        return checkFilter(player, message, false);
    }

    private boolean checkFilter(final Player player, String message, boolean book) {
        if (peacekeeper.muteTable.mutedPlayers.containsKey(player.getUniqueId())) {
            return false;
        }
        blockedWords.clear();
        foundBlocked.clear();

        String realMessage = message;
        message = ChatColor.stripColor(message.toLowerCase()).replaceAll("\"", "");
        boolean blocked = false;
//        final String originalMessage = message;
        if (CommandUtils.containsNumber(message, 6) || message.contains("server") || message.contains("craft") || message.contains(".ws") || message.contains(".no")) {
            if (checkStrictIP(player, message, book)) {
                blocked = true;
            }
        } else {
            for (Map.Entry<String, Long> e : peacekeeper.chatFilter.strict.entrySet()) {
                if (message.contains(e.getKey())) {
                    handleBan(player, e.getValue());
                    blocked = true;
                }
            }
        }

        Pattern pattern = Pattern.compile("[^a-zA-Z0-9]");
        if (pattern.matcher(message).matches()) {
            return false;
        }

        String clipped = message;
        for (int i = clipped.length() - 1; i > 0; i--) {
            char last = clipped.charAt(i);
            if (last != '\\' && !Character.isAlphabetic(last) && !Character.isDigit(last)) {
                clipped = clipped.substring(0, i);
            } else {
                break;
            }
        }

        String wildcarded = clipped;
        String flatWildcard = wildcarded.trim().replaceAll(" +", " ");
        char[] flatWildcardArray = flatWildcard.toCharArray();
        List<Character> flatChars = new ArrayList<>();

        List<Integer> toRemoveBefore = new ArrayList<>();
        List<Integer> toRemoveAfter = new ArrayList<>();
        // Actually flatting the wildcard
        for (int i = 0; i < flatWildcardArray.length; i++) {
            char c = flatWildcardArray[i];
            flatChars.add(c);
            if (c == ' ') {
                continue;
            }
            int beforeCount = 0;
            int afterCount = 0;
            //Before
            for (int b = i - 1; b > 0; b--) {
                if (i == 0) break;
                char bc = flatWildcardArray[b];
                if (bc == ' ') break;
                else beforeCount++;
            }
            //After
            for (int b = i + 1; b < flatWildcardArray.length; b++) {
                char bc = flatWildcardArray[b];
                if (bc == ' ') break;
                else afterCount++;
            }

            if (beforeCount < leniency && afterCount < leniency) {
                if (i != 0)
                    toRemoveBefore.add(i);
//                    flatChars = FilterUtils.removeLastSpace(flatChars, i);
                if (i < flatWildcardArray.length)
                    toRemoveAfter.add(i);
            }

        }

//        String test = "h e l l o";
//        List<Character> cs = new ArrayList<>();
//        for (char c : test.toCharArray()) {
//            cs.add(c);
//        }
//        cs = FilterUtils.removeLastSpace(cs, 4);
//        cs = FilterUtils.removeLastSpace(cs, 2);
//        cs = FilterUtils.removeLastSpace(cs, 4);
//        cs = FilterUtils.removeLastSpace(cs, 5);
//        StringBuilder bu = new StringBuilder();
//        for (Character c : cs) {
//            bu.append(c);
//        }
//        Bukkit.getConsoleSender().sendMessage(bu.toString());
        List<Integer> indices = FilterUtils.getLastSpaceIndices(flatChars, toRemoveBefore);
        indices.addAll(FilterUtils.getNextSpaceIndices(flatChars, toRemoveAfter));
        flatChars = ArrayHelper.removeAllIndices(flatChars, indices);
        StringBuilder builder = new StringBuilder();
        for (Character c : flatChars) {
            builder.append(c);
        }
        flatWildcard = builder.toString();
//        String wildcarded = clipped.replace(, "/w");

//        for (final String s : peacekeeper.chatFilter.semiblocked) {
//            for (String e : peacekeeper.chatFilter.exceptions) {
//                if (!CommandUtils.matchAll(e, flatWildcard) && CommandUtils.matchAll(s, flatWildcard)) {
//                    return true;
//                }
//            }
//        }

        for (String sp : realMessage.split("\\s+")) {
            for (final String s : peacekeeper.chatFilter.wholeOnly) {
                if (sp.equalsIgnoreCase(s)) {
                    blocked = true;
                    blockedWords.add(sp);
                }
            }
        }

        String[] split = wildcarded.split("\\s+");
        int index = 0;
        for (String sp : split) {
            final String realSp = sp;
            String noSymbol = sp.replaceAll("[^A-Za-z0-9]", "");

            Pattern p = Pattern.compile("[^A-Za-z0-9*!#$? ]");
            sp = p.matcher(sp).replaceAll("");

            pattern = Pattern.compile(CommandUtils.CONTAINS_SPECIAL_CHAR_NOBACK);
            sp = pattern.matcher(sp).replaceFirst("[A-Za-z0-9]");

            for (final String s : peacekeeper.chatFilter.blockedWords) {
                if (CommandUtils.matchAll(s, sp) || CommandUtils.matchAll(s, noSymbol)) {
                    blocked = true;
                    blockedWords.add(realSp);
                }
            }

            for (final String s : peacekeeper.chatFilter.semiblocked) {
                for (String e : peacekeeper.chatFilter.exceptions) {
                    if (!CommandUtils.matchAll(e, sp) && CommandUtils.matchAll(s, sp)) {
                        blocked = true;
                        blockedWords.add(realSp);
                    }
                }
            }
            index++;
        }

        for (String s : peacekeeper.chatFilter.blockedWords) {
            if (blockedWords.contains(s)) continue;
            if (flatWildcard.contains(s) || s.matches("\\b(?i)" + flatWildcard.replaceAll("[^A-Za-z0-9]", "") + "\\b")) {
                blocked = true;
                foundBlocked.add(s);
            }
        }

        if (blocked) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(peacekeeper, new Runnable() {
                @Override
                public void run() {
                    peacekeeper.filterManager.addBlocked(player);
                }
            }, 10L);
        }
        return blocked;
    }

    private boolean checkStrictIP(Player player, String message, boolean book) {
        if (CommandUtils.isIP(message) || CommandUtils.isIP(message.replaceAll("\\s+", ""))) {
            if (messageContainsBlockedIP(player, message) || messageContainsBlockedIP(player, message.replaceAll("\\s+", ""))) {
                return true;
            }
        } else if (!book) {
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

            int index = 0;
            for (char sc : message.toCharArray()) {
                if (sc == c[index]) {
                    index++;
                }
            }

            if ((double) (index) / (double) (e.getKey().length) >= .75d) {
                handleBan(player, e.getValue());
                return true;
            }
        }
        return false;
    }

    private void handleBan(final Player player, final long length) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(peacekeeper, new Runnable() {
            @Override
            public void run() {
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
                        ChatUtils.banPlayerMessage(Bukkit.getConsoleSender(), player.getName(), banData.banLength, banData.reason);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(peacekeeper, new Runnable() {
                            @Override
                            public void run() {
                                player.kickPlayer(message);
                            }
                        });
                    }
                });
            }
        }, 2L);
    }

    // 0 = chat, 1 = command, 2 = book, 3 = sign, 4 = item
    private void broadcastFilter(Player player, String message, int type) {
        Iterator<String> it = blockedWords.iterator();
        while (it.hasNext()) {
            String s = it.next();
            Pattern pat = Pattern.compile("(?i)" + Pattern.quote(s));
            message = pat.matcher(message).replaceAll(ChatColor.UNDERLINE + "" + ChatColor.BOLD + s + ChatColor.RED);
        }

        String m = ChatColor.DARK_RED + player.getName() + ChatColor.RED + " used blocked word(s):\n \"" + message + "\"";

        if (!foundBlocked.isEmpty()) {
            m += "\n" + ChatColor.DARK_RED + "Found similar: " + ChatColor.RED + foundBlocked.toString();
        }

        String typeMessage;
        switch (type) {
            case 0:
                typeMessage = "Chat";
                break;
            case 1:
                typeMessage = "Command";
                break;
            case 2:
                typeMessage = "Book";
                break;
            case 3:
                typeMessage = "Sign";
                break;
            case 4:
                typeMessage = "Item";
                break;
            default:
                typeMessage = "ERROR";
        }
        m += "\n" + ChatColor.DARK_AQUA + "Type: " + ChatColor.AQUA + typeMessage;
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

    private char[] removeChar(char[] chars, char remove) {
        return ArrayUtils.removeElement(chars, remove);
    }

}
