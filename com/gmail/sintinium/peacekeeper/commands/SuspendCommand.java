package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.data.PlayerData;
import com.gmail.sintinium.peacekeeper.data.conversation.SuspendConversationData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerBanTable;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.BanUtils;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SuspendCommand extends BaseCommand {

    public SuspendCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    // Suspends the given player from the server
    public static void suspendUser(final Peacekeeper peacekeeper, final CommandSender sender, final int playerID, final String username, final Long time, final String reason, final String description) {
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                Integer adminID = peacekeeper.userTable.getPlayerIDFromUUID(((Player) sender).getUniqueId().toString());
                int recordID = peacekeeper.recordTable.addRecord(playerID, null, adminID, PlayerRecordTable.BAN, time, reason, description);
                BanData banData = new BanData(null, System.currentTimeMillis(), playerID, null, reason, adminID, time, PlayerBanTable.PLAYER, recordID);
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
    public boolean onCommand(final CommandSender sender, final Command command, String s, final String[] args) {
        // suspend <player> <reason>
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

                String nameInput = args[0];
                final String reasonInput = CommandUtils.argsToReason(args, 1);
                final PlayerData playerData = peacekeeper.userTable.getPlayerData(sender, nameInput);
                if (playerData == null) {
                    ChatUtils.playerNotFoundMessage(sender, nameInput);
                    return;
                }

                // Add player to conversations in main thread
                Bukkit.getScheduler().runTask(peacekeeper, new Runnable() {
                    @Override
                    public void run() {
                        String header = ChatColor.DARK_AQUA + "Suspending: " + ChatColor.AQUA + playerData.username;
                        SuspendConversationData data = new SuspendConversationData(peacekeeper.timeManager.configMap.get(TimeManager.SUSPEND), ConversationListener.ConversationType.SUSPEND, header);
                        data.setupSuspendConversation(playerData.playerID, reasonInput, playerData.username);
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
            sender.sendMessage("Args: suspend <player> <length> <reason>");
            return;
        }
        String nameInput = args[0];
        String lengthInput = args[1];
        String reasonInput = CommandUtils.argsToReason(args, 2);
        PlayerData playerData = peacekeeper.userTable.getPlayerData(sender, nameInput);
        if (playerData == null) {
            ChatUtils.playerNotFoundMessage(sender, nameInput);
            return;
        }

        suspendUser(peacekeeper, sender, playerData.playerID, playerData.username, TimeUtils.stringToMillis(lengthInput), reasonInput, null);
    }

}
