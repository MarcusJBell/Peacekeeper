package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.WarnData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarnCommand extends BaseCommand {

    public WarnCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String s, final String[] args) {
        // /warn <player> <reason>
        if (args.length < 2) return false;
        final String usernameInput = args[0];
        final String reason = CommandUtils.argsToReason(args, 1);
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                Integer playerID = peacekeeper.userTable.getPlayerIDFromUsername(usernameInput);
                if (playerID == null) {
                    ChatUtils.playerNotFoundMessage(sender, usernameInput);
                    return;
                }
                String playerName = peacekeeper.userTable.getUsername(playerID);

                Integer adminID = sender instanceof Player ? peacekeeper.userTable.getPlayerIDFromUUID(((Player) sender).getUniqueId().toString()) : null;
                int recordID = peacekeeper.recordTable.addRecord(playerID, null, adminID, PlayerRecordTable.WARNING, null, reason, null);
                peacekeeper.warnTable.warnPlayer(playerID, reason, adminID, recordID);
                Player player = Bukkit.getPlayerExact(playerName);
                if (player != null && player.isOnline()) {
                    warnPlayer(player);
                }
                ChatUtils.warnMessage(sender, playerName);
            }
        });


        return true;
    }

    public void warnPlayer(Player player) {
        int playerID = peacekeeper.userTable.getPlayerIDFromUUID(player.getUniqueId().toString());
        Integer warnID = peacekeeper.warnTable.getWarnIDFromPlayerID(playerID);
        if (warnID == null) {
            peacekeeper.getLogger().warning("Tried to warn player who has no warnings: " + player.getName());
        }
        WarnData warnData = peacekeeper.warnTable.warnData(warnID);
        player.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");
        player.sendMessage(warnData.generateWarnMessage(peacekeeper));
        player.sendMessage(ChatColor.GOLD + "NOTE: " + ChatColor.YELLOW + "Failure to follow the warning can/will lead to future suspension/mute");
        player.sendMessage(ChatColor.GOLD + "-----------------------------------------------------");
        peacekeeper.warnTable.deleteWarning(warnData.warnID);
    }

}
