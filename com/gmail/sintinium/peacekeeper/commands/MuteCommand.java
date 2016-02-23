package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.ConversationData;
import com.gmail.sintinium.peacekeeper.data.MuteData;
import com.gmail.sintinium.peacekeeper.data.PlayerData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MuteCommand extends BaseCommand {

    public MuteCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    //TODO: Replace this placeholder code for something that reads a modifiable YAML
    public static List<String> generateSeverities() {
        List<String> strings = new ArrayList<>();
        List<String> messages = generateSeveritiesMessages();
        for (int i = 0; i < messages.size(); i++) {
            String count = String.valueOf(i + 1);
            strings.add(ChatColor.AQUA + count + ". " + ChatColor.DARK_AQUA + messages.get(i));
        }
        return strings;
    }

    //TODO: Replace this placeholder code for something that reads a modifiable YAML
    public static List<String> generateSeveritiesMessages() {
        List<String> strings = new ArrayList<>();
        strings.add("Common spam such as: CAPS and ijijiajsd");
        strings.add("Arguing with/ignoring admin");
        strings.add("Arguing with/ignoring admin");
        return strings;
    }

    // Method that is actually called to mute a user
    public static void muteUser(final CommandSender sender, final Peacekeeper peacekeeper, final String uuid, final String username, final int playerID, final Long length, final String reason, final Integer severity) {
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                Integer adminID = peacekeeper.userTable.getPlayerIDFromUUID(((Player) sender).getUniqueId().toString());
                int recordID = peacekeeper.recordTable.addRecord(playerID, adminID, PlayerRecordTable.MUTE, length, reason, severity);
                int muteID = peacekeeper.muteTable.muteUser(playerID, length, reason, adminID, recordID);
                MuteData muteData = peacekeeper.muteTable.muteData(muteID);
                peacekeeper.muteTable.mutedPlayers.put(UUID.fromString(uuid), muteData);
                ChatUtils.muteMessage(sender, username, length, reason);

                // Get player back on main thread
                final MuteData MUTE_DATA = muteData;
                Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        Player player = Peacekeeper.getExactPlayer(username);
                        if (player != null) {
                            player.sendMessage(ChatColor.DARK_RED + "You have been muted by: " + ChatColor.RED + MUTE_DATA.adminName);
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
                }
                String usernameInput = args[0];
                final String reasonInput = CommandUtils.argsToReason(args, 1);
                final PlayerData playerData = peacekeeper.userTable.getPlayerData(usernameInput);
                if (playerData == null) {
                    playerNotFoundMessage(sender, usernameInput);
                    return;
                }

                // Add sender to conversation in main bukkit thread
                Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        ConversationData data = new ConversationData(generateSeverities(), ConversationListener.ConversationType.MUTE);
                        data.setupMuteConversation(playerData.playerID, reasonInput, playerData.uuid.toString());
                        peacekeeper.conversationListener.conversations.put((Player) sender, data);
                        peacekeeper.conversationListener.sendConversationInstructions((Player) sender);
                    }
                });
            }
        });

        return true;
    }

    // If the command isn't send by the player handle it as manual override since conversations won't work with
    // console without it being annoying
    public void handleConsole(CommandSender sender, String args[]) {
        if (args.length < 3) {
            sender.sendMessage("Args: mute <player> <length> <reason>");
            return;
        }
        String nameInput = args[0];
        String lengthInput = args[1];
        String reasonInput = CommandUtils.argsToReason(args, 2);
        PlayerData playerData = peacekeeper.userTable.getPlayerData(nameInput);
        if (playerData == null) {
            playerNotFoundMessage(sender, nameInput);
            return;
        }

        muteUser(sender, peacekeeper, playerData.uuid.toString(), playerData.username, playerData.playerID, TimeUtils.stringToMillis(lengthInput), reasonInput, null);
    }

    public void playerNotFoundMessage(CommandSender sender, String name) {
        sender.sendMessage(ChatColor.DARK_RED + "Player " + name + " was not found in the database");
    }

}
