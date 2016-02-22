package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.data.ConversationData;
import com.gmail.sintinium.peacekeeper.data.PlayerData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerBanTable;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.utils.BanUtils;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SuspendCommand extends BaseCommand {

    public SuspendCommand(Peacekeeper peacekeeper) {
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
        strings.add("Being a \"Troll\"");
        strings.add("Stealing");
        strings.add("Griefing");
        strings.add("Cheating");
        return strings;
    }

    // Suspends the given player from the server
    public static void suspendUser(Peacekeeper peacekeeper, CommandSender sender, int playerID, String username, Long time, String reason, Integer severity) {
        ChatUtils.banPlayerMessage(sender, username, time, reason);
        Integer adminID = peacekeeper.userTable.getId(((Player) sender).getUniqueId().toString());
        int recordID = peacekeeper.recordTable.addRecord(playerID, adminID, PlayerRecordTable.BAN, time, reason, severity);
        BanData banData = new BanData(null, System.currentTimeMillis(), playerID, null, reason, adminID, time, PlayerBanTable.PLAYER, recordID);
        peacekeeper.banTable.banUser(playerID, banData);

        Player player = Peacekeeper.getPlayer(sender, username);
        if (player != null)
            player.kickPlayer(BanUtils.generateBanMessage(peacekeeper, banData));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        // suspend <player> <reason>
        if (!(sender instanceof Player))
            return handleConsole(sender, args);

        if (args.length < 2) {
            return false;
        }

        String nameInput = args[0];
        String reasonInput = CommandUtils.argsToReason(args, 1);
        PlayerData playerData = peacekeeper.userTable.getPlayerData(nameInput);
        if (playerData == null) {
            ChatUtils.playerNotFoundMessage(sender, nameInput);
            return true;
        }

        ConversationData data = new ConversationData(generateSeverities(), ConversationListener.ConversationType.SUSPEND);
        data.setupSuspendConversation(playerData.playerID, reasonInput, playerData.username);
        peacekeeper.conversationListener.conversations.put((Player) sender, data);
        peacekeeper.conversationListener.sendConversationInstructions((Player) sender);

        return true;
    }

    // If the command isn't send by the player handle it as manual override since conversations won't work with
    // console without it being annoying
    public boolean handleConsole(CommandSender sender, String args[]) {
        if (args.length < 3) {
            sender.sendMessage("Args: suspend <player> <length> <reason>");
            return true;
        }
        String nameInput = args[0];
        String lengthInput = args[1];
        String reasonInput = CommandUtils.argsToReason(args, 2);
        PlayerData playerData = peacekeeper.userTable.getPlayerData(nameInput);
        if (playerData == null) {
            ChatUtils.playerNotFoundMessage(sender, nameInput);
            return true;
        }

        suspendUser(peacekeeper, sender, playerData.playerID, playerData.username, TimeUtils.stringToMillis(lengthInput), reasonInput, null);
        return true;
    }

}
