package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.data.MuteData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerBanTable;
import com.gmail.sintinium.peacekeeper.db.tables.UserTable;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerInfoCommand extends BaseCommand {

    public PlayerInfoCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 1) {
            return false;
        }

        String offlineUUID = peacekeeper.userTable.getOfflineUUID(args[0]);
        if (offlineUUID == null) {
            sender.sendMessage(ChatColor.DARK_RED + "Player " + args[0] + " was not found in database");
            return true;
        }

        UserTable db = peacekeeper.userTable;
        int playerID = db.getId(offlineUUID);
        String name = db.getUsername(playerID);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date joinDate = new Date(db.getUserTime(playerID));
        sdf.format(joinDate);
        String joinTime = joinDate.toString();
        String ip = db.getIP(playerID);

        sender.sendMessage(ChatColor.DARK_AQUA + "--- Player Info --");
        sender.sendMessage(ChatColor.DARK_AQUA + "Username: " + ChatColor.AQUA + name);
        sender.sendMessage(ChatColor.DARK_AQUA + "UUID: " + ChatColor.AQUA + offlineUUID);
        sender.sendMessage(ChatColor.DARK_AQUA + "Join date: " + ChatColor.AQUA + joinTime);
        if (sender.hasPermission("peacekeeper.playerinfo.ip")) {
            sender.sendMessage(ChatColor.DARK_AQUA + "IP: " + ChatColor.AQUA + ip);
        }

        boolean isPunished = false;

        BanData banData = peacekeeper.handleBan(playerID);
        if (banData != null) {
            String message;
            isPunished = true;
            if ((banData.banTime + banData.banLength) - System.currentTimeMillis() >= 1000) {
                if (banData.type == PlayerBanTable.PLAYER) {
                    if (banData.banLength != null)
                        message = ChatColor.AQUA + name + ChatColor.DARK_RED + " is suspended for " + ChatColor.YELLOW + TimeUtils.millsToString((banData.banTime + banData.banLength) - System.currentTimeMillis());
                    else
                        message = ChatColor.AQUA + name + ChatColor.DARK_RED + " is permanently banned";
                } else {
                    message = ChatColor.DARK_AQUA + "Player: " + ChatColor.AQUA + name + ChatColor.DARK_RED + " is IP banned";
                }
                sender.sendMessage(message + ChatColor.YELLOW + " RecordID: " + banData.recordId);
            }
        }

        if (peacekeeper.muteTable.isPlayerMuted(playerID)) {
            Integer muteID = peacekeeper.muteTable.getMuteIDFromPlayerID(playerID);
            if (muteID != null) {
                String message;
                isPunished = true;
                MuteData muteData = peacekeeper.muteTable.muteData(muteID);
                if ((muteData.muteTime + muteData.muteLength) - System.currentTimeMillis() >= 1000) {
                    if (muteData.muteLength != null) {
                        message = ChatColor.AQUA + name + ChatColor.DARK_RED + " is muted for " + ChatColor.YELLOW + TimeUtils.millsToString((muteData.muteTime + muteData.muteLength) - System.currentTimeMillis());
                    } else {
                        message = ChatColor.AQUA + name + ChatColor.DARK_RED + " is permanently muted";
                    }
                    sender.sendMessage(message + ChatColor.YELLOW + " RecordID: " + muteData.recordId);
                }
            }
        }

        if (isPunished)
            sender.sendMessage(ChatColor.DARK_AQUA + "To see current punishment info do: /records id <id>");

        if (peacekeeper.recordTable.doesValueExist("PlayerID", playerID))
            sender.sendMessage(ChatColor.DARK_AQUA + "To see all(" + peacekeeper.recordTable.recordCount(playerID) + ") user's records do:" + " /records user " + ChatColor.AQUA + name);
        else
            sender.sendMessage(ChatColor.AQUA + name + ChatColor.DARK_AQUA + " has a clean record!");

        return true;
    }

}
