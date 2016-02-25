package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.data.MuteData;
import com.gmail.sintinium.peacekeeper.data.PlayerData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerBanTable;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class PlayerInfoCommand extends BaseCommand {

    public PlayerInfoCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String s, final String[] args) {
        if (args.length < 1) {
            return false;
        }
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                PlayerData playerData = peacekeeper.userTable.getPlayerData(args[0]);
                if (playerData == null) {
                    ChatUtils.playerNotFoundMessage(sender, args[0]);
                    return;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ssa z");
                sdf.setTimeZone(TimeZone.getTimeZone("EST"));
                OfflinePlayer player = Bukkit.getOfflinePlayer(playerData.uuid);

                Date joinDate = new Date(player.getFirstPlayed());
                String joinTime = sdf.format(joinDate);

                String lastSeen = "Currently online";
                if (!player.isOnline()) {
                    Date lastDate = new Date(player.getLastPlayed());
                    lastSeen = sdf.format(lastDate);
                }

                sender.sendMessage(ChatColor.DARK_AQUA + "---- Player Info ----");
                sender.sendMessage(ChatColor.DARK_AQUA + "Username: " + ChatColor.AQUA + playerData.username);
                sender.sendMessage(ChatColor.DARK_AQUA + "UUID: " + ChatColor.AQUA + playerData.uuid);
                sender.sendMessage(ChatColor.DARK_AQUA + "Join date: " + ChatColor.AQUA + joinTime);
                sender.sendMessage(ChatColor.DARK_AQUA + "Last seen: " + ChatColor.AQUA + lastSeen);

                if (sender.hasPermission("peacekeeper.playerinfo.ip")) {
                    sender.sendMessage(ChatColor.DARK_AQUA + "IP: " + ChatColor.AQUA + playerData.ip);
                }

                boolean isPunished = false;

                BanData banData = peacekeeper.handleBan(playerData.playerID);
                if (banData != null) {
                    String message;
                    isPunished = true;
                    if ((banData.banTime + banData.banLength) - System.currentTimeMillis() >= 1000) {
                        if (banData.type == PlayerBanTable.PLAYER) {
                            if (banData.banLength != null)
                                message = ChatColor.AQUA + playerData.username + ChatColor.DARK_RED + " is suspended for " + ChatColor.YELLOW + TimeUtils.millsToString((banData.banTime + banData.banLength) - System.currentTimeMillis());
                            else
                                message = ChatColor.AQUA + playerData.username + ChatColor.DARK_RED + " is permanently banned";
                        } else {
                            message = ChatColor.DARK_AQUA + "Player: " + ChatColor.AQUA + playerData.username + ChatColor.DARK_RED + " is IP banned";
                        }
                        sender.sendMessage(message + ChatColor.YELLOW + " RecordID: " + banData.recordId);
                    }
                }

                if (peacekeeper.muteTable.isPlayerMuted(playerData.playerID)) {
                    Integer muteID = peacekeeper.muteTable.getMuteIDFromPlayerID(playerData.playerID);
                    if (muteID != null) {
                        String message;
                        isPunished = true;
                        MuteData muteData = peacekeeper.muteTable.muteData(muteID);
                        if ((muteData.muteTime + muteData.muteLength) - System.currentTimeMillis() >= 1000) {
                            if (muteData.muteLength != null) {
                                message = ChatColor.AQUA + playerData.username + ChatColor.DARK_RED + " is muted for " + ChatColor.YELLOW + TimeUtils.millsToString((muteData.muteTime + muteData.muteLength) - System.currentTimeMillis());
                            } else {
                                message = ChatColor.AQUA + playerData.username + ChatColor.DARK_RED + " is permanently muted";
                            }
                            sender.sendMessage(message + ChatColor.YELLOW + " RecordID: " + muteData.recordId);
                        }
                    }
                }

                if (isPunished)
                    sender.sendMessage(ChatColor.DARK_AQUA + "To see current punishment info do: /records id <id>");

                if (peacekeeper.recordTable.doesValueExist("PlayerID", playerData.playerID))
                    sender.sendMessage(ChatColor.DARK_AQUA + "To see all(" + peacekeeper.recordTable.recordCount(playerData.playerID) + ") user's records do:" + " /records user " + ChatColor.AQUA + playerData.username);
                else
                    sender.sendMessage(ChatColor.AQUA + playerData.username + ChatColor.DARK_AQUA + " has a clean record!");

            }
        });

        return true;
    }

}
