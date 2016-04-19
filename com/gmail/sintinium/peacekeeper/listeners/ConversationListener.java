package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.commands.MuteCommand;
import com.gmail.sintinium.peacekeeper.commands.SuspendCommand;
import com.gmail.sintinium.peacekeeper.data.conversation.ConversationData;
import com.gmail.sintinium.peacekeeper.data.conversation.MuteConversationData;
import com.gmail.sintinium.peacekeeper.data.conversation.ReportConversationData;
import com.gmail.sintinium.peacekeeper.data.conversation.SuspendConversationData;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class ConversationListener implements Listener {

    public static final String cancel = "[\"\",{\"text\":\"[CANCEL] \",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"CANCELCONVERSATION\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to cancel\"}]}}}]";
    public static final String cancelFinish = "[\"\",{\"text\":\"[CANCEL] \",\"color\":\"red\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"CANCELCONVERSATION\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to cancel\"}]}}},{\"text\":\"[SUBMIT]\",\"color\":\"green\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"FINISHEDCONVERSATION\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to submit\"}]}}}]";
    private static final int pageCount = 8;
    public Map<Player, ConversationData> conversations;
    private Peacekeeper peacekeeper;

    public ConversationListener(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        conversations = new HashMap<>();
    }

    // Removes the player from the conversation and sends missed messages.
    public void removeConversation(final Player sender, final boolean cancelled) {
        final ConversationData data = peacekeeper.conversationListener.conversations.get(sender);
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                ChatUtils.clearChat(sender);
                if (!cancelled) {
                    if (data instanceof SuspendConversationData) {
                        onSuspendChatFinish(sender);
                    } else if (data instanceof MuteConversationData) {
                        onMuteChatFinish(sender);
                    } else if (data instanceof ReportConversationData) {
                        onReportFinish(sender);
                    }
                }
            }
        });
    }

    public void confirmOrCancel(final Player sender) {
        final ConversationData data = peacekeeper.conversationListener.conversations.get(sender);
        Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
            @Override
            public void run() {
                List<String> missedChat = data.missedMessages;
                peacekeeper.conversationListener.conversations.remove(sender);

                if (missedChat.isEmpty()) {
                    sender.sendMessage("You missed no chat messages");
                } else {
                    sender.sendMessage(ChatColor.DARK_AQUA + "--- Missed Chat ---");
                    for (String message : missedChat) {
                        sender.sendMessage(message);
                    }
                }
            }
        });
    }

    public void syncCancel(Player player) {
        final ConversationData data = peacekeeper.conversationListener.conversations.get(player);
        List<String> missedChat = data.missedMessages;
        peacekeeper.conversationListener.conversations.remove(player);
        ChatUtils.clearChat(player);

        if (missedChat.isEmpty()) {
            player.sendMessage("Cancelled, you missed no chat messages");
        } else {
            player.sendMessage(ChatColor.DARK_AQUA + "--- Missed Chat ---");
            for (String message : missedChat) {
                player.sendMessage(message);
            }
        }
    }

    // Handles conversations
    @EventHandler(priority = EventPriority.HIGH)
    public void playerChat(AsyncPlayerChatEvent event) {
        // If the sender is in a conversation cancel it and handle accordingly
        if (conversations.containsKey(event.getPlayer())) {
            event.setCancelled(true);
            if (event.getMessage().equals("CANCELCONVERSATION")) {
                removeConversation(event.getPlayer(), true);
                return;
            } else if (event.getMessage().equals("FINISHEDCONVERSATION")) {
                removeConversation(event.getPlayer(), false);
                return;
            } else if (event.getMessage().startsWith("page")) {
                String[] args = event.getMessage().split(" ");
                if (args.length != 2) {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Syntax: 'Page <number>'");
                    return;
                } else if (!StringUtils.isNumeric(args[1])) {
                    event.getPlayer().sendMessage(ChatColor.YELLOW + "Page must be a number.");
                    return;
                }
                conversations.get(event.getPlayer()).page = Integer.parseInt(args[1]);
                sendConversationInstructions(event.getPlayer());
                return;
            }
            ConversationData data = conversations.get(event.getPlayer());
            if (data instanceof MuteConversationData) handleMuteChat(event);
            else if (data instanceof SuspendConversationData) handleSuspendChat(event);
            else if (data instanceof ReportConversationData) handleReportChat(event);
        }
        // If someone is in a conversation hide the message from them
        else if (!conversations.isEmpty()) {
            for (Map.Entry pair : conversations.entrySet()) {
                Player p = (Player) pair.getKey();
                event.getRecipients().remove(p);
            }
        }
    }

    // Monitors chat and stores messages that a player might miss if they're suspending/muting a player
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void monitorPlayerChat(AsyncPlayerChatEvent event) {
        if (!conversations.isEmpty()) {
            for (Map.Entry pair : conversations.entrySet()) {
                ConversationData data = (ConversationData) pair.getValue();
                data.missedMessages.add(event.getPlayer().getDisplayName() + ChatColor.RESET + ": " + event.getMessage());
            }
        }
    }

    public void handleReportChat(final AsyncPlayerChatEvent event) {
        final ReportConversationData data = (ReportConversationData) conversations.get(event.getPlayer());
        if (event.getMessage().startsWith("SELECTCATEGORY")) {
            Integer select = splitSelect(event.getMessage());
            if (data.timeResults.contains(data.results.get(select - 1))) {
                data.timeResults.remove(data.results.get(select - 1));
            } else {
                data.timeResults.add(data.results.get(select - 1));
            }
            sendConversationInstructions(event.getPlayer());
            return;
        }

        String finalMessage = data.getFinalMessage();
        if (finalMessage.length() + event.getMessage().length() <= 500)
            data.addMessage(event.getMessage());
        sendConversationInstructions(event.getPlayer());
        if (data.getFinalMessage().length() + event.getMessage().length() >= 500) {
            event.getPlayer().sendMessage(ChatColor.RED + "MAX LENGTH ALLOWED!");
        }
    }

    // If the person is in a mute conversation handle it here
    public void handleMuteChat(final AsyncPlayerChatEvent event) {
        if (!event.getMessage().startsWith("SELECTCATEGORY")) {
            sendConversationInstructions(event.getPlayer());
            return;
        }
        final Integer select = splitSelect(event.getMessage());
        if (select == null) {
            sendConversationInstructions(event.getPlayer());
            return;
        }
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                final ConversationData data = conversations.get(event.getPlayer());

                if (data.timeResults.contains(data.results.get(select - 1))) {
//                    data.finalTime -= TimeUtils.stringToMillis(data.results.get(select - 1).length);
                    data.timeResults.remove(data.results.get(select - 1));
                } else {
//                    data.finalTime += TimeUtils.stringToMillis(data.results.get(select - 1).length);
                    data.timeResults.add(data.results.get(select - 1));
                }
                sendConversationInstructions(event.getPlayer());
            }
        });
    }

    // If the person is in a suspend conversation handle it here
    public void handleSuspendChat(final AsyncPlayerChatEvent event) {
        if (!event.getMessage().startsWith("SELECTCATEGORY")) {
            sendConversationInstructions(event.getPlayer());
            return;
        }
        Integer select = splitSelect(event.getMessage());
        if (select == null) {
            sendConversationInstructions(event.getPlayer());
            return;
        }

        final Integer stockID = select;
        final ConversationData data = conversations.get(event.getPlayer());

        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                if (data.timeResults.contains(data.results.get(stockID - 1))) {
//                    data.finalTime -= TimeUtils.stringToMillis(data.results.get(stockID - 1).length);
                    data.timeResults.remove(data.results.get(stockID - 1));
                } else {
//                    data.finalTime += TimeUtils.stringToMillis(data.results.get(stockID - 1).length);
                    data.timeResults.add(data.results.get(stockID - 1));
                }
                sendConversationInstructions(event.getPlayer());
            }
        });
    }

    public void onReportFinish(final Player player) {
        final ReportConversationData data = (ReportConversationData) conversations.get(player);
        peacekeeper.commandManager.reportCommand.submitReport(player, data.getFinalMessage(), categoriesToString(data), data.reportingUsers);
        player.sendMessage(ChatColor.GREEN + "Thank you for your report.");
    }

    public void onMuteChatFinish(final Player sender) {
        final ConversationData data = conversations.get(sender);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                MuteCommand.muteUser(sender, peacekeeper, data.punishedUUID, data.punishedUsername, data.playerID, calcTime(data.timeResults), data.reason, categoriesToString(data), (MuteConversationData) data);

                String categoryString = categoriesToString(data);
                if (data.timeResults.size() == 1)
                    ChatUtils.broadcast(ChatColor.DARK_AQUA + "Mute category: " + ChatColor.AQUA + categoryString);
                else
                    ChatUtils.broadcast(ChatColor.DARK_AQUA + "Mute categories: " + ChatColor.AQUA + categoryString);
            }
        };
        sendConfirm(sender, r, shouldWarn(data), data.punishedUsername, ChatColor.DARK_AQUA + "Player: " + ChatColor.AQUA + data.punishedUsername, ChatColor.DARK_AQUA + "Reason: " + ChatColor.AQUA + data.reason, ChatColor.DARK_AQUA + "Categories: " + ChatColor.AQUA + categoriesToString(data));
    }

    public void onSuspendChatFinish(final Player sender) {
        final ConversationData data = conversations.get(sender);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                SuspendCommand.suspendUser(peacekeeper, sender, data.playerID, data.punishedUsername, calcTime(data.timeResults), data.reason, categoriesToString(data), (SuspendConversationData) data);

                String categoryString = categoriesToString(data);
                if (data.timeResults.size() == 1)
                    ChatUtils.broadcast(ChatColor.DARK_AQUA + "Suspend category: " + ChatColor.AQUA + categoryString);
                else
                    ChatUtils.broadcast(ChatColor.DARK_AQUA + "Suspend categories: " + ChatColor.AQUA + categoryString);
            }
        };
        sendConfirm(sender, r, shouldWarn(data), data.punishedUsername, ChatColor.DARK_AQUA + "Player: " + ChatColor.AQUA + data.punishedUsername, ChatColor.DARK_AQUA + "Reason: " + ChatColor.AQUA + data.reason, ChatColor.DARK_AQUA + "Categories: " + ChatColor.AQUA + categoriesToString(data));
    }

    private void sendConfirm(Player sender, Runnable action, boolean shouldWarn, String punishing, String... message) {
        sender.sendMessage(ChatColor.YELLOW + "Please confirm the following...");
        for (String s : message) {
            sender.sendMessage(s);
        }
        if (shouldWarn) {
            sender.sendMessage(ChatColor.GOLD + "NOTE: These categories should be warned before punishing via /warn.");
            sender.sendMessage(ChatColor.DARK_RED + "-- Failure to warn could result in staff discipline if necessary");
            sender.sendMessage(ChatColor.DARK_RED + "-- Be sure to do " + ChatColor.AQUA + "/rec p " + punishing + ChatColor.DARK_RED + " to check their records for warnings!");
        }
        sender.sendMessage(ChatColor.DARK_AQUA + "Type '/pk confirm' if the information above is accurate");
        sender.sendMessage(ChatColor.DARK_AQUA + "Type '/pk cancel' to cancel punishment");
        peacekeeper.commandManager.peacekeeperCommand.confirmDatas.put(sender, action);
    }

    // Messages the player what they need to type to continue on with the conversation
    public void sendConversationInstructions(Player player) {
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
        player.sendMessage("");
        ConversationData data = conversations.get(player);
        player.sendMessage(data.header);
        player.sendMessage(ChatColor.YELLOW + "Click all that apply.");

        int adjustedPageCount = pageCount;
        if (data instanceof ReportConversationData) {
            adjustedPageCount = 5;
        }

        for (int i = data.page * adjustedPageCount; i < (data.page * adjustedPageCount) + adjustedPageCount && i < data.results.size(); i++) {
            TimeManager.TimeResult r = data.results.get(i);
            ChatUtils.sendTellRaw(peacekeeper, player, "[\"\",{\"text\":\"" + String.valueOf(i + 1) + ". " + r.description + "\",\"color\":\"dark_aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + ("SELECTCATEGORY " + (i + 1)) + "\"}}]");
        }

        // Add buttons and alert the player what is needed.
        if (data instanceof ReportConversationData) {
            if (data.timeResults.isEmpty()) {
                player.sendMessage(ChatColor.RED + "**Please select at least one category.");
            } else {
                player.sendMessage(ChatColor.YELLOW + "Selected categories: " + categoriesToString(data));
            }
            if (((ReportConversationData) data).messages.size() <= 0) {
                player.sendMessage(ChatColor.RED + "**Please include a brief description of the issue.");
            } else {
                player.sendMessage("Current message-");
                String message = ((ReportConversationData) data).getFinalMessage();
                player.sendMessage(message);
                player.sendMessage(message.length() + "/" + "500 characters left");
            }
            if (!data.timeResults.isEmpty() && ((ReportConversationData) data).messages.size() > 0) {
                ChatUtils.sendTellRaw(peacekeeper, player, cancelFinish);
            } else {
                ChatUtils.sendTellRaw(peacekeeper, player, cancel);
            }
        } else {
            if (!data.timeResults.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "Selected categories: " + categoriesToString(data));

                ChatUtils.sendTellRaw(peacekeeper, player, cancelFinish);
            } else {
                ChatUtils.sendTellRaw(peacekeeper, player, cancel);
            }
        }

        if ((data.results.size() - 1) / adjustedPageCount != 0) {
            String pageInfo = " " + (data.page + 1) + "/" + (((data.results.size() - 1) / adjustedPageCount) + 1) + " ";
            if (data.page == 0) {
                String message = "[\"\",{\"text\":\"" + pageInfo + "\",\"color\":\"aqua\"},{\"text\":\"[>]\",\"color\":\"yellow\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "page " + (data.page + 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Next Page\"}]}}}]";
                ChatUtils.sendTellRaw(peacekeeper, player, message);
            } else if (data.page >= (data.results.size() - 1) / adjustedPageCount) {
                String message = "[\"\",{\"text\":\"[<]\",\"color\":\"yellow\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "page " + (data.page - 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Previous Page\"}]}}},{\"text\":\"" + pageInfo + "\",\"color\":\"aqua\",\"bold\":false}]";
                ChatUtils.sendTellRaw(peacekeeper, player, message);
            } else {
                String message = "[\"\",{\"text\":\"[<]\",\"color\":\"yellow\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "page " + (data.page - 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Previous Page\"}]}}},{\"text\":\"" + pageInfo + "\",\"color\":\"aqua\",\"bold\":false},{\"text\":\"[>]\",\"color\":\"yellow\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "page " + (data.page + 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Next Page\"}]}}}]";
                ChatUtils.sendTellRaw(peacekeeper, player, message);
            }
        }
    }

    // If a player quits remove them from the conversation to prevent any memory leaks
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        peacekeeper.commandManager.peacekeeperCommand.confirmDatas.remove(event.getPlayer());
        conversations.remove(event.getPlayer());
    }

    public String categoriesToString(ConversationData conversationData) {
        String categoryString = null;
        for (TimeManager.TimeResult r : conversationData.timeResults) {
            if (categoryString == null) {
                categoryString = r.description;
                continue;
            }
            categoryString += ", " + r.description;
        }
        return categoryString;
    }

    public boolean shouldWarn(ConversationData data) {
        for (TimeManager.TimeResult r : data.timeResults) {
            if (r.shouldWarn) return true;
        }
        return false;
    }

    public Integer splitSelect(String message) {
        String[] split = message.split(" ");
        if (split.length == 0) {
            return null;
        }
        if (!StringUtils.isNumeric(split[1])) {
            return null;
        }
        return Integer.parseInt(split[1]);
    }

    public long calcTime(Set<TimeManager.TimeResult> timeResults) {
        List<TimeManager.TimeResult> result = new ArrayList<>(timeResults);
        Collections.sort(result, new Comparator<TimeManager.TimeResult>() {
            @Override
            public int compare(TimeManager.TimeResult o1, TimeManager.TimeResult o2) {
                return Long.compare(o1.timeLength, o2.timeLength);
            }
        });
        Collections.reverse(result);
        long finalTime = 0;
        int index = 0;
        for (TimeManager.TimeResult r : timeResults) {
            if (index > 2) break;
            if (index == 0) {
                finalTime += r.timeLength;
            } else {
                finalTime += r.timeLength / (index * 2);
            }
            index++;
        }
        return finalTime;
    }

    public enum ConversationType {
        WARN, KICK, MUTE, SUSPEND, BAN, IPBAN, REPORT
    }

}
