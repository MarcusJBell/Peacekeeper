package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerBanTable;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
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
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        // args /ipban <IP|Username> <Reason>
        if (args.length < 2) {
            return false;
        }
        if (CommandUtils.isIP(args[0])) { // If regex detects ip handle as ip
            return handleIP(sender, args);
        } else {
            return handlePlayer(sender, args);
        }
    }

    public boolean handlePlayer(CommandSender sender, String[] args) {
        BanData banData;
        String reason = CommandUtils.argsToReason(args, 1);
        Integer playerID = peacekeeper.userTable.getId(args[0]);
        if (playerID == null) {
            ChatUtils.playerNotFoundMessage(sender, args[0]);
            return true;
        }
        Integer adminID = null;
        if (sender instanceof Player) {
            adminID = peacekeeper.userTable.getId(((Player) sender).getUniqueId().toString());
        }
        String ip = peacekeeper.userTable.getIP(playerID);
        int recordID = peacekeeper.recordTable.addRecord(playerID, adminID, PlayerRecordTable.IP, null, reason, null);
        banData = new BanData(null, System.currentTimeMillis(), playerID, ip, reason, adminID, null, PlayerBanTable.IP, recordID);
        peacekeeper.banTable.banIP(banData);

        // Kick player if they're on the server
//        Player player = Peacekeeper.getPlayer(sender, args, 0);
//        if (player != null) {
//            player.kickPlayer(BanUtils.generateBanMessage(peacekeeper, banData));
//        }

        // Kick all players who have the same IP as IP banned player
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getAddress().getHostName().equals(ip)) {
                p.kickPlayer(BanUtils.generateBanMessage(peacekeeper, banData));
                ChatUtils.banIPMessage(sender, banData.ip, null, banData.reason);
            }
        }
        ChatUtils.banIPMessage(sender, ip, null, reason);
        return true;
    }

    public boolean handleIP(CommandSender sender, String[] args) {
        BanData banData;
        Integer adminID = null;
        if (sender instanceof Player) {
            adminID = peacekeeper.userTable.getId(((Player) sender).getUniqueId().toString());
        }
        String reason = CommandUtils.argsToReason(args, 1);
        banData = new BanData(null, null, null, args[0], reason, adminID, null, PlayerBanTable.IP, null);
        peacekeeper.recordTable.addRecord(null, adminID, PlayerRecordTable.IP, null, reason, null);
        peacekeeper.banTable.banIP(banData);

        // Kick all online players who have the same IP as banned IP
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (p.getAddress().getHostName().equals(args[0])) {
                p.kickPlayer(BanUtils.generateBanMessage(peacekeeper, banData));
                ChatUtils.banIPMessage(sender, banData.ip, null, banData.reason);
            }
        }
        ChatUtils.banIPMessage(sender, args[0], null, reason);
        return true;
    }

}
