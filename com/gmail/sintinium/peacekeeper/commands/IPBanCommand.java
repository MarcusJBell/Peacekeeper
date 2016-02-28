package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerBanTable;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.BanUtils;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IPBanCommand extends BaseCommand {

    public IPBanCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String s, final String[] args) {
        // args /ipban <IP|Username> <Reason>
        if (args.length < 2) {
            return false;
        }
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                if (CommandUtils.isIP(args[0])) { // If regex detects ip handle as ip
                    handleIP(sender, args);
                } else {
                    handlePlayer(sender, args);
                }
            }
        });
        return true;
    }

    public void handlePlayer(final CommandSender sender, String[] args) {
        final BanData banData;
        String reason = CommandUtils.argsToReason(args, 1);
        Integer playerID = peacekeeper.userTable.getPlayerIDFromUsername(args[0]);
        if (playerID == null) {
            ChatUtils.playerNotFoundMessage(sender, args[0]);
            return;
        }
        Integer adminID = null;
        if (sender instanceof Player) {
            adminID = peacekeeper.userTable.getPlayerIDFromUUID(((Player) sender).getUniqueId().toString());
        }
        final String ip = peacekeeper.userTable.getIP(playerID);
        int recordID = peacekeeper.recordTable.addRecord(playerID, ip, adminID, PlayerRecordTable.IP, null, reason, null);
        banData = new BanData(null, System.currentTimeMillis(), playerID, ip, reason, adminID, null, PlayerBanTable.IP, recordID);
        peacekeeper.banTable.banIP(banData);
        final String adminName = peacekeeper.userTable.getUsername(adminID);

        // Kick all players who have the same IP as IP banned player
        Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (p.getAddress().getHostName().equals(ip)) {
                        p.kickPlayer(BanUtils.generateSyncedBanMessage(banData, adminName));
                        ChatUtils.banIPMessage(sender, banData.ip, null, banData.reason);
                    }
                }
            }
        });
        ChatUtils.banIPMessage(sender, ip, null, reason);
    }

    public void handleIP(final CommandSender sender, final String[] args) {
        final BanData banData;
        Integer adminID = null;
        if (sender instanceof Player) {
            adminID = peacekeeper.userTable.getPlayerIDFromUUID(((Player) sender).getUniqueId().toString());
        }
        final String reason = CommandUtils.argsToReason(args, 1);
        banData = new BanData(null, null, null, args[0], reason, adminID, null, PlayerBanTable.IP, null);
        peacekeeper.recordTable.addRecord(null, args[0], adminID, PlayerRecordTable.IP, null, reason, null);
        peacekeeper.banTable.banIP(banData);

        // Kick player inside main thread
        Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
            @Override
            public void run() {
                // Kick all online players who have the same IP as banned IP
                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (p.getAddress().getHostName().equals(args[0])) {
                        p.kickPlayer(BanUtils.generateBanMessage(peacekeeper, banData));
                        ChatUtils.banIPMessage(sender, banData.ip, null, banData.reason);
                    }
                }
                ChatUtils.banIPMessage(sender, args[0], null, reason);
            }
        });
    }
}
