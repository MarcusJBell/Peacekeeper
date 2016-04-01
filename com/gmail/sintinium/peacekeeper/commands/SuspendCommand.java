package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.data.PlayerData;
import com.gmail.sintinium.peacekeeper.data.conversation.SuspendConversationData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerBanTable;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class SuspendCommand extends BaseCommand {

    public SuspendCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    // Suspends the given player from the server
    public static void suspendUser(final Peacekeeper peacekeeper, final CommandSender sender, final int playerID, final String username, final Long inLength, final String reasonInput, final String description, final SuspendConversationData conversationData) {
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                Integer adminID = null;
                if (sender instanceof Player)
                    adminID = peacekeeper.userTable.getPlayerIDFromUUID(((Player) sender).getUniqueId().toString());
                if (conversationData != null && conversationData.updateSuspension) {
                    peacekeeper.recordTable.removeRecord(conversationData.oldRecord);
                    peacekeeper.banListener.cachedBans.remove(UUID.fromString(peacekeeper.userTable.getUserUUID(playerID)));
                }
                try {
                    peacekeeper.muteTable.db.getConnection().commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                final Long time;
                final PunishmentHelper.PunishmentResult result;
                if (inLength != null) {
                    result = peacekeeper.punishmentHelper.getTime(playerID, ConversationListener.ConversationType.SUSPEND, inLength);
                    time = result.time;
                } else {
                    time = null;
                    result = null;
                }

                final String ordinal;
                final String reason;
                if (result != null) {
                    ordinal = TimeUtils.ordinal(result.offenseCount + 1);
                    reason = reasonInput + " (" + ordinal + " offense)";
                } else {
                    reason = reasonInput;
                }

                int recordID = peacekeeper.recordTable.addRecord(playerID, null, adminID, PlayerRecordTable.BAN, time, reason, description);
                BanData banData = new BanData(null, System.currentTimeMillis(), playerID, null, reason, adminID, time, PlayerBanTable.PLAYER, recordID);
                peacekeeper.banTable.banUser(playerID, banData);
                final String banMessage = BanUtils.generateBanMessage(peacekeeper, banData);

                ChatUtils.banPlayerMessage(sender, username, time, reason);

                // Kick player back on main thread
                Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        Player player = Peacekeeper.getExactPlayer(username);
                        if (player != null)
                            player.kickPlayer(banMessage);
                    }
                });
            }
        });
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, String s, final String[] args) {
        // suspend <player> <reason>
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                if (!(sender instanceof Player)) {
                    handleConsole(sender, args);
                    return;
                }

                if (args.length < 2) {
                    sender.sendMessage(command.getUsage());
                    return;
                } else if (peacekeeper.conversationListener.conversations.containsKey(sender)) {
                    sender.sendMessage(ChatColor.YELLOW + "You cannot suspend while doing another action.");
                    sender.sendMessage(ChatColor.YELLOW + "Cancel your previous action and try again.");
                    return;
                }

                String nameInput = args[0];
                final String reasonInput = CommandUtils.argsToReason(args, 1);
                final PlayerData playerData = peacekeeper.userTable.getPlayerData(sender, nameInput);
                if (playerData == null) {
                    ChatUtils.playerNotFoundMessage(sender, nameInput);
                    return;
                }

                if (peacekeeper.banTable.isPlayerBanned(playerData.playerID)) {
                    BanData banData = peacekeeper.handleBan(playerData.playerID);
                    if (banData != null) {
                        Integer playerID = peacekeeper.userTable.getPlayerIDFromUUID(((Player) sender).getUniqueId().toString());
                        String name = peacekeeper.userTable.getUsername(playerID);
                        if (playerID == null || banData.adminId != playerID.intValue()) {
                            if (sender.hasPermission("peacekeeper.command.overridepunishment")) {
                                updateSuspend(sender, playerData, banData, reasonInput, name, false);
                            } else {
                                sender.sendMessage(ChatColor.RED + playerData.username + ChatColor.DARK_RED + " is currently banned by: " + ChatColor.RED + name);
                                sender.sendMessage(ChatColor.DARK_RED + "And you do not have permission to override punishments");
                                return;
                            }
                            return;
                        } else if (banData.adminId == playerID.intValue()) {
                            updateSuspend(sender, playerData, banData, reasonInput, name, true);
                            return;
                        }
                    }
                }

                // Add player to conversations in main thread
                Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        String header = ChatColor.DARK_AQUA + "Suspending: " + ChatColor.AQUA + playerData.username;
                        SuspendConversationData data = new SuspendConversationData(peacekeeper.timeManager.configMap.get(TimeManager.SUSPEND), ConversationListener.ConversationType.SUSPEND, header);
                        data.setupSuspendConversation(playerData.playerID, reasonInput, playerData.username);
                        peacekeeper.conversationListener.conversations.put((Player) sender, data);
                        peacekeeper.conversationListener.sendConversationInstructions((Player) sender);
                    }
                });
            }
        });

        return true;
    }

    public void updateSuspend(final CommandSender sender, final PlayerData playerData, final BanData banData, final String reason, String previousBanner, final boolean selves) {
        final String oldBanner = previousBanner + "'s";
        Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
            @Override
            public void run() {
                String header;
                if (!selves) {
                    header = ChatColor.DARK_AQUA + "Overriding " + ChatColor.AQUA + oldBanner + ChatColor.DARK_AQUA + " suspension for: " + ChatColor.AQUA + playerData.username + "\n" +
                            ChatColor.DARK_RED + "NOTE: YOU SHOULD ONLY EDIT OTHER'S SUSPENSIONS ONLY WITH PERMISSION";
                } else {
                    header = ChatColor.DARK_AQUA + "Updating suspension for: " + ChatColor.AQUA + playerData.username;
                }

                SuspendConversationData data = new SuspendConversationData(peacekeeper.timeManager.configMap.get(TimeManager.SUSPEND), ConversationListener.ConversationType.SUSPEND, header);
                data.updateSuspension(banData.recordId);
                data.setupSuspendConversation(playerData.playerID, reason, playerData.username);
                peacekeeper.conversationListener.conversations.put((Player) sender, data);
                peacekeeper.conversationListener.sendConversationInstructions((Player) sender);
            }
        });
    }

    // If the command isn't send by the player handle it as manual override since conversations won't work with
    // console without it being annoying
    public void handleConsole(final CommandSender sender, final String args[]) {
        if (args.length < 3) {
            sender.sendMessage("Args: suspend <player> <length> <reason>");
            return;
        }
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                String nameInput = args[0];
                String lengthInput = args[1];
                String reasonInput = CommandUtils.argsToReason(args, 2);
                PlayerData playerData = peacekeeper.userTable.getPlayerData(sender, nameInput);
                if (playerData == null) {
                    ChatUtils.playerNotFoundMessage(sender, nameInput);
                    return;
                }

                long time = TimeUtils.stringToMillis(lengthInput);
                suspendUser(peacekeeper, sender, playerData.playerID, playerData.username, time, reasonInput, null, null);
                ChatUtils.banPlayerMessage(sender, playerData.username, time, reasonInput);
            }
        });
    }

}
