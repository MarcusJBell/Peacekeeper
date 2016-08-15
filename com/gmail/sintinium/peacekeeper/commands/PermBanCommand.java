package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.data.PlayerData;
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

public class PermBanCommand extends BaseCommand {

    public PermBanCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    private static void banUser(final Peacekeeper peacekeeper, final CommandSender sender, final int playerID, final String username, final String reason) {
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                Integer adminID = null;
                if (sender instanceof Player)
                    adminID = peacekeeper.userTable.getPlayerIDFromUUID(((Player) sender).getUniqueId().toString());
                int recordID = peacekeeper.recordTable.addRecord(playerID, null, adminID, PlayerRecordTable.BAN, null, reason, "Permanent Ban");
                BanData banData = new BanData(null, System.currentTimeMillis(), playerID, null, reason, adminID, null, PlayerBanTable.PLAYER, recordID);
                peacekeeper.banTable.banUser(playerID, banData);
                final String banMessage = BanUtils.generateBanMessage(peacekeeper, banData);

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
    public boolean onCommand(final CommandSender sender, Command command, String s, final String[] args) {
        if (args.length < 2) {
            return false;
        }
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                String nameInput = args[0];
                String reasonInput = CommandUtils.argsToReason(args, 1);
                PlayerData playerData = peacekeeper.userTable.getPlayerData(sender, nameInput);
                if (playerData == null) {
                    ChatUtils.playerNotFoundMessage(sender, nameInput);
                    return;
                }

                banUser(peacekeeper, sender, playerData.playerID, playerData.username, reasonInput);
                ChatUtils.banPlayerMessage(sender, playerData.username, null, reasonInput);
            }
        });
        return true;
    }

}
