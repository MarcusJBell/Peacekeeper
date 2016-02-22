package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.ConversationData;
import com.gmail.sintinium.peacekeeper.data.MuteData;
import com.gmail.sintinium.peacekeeper.data.PlayerData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
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
    public static void muteUser(CommandSender sender, Peacekeeper peacekeeper, String uuid, String username, int playerID, Long length, String reason, Integer severity) {
        Integer adminID = peacekeeper.userTable.getId(((Player) sender).getUniqueId().toString());
        int recordID = peacekeeper.recordTable.addRecord(playerID, adminID, PlayerRecordTable.MUTE, length, reason, severity);
        int muteID = peacekeeper.muteTable.muteUser(playerID, length, reason, adminID, recordID);
        MuteData muteData = peacekeeper.muteTable.muteData(muteID);
        peacekeeper.muteTable.mutedPlayers.put(UUID.fromString(uuid), muteData);
        ChatUtils.muteMessage(sender, username, length, reason);

        Player player = Peacekeeper.getPlayer(sender, username);
        if (player != null) {
            player.sendMessage(ChatColor.DARK_RED + "You have been muted by: " + ChatColor.RED + muteData.adminName);
            if (length != null)
                player.sendMessage(ChatColor.YELLOW + "Mute will end in: " + TimeUtils.millsToString(length));
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player))
            return handleConsole(sender, args);
        if (args.length < 2) {
            return false;
        }
        String usernameInput = args[0];
        String reasonInput = CommandUtils.argsToReason(args, 1);
        PlayerData playerData = peacekeeper.userTable.getPlayerData(usernameInput);
        if (playerData == null) {
            playerNotFoundMessage(sender, usernameInput);
            return true;
        }

        ConversationData data = new ConversationData(generateSeverities(), ConversationListener.ConversationType.MUTE);
        data.setupMuteConversation(playerData.playerID, reasonInput, playerData.uuid.toString());
        peacekeeper.conversationListener.conversations.put((Player) sender, data);
        peacekeeper.conversationListener.sendConversationInstructions((Player) sender);
        return true;
    }

    // If the command isn't send by the player handle it as manual override since conversations won't work with
    // console without it being annoying
    public boolean handleConsole(CommandSender sender, String args[]) {
        if (args.length < 3) {
            sender.sendMessage("Args: mute <player> <length> <reason>");
            return true;
        }
        String nameInput = args[0];
        String lengthInput = args[1];
        String reasonInput = CommandUtils.argsToReason(args, 2);
        PlayerData playerData = peacekeeper.userTable.getPlayerData(nameInput);
        if (playerData == null) {
            playerNotFoundMessage(sender, nameInput);
            return true;
        }

        muteUser(sender, peacekeeper, playerData.uuid.toString(), playerData.username, playerData.playerID, TimeUtils.stringToMillis(lengthInput), reasonInput, null);
        return true;
    }

    public void playerNotFoundMessage(CommandSender sender, String name) {
        sender.sendMessage(ChatColor.DARK_RED + "Player " + name + " was not found in the database");
    }

}
