package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.MuteData;
import com.gmail.sintinium.peacekeeper.data.PlayerData;
import com.gmail.sintinium.peacekeeper.data.conversation.MuteConversationData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import com.gmail.sintinium.peacekeeper.utils.PunishmentHelper;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

public class MuteCommand extends BaseCommand {

    public MuteCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    // Method that is actually called to mute a user
    public static void muteUser(final CommandSender sender, final Peacekeeper peacekeeper, final String uuid, final String username, final int playerID, final Long inLength, final String reasonInput, final String description, final MuteConversationData conversationData) {
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                Integer adminID = null;
                if (sender instanceof Player)
                    adminID = peacekeeper.userTable.getPlayerIDFromUUID(((Player) sender).getUniqueId().toString());
                if (conversationData != null && conversationData.updateMute) {
                    peacekeeper.recordTable.removeRecord(conversationData.oldRecord);
                }
                try {
                    peacekeeper.muteTable.db.getConnection().commit();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                final Long length;

                final PunishmentHelper.PunishmentResult result;
                if (inLength != null) {
                    result = peacekeeper.punishmentHelper.getTime(playerID, ConversationListener.ConversationType.MUTE, inLength);
                    length = result.time;
                } else {
                    length = null;
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

                int recordID = peacekeeper.recordTable.addRecord(playerID, null, adminID, PlayerRecordTable.MUTE, length, reason, description);
                int muteID = peacekeeper.muteTable.muteUser(playerID, length, reason, adminID, recordID);
                MuteData muteData = peacekeeper.muteTable.muteData(muteID);
                peacekeeper.muteTable.mutedPlayers.put(UUID.fromString(uuid), muteData);
                ChatUtils.muteMessage(sender, username, length, reason);

                // Get player back on main thread
                final MuteData MUTE_DATA = muteData;
                final String adminName = peacekeeper.userTable.getUsername(muteData.adminId);
                Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        Player player = Peacekeeper.getExactPlayer(username);
                        if (player != null) {
                            player.sendMessage(ChatColor.DARK_RED + "You have been muted by: " + ChatColor.RED + adminName);
                            player.sendMessage(ChatColor.DARK_RED + "Reason: " + ChatColor.YELLOW + reason);
                            if (length != null)
                                player.sendMessage(ChatColor.YELLOW + "Mute will end in: " + TimeUtils.millsToString(length));
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, String s, final String[] args) {
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
                    sender.sendMessage(ChatColor.YELLOW + "You cannot mute while doing another action.");
                    sender.sendMessage(ChatColor.YELLOW + "Cancel your previous action and try again.");
                    return;
                }
                String usernameInput = args[0];
                final String reasonInput = CommandUtils.argsToReason(args, 1);
                final PlayerData playerData = peacekeeper.userTable.getPlayerData(sender, usernameInput);
                if (playerData == null) {
                    playerNotFoundMessage(sender, usernameInput);
                    return;
                }

                if (peacekeeper.muteTable.isPlayerMuted(playerData.playerID)) {
                    MuteData muteData = peacekeeper.handleMute(playerData.playerID);
                    if (muteData != null) {
                        Integer playerID = peacekeeper.userTable.getPlayerIDFromUUID(((Player) sender).getUniqueId().toString());
                        String name = peacekeeper.userTable.getUsername(playerID);
                        if (playerID == null || muteData.adminId != playerID.intValue()) {
                            if (sender.hasPermission("peacekeeper.command.overridepunishment")) {
                                updateMute(sender, playerData, muteData, reasonInput, name, false);
                            } else {
                                sender.sendMessage(ChatColor.RED + playerData.username + ChatColor.DARK_RED + " is currently muted by: " + ChatColor.RED + name);
                                sender.sendMessage(ChatColor.DARK_RED + "And you do not have permission to override punishments");
                                return;
                            }
                            return;
                        } else if (muteData.adminId == playerID.intValue()) {
                            updateMute(sender, playerData, muteData, reasonInput, name, true);
                            return;
                        }
                    }
                }

                // Add sender to conversation in main bukkit thread
                Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        String header = ChatColor.DARK_AQUA + "Muting: " + ChatColor.AQUA + playerData.username;
                        MuteConversationData data = new MuteConversationData(peacekeeper.timeManager.configMap.get(TimeManager.MUTE), ConversationListener.ConversationType.MUTE, header);
                        data.setupMuteConversation(playerData.playerID, reasonInput, playerData.uuid.toString(), playerData.username);
                        peacekeeper.conversationListener.conversations.put((Player) sender, data);
                        peacekeeper.conversationListener.sendConversationInstructions((Player) sender);
                    }
                });
            }
        });

        return true;
    }

    private void updateMute(final CommandSender sender, final PlayerData playerData, final MuteData muteData, final String reason, String previousMuter, final boolean selves) {
        final String oldBanner = previousMuter + "'s";
        Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
            @Override
            public void run() {
                String header;
                if (!selves) {
                    header = ChatColor.DARK_AQUA + "Overriding " + ChatColor.AQUA + oldBanner + ChatColor.DARK_AQUA + " mute for: " + ChatColor.AQUA + playerData.username + "\n" +
                            ChatColor.DARK_RED + "NOTE: YOU SHOULD ONLY EDIT OTHER'S SUSPENSIONS ONLY WITH PERMISSION";
                } else {
                    header = ChatColor.DARK_AQUA + "Updating mute for: " + ChatColor.AQUA + playerData.username;
                }

                MuteConversationData data = new MuteConversationData(peacekeeper.timeManager.configMap.get(TimeManager.MUTE), ConversationListener.ConversationType.MUTE, header);
                data.updateMute(muteData.recordId);
                data.setupMuteConversation(playerData.playerID, reason, playerData.uuid.toString(), playerData.username);
                peacekeeper.conversationListener.conversations.put((Player) sender, data);
                peacekeeper.conversationListener.sendConversationInstructions((Player) sender);
            }
        });
    }

    // If the command isn't send by the player handle it as manual override since conversations won't work with
    // console without it being annoying
    private void handleConsole(final CommandSender sender, final String args[]) {
        if (args.length < 3) {
            sender.sendMessage("Args: mute <player> <length> <reason>");
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
                    playerNotFoundMessage(sender, nameInput);
                    return;
                }

                long time = TimeUtils.stringToMillis(lengthInput);
                muteUser(sender, peacekeeper, playerData.uuid.toString(), playerData.username, playerData.playerID, time, reasonInput, null, null);
                ChatUtils.muteMessage(sender, playerData.username, time, reasonInput);
            }
        });
    }

    private void playerNotFoundMessage(CommandSender sender, String name) {
        sender.sendMessage(ChatColor.DARK_RED + "Player " + name + " was not found in the database");
    }

}
