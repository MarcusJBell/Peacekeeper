package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.commands.MuteCommand;
import com.gmail.sintinium.peacekeeper.commands.SuspendCommand;
import com.gmail.sintinium.peacekeeper.data.ConversationData;
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

    public static final String cancel = "{\"text\":\"[CANCEL]\",\"color\":\"yellow\",\"bold\":true,\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/peacekeepercancel\"}}";
    public Map<Player, ConversationData> conversations;
    private Peacekeeper peacekeeper;

    public ConversationListener(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        conversations = new HashMap<>();
    }

    // Removes the player from the conversation and sends missed messages.
    public void removeConversation(Player sender, boolean cancelled) {
        ConversationData data = peacekeeper.conversationListener.conversations.get(sender);
        List<String> missedChat = data.missedMessages;
        peacekeeper.conversationListener.conversations.remove(sender);
        ChatUtils.clearChat(sender);

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

    // If the player cancels the conversation remove them from the map and send them missed messages
    public void cancelConversation(Player sender) {
        removeConversation(sender, true);
    }

    // Handles conversations
    @EventHandler(priority = EventPriority.HIGH)
    public void playerChat(AsyncPlayerChatEvent event) {
        // If the sender is in a conversation cancel it and handle accordingly
        if (conversations.containsKey(event.getPlayer())) {
            ConversationData data = conversations.get(event.getPlayer());
            event.setCancelled(true);
            if (data.muteConversation) handleMuteChat(event);
            else if (data.banConversation) handleSuspendChat(event);
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
        //TODO: If this is ever changed again change from conversation data to a super class of it. It makes for a mess if any variable needs changed
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                final Integer stockID = Integer.parseInt(event.getMessage());
                final ConversationData data = conversations.get(event.getPlayer());
                final PunishmentHelper.PunishmentResult result = peacekeeper.punishmentHelper.getTime(data.playerID, ConversationListener.ConversationType.MUTE, data.results.get(stockID - 1).length);
                final String username = peacekeeper.userTable.getUsername(data.playerID);

                final String ordinal = TimeUtils.ordinal(result.offenseCount);
                //Since we're Async we need to run on the same thread
                Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        MuteCommand.muteUser(event.getPlayer(), peacekeeper, data.punishedUUID, username, data.playerID, result.time, data.reason + " (" + ordinal + " offense)", stockID);
                    }
                });
                removeConversation(event.getPlayer(), false);
            }
        });
    }

    // If the person is in a suspend conversation handle it here
    public void handleSuspendChat(final AsyncPlayerChatEvent event) {
        if (!StringUtils.isNumeric(event.getMessage())) {
            sendConversationInstructions(event.getPlayer());
            return;
        }
        //TODO: If this is ever changed again change from conversation data to a super class of it. It makes for a mess if any variable needs changed
        final Integer stockID = Integer.parseInt(event.getMessage());
        final ConversationData data = conversations.get(event.getPlayer());

        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                final PunishmentHelper.PunishmentResult result = peacekeeper.punishmentHelper.getTime(data.playerID, ConversationListener.ConversationType.SUSPEND, data.results.get(stockID - 1).length);

                final String ordinal = TimeUtils.ordinal(result.offenseCount + 1);
                //Since we're Async we need to run on the same thread
                Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        SuspendCommand.suspendUser(peacekeeper, event.getPlayer(), data.playerID, data.punishedUsername, result.time, data.reason + " (" + ordinal + " offense)", stockID);
                    }
                });
                removeConversation(event.getPlayer(), false);
            }
        });
    }

    // Messages the player what they need to type to continue on with the conversation
    public void sendConversationInstructions(Player player) {
        ConversationData data = conversations.get(player);
        int i = 0;
        for (TimeManager.TimeResult r : data.results) {
            i++;
            player.sendMessage(ChatColor.DARK_AQUA + String.valueOf(i) + ". " + r.description + ": " + ChatColor.AQUA + r.length);
        }

        player.sendMessage(ChatColor.YELLOW + "To cancel click the cancel button below");
        IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(cancel);
        PacketPlayOutChat packet = new PacketPlayOutChat(component);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    // If a player quits remove them from the conversation to prevent any memory leaks
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        conversations.remove(event.getPlayer());
    }

    public enum ConversationType {
        WARN, KICK, MUTE, SUSPEND, BAN, IPBAN, REPORT
    }

}
