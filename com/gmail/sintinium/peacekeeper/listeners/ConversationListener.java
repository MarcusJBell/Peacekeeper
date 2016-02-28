package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.commands.MuteCommand;
import com.gmail.sintinium.peacekeeper.commands.SuspendCommand;
import com.gmail.sintinium.peacekeeper.data.conversation.ConversationData;
import com.gmail.sintinium.peacekeeper.data.conversation.MuteConversationData;
import com.gmail.sintinium.peacekeeper.data.conversation.SuspendConversationData;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.PunishmentHelper;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    }
                }

                Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        List<String> missedChat = data.missedMessages;
                        peacekeeper.conversationListener.conversations.remove(sender);

                        if (missedChat.isEmpty()) {
                            if (cancelled)
                                sender.sendMessage("Cancelled, you missed no chat messages");
                            else
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
            }
            ConversationData data = conversations.get(event.getPlayer());
            if (data instanceof MuteConversationData) handleMuteChat(event);
            else if (data instanceof SuspendConversationData) handleSuspendChat(event);
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

    // If the person is in a mute conversation handle it here
    public void handleMuteChat(final AsyncPlayerChatEvent event) {
        if (!StringUtils.isNumeric(event.getMessage())) {
            sendConversationInstructions(event.getPlayer());
            return;
        }
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                final Integer stockID = Integer.parseInt(event.getMessage());
                final ConversationData data = conversations.get(event.getPlayer());
                final PunishmentHelper.PunishmentResult result = peacekeeper.punishmentHelper.getTime(data.playerID, ConversationListener.ConversationType.MUTE, data.results.get(stockID - 1).length);

                if (data.timeResults.contains(data.results.get(stockID - 1))) {
                    data.finalTime -= result.time;
                    data.timeResults.remove(data.results.get(stockID - 1));
                } else {
                    data.finalTime += result.time;
                    data.timeResults.add(data.results.get(stockID - 1));
                }
                sendConversationInstructions(event.getPlayer());
            }
        });
    }

    // If the person is in a suspend conversation handle it here
    public void handleSuspendChat(final AsyncPlayerChatEvent event) {
        if (!StringUtils.isNumeric(event.getMessage())) {
            sendConversationInstructions(event.getPlayer());
            return;
        }

        final Integer stockID = Integer.parseInt(event.getMessage());
        final ConversationData data = conversations.get(event.getPlayer());

        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                final PunishmentHelper.PunishmentResult result = peacekeeper.punishmentHelper.getTime(data.playerID, ConversationListener.ConversationType.SUSPEND, data.results.get(stockID - 1).length);
                if (data.timeResults.contains(data.results.get(stockID - 1))) {
                    data.finalTime -= result.time;
                    data.timeResults.remove(data.results.get(stockID - 1));
                } else {
                    data.finalTime += result.time;
                    data.timeResults.add(data.results.get(stockID - 1));
                }
                sendConversationInstructions(event.getPlayer());
            }
        });
    }

    public void onMuteChatFinish(final Player sender) {
        final ConversationData data = conversations.get(sender);
        final String ordinal = TimeUtils.ordinal(peacekeeper.punishmentHelper.getOffsenseCount(ConversationType.MUTE, data.playerID) + 1);
        String finalReason = data.reason + " (" + ordinal + " offense)";
        MuteCommand.muteUser(sender, peacekeeper, data.punishedUUID, data.punishedUsername, data.playerID, data.finalTime, finalReason, categoriesToString(data));
        ChatUtils.muteMessage(sender, data.punishedUsername, data.finalTime, finalReason);

        String categoryString = categoriesToString(data);
        if (data.timeResults.size() == 1)
            ChatUtils.broadcast(ChatColor.DARK_AQUA + "Mute category: " + ChatColor.AQUA + categoryString);
        else
            ChatUtils.broadcast(ChatColor.DARK_AQUA + "Mute categories: " + ChatColor.AQUA + categoryString);
    }

    public void onSuspendChatFinish(Player sender) {
        ConversationData data = conversations.get(sender);
        final String ordinal = TimeUtils.ordinal(peacekeeper.punishmentHelper.getOffsenseCount(ConversationType.SUSPEND, data.playerID) + 1);

        String finalReason = data.reason + " (" + ordinal + " offense)";
        SuspendCommand.suspendUser(peacekeeper, sender, data.playerID, data.punishedUsername, data.finalTime, finalReason, categoriesToString(data));
        ChatUtils.banPlayerMessage(sender, data.punishedUsername, data.finalTime, finalReason);

        String categoryString = categoriesToString(data);
        if (data.timeResults.size() == 1)
            ChatUtils.broadcast(ChatColor.DARK_AQUA + "Suspend category: " + ChatColor.AQUA + categoryString);
        else
            ChatUtils.broadcast(ChatColor.DARK_AQUA + "Suspend categories: " + ChatColor.AQUA + categoryString);
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
        ConversationData data = conversations.get(player);
        player.sendMessage(data.header);
        player.sendMessage(ChatColor.YELLOW + "Click/type all that apply.");
        for (int i = data.page * pageCount; i < (data.page * pageCount) + pageCount && i < data.results.size(); i++) {
            TimeManager.TimeResult r = data.results.get(i);
            IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a("[\"\",{\"text\":\"" + String.valueOf(i + 1) + ". " + r.description + ": " + "\",\"color\":\"dark_aqua\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + (i + 1) + "\"}},{\"text\":\"" + r.length + "\",\"color\":\"aqua\"}]");
            PacketPlayOutChat packet = new PacketPlayOutChat(component);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
//            player.sendMessage(ChatColor.DARK_AQUA + String.valueOf(i) + ". " + r.description + ": " + ChatColor.AQUA + r.length);
        }

//        player.sendMessage(ChatColor.DARK_AQUA + "-------------------");
        if (!data.timeResults.isEmpty()) {

            player.sendMessage(ChatColor.YELLOW + "Selected categories: " + categoriesToString(data));

            IChatBaseComponent component1 = IChatBaseComponent.ChatSerializer.a(cancelFinish);
            PacketPlayOutChat packet1 = new PacketPlayOutChat(component1);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet1);
        } else {
            IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(cancel);
            PacketPlayOutChat packet = new PacketPlayOutChat(component);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
        String pageInfo = " " + (data.page + 1) + "/" + ((data.results.size() / pageCount) + 1) + " ";
        if (data.page == 0) {
            IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a("[\"\",{\"text\":\"" + pageInfo + "\",\"color\":\"aqua\"},{\"text\":\"[>]\",\"color\":\"yellow\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "page " + (data.page + 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Next Page\"}]}}}]");
            PacketPlayOutChat packet = new PacketPlayOutChat(component);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        } else if (data.page >= data.results.size() / pageCount) {
            IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a("[\"\",{\"text\":\"[<]\",\"color\":\"yellow\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "page " + (data.page - 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Previous Page\"}]}}},{\"text\":\"" + pageInfo + "\",\"color\":\"aqua\",\"bold\":false}]");
            PacketPlayOutChat packet = new PacketPlayOutChat(component);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        } else {
            IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a("[\"\",{\"text\":\"[<]\",\"color\":\"yellow\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "page " + (data.page - 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Previous Page\"}]}}},{\"text\":\"" + pageInfo + "\",\"color\":\"aqua\",\"bold\":false},{\"text\":\"[>]\",\"color\":\"yellow\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"" + "page " + (data.page + 1) + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Next Page\"}]}}}]");
            PacketPlayOutChat packet = new PacketPlayOutChat(component);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    // If a player quits remove them from the conversation to prevent any memory leaks
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
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

    public enum ConversationType {
        WARN, KICK, MUTE, SUSPEND, BAN, IPBAN, REPORT
    }

}
