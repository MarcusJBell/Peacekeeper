package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReleaseCommand extends BaseCommand {

    public ReleaseCommand(Peacekeeper peacekeeper) {
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
                if (CommandUtils.isIP(args[0])) { // If input is IP
                    peacekeeper.banTable.unbanIP("'" + args[0] + "'");
                    ChatUtils.releaseIPMessage(sender, args[0]);
                } else { // Input is not IP
                    Integer playerID = peacekeeper.userTable.getPlayerIDFromUsername(args[0]);
                    if (playerID == null) {
                        ChatUtils.playerNotFoundMessage(sender, args[0]);
                        return;
                    }
                    peacekeeper.banTable.unbanPlayer(playerID);
                    peacekeeper.banTable.unbanIP(peacekeeper.userTable.getIP(playerID));
                    peacekeeper.muteTable.unmutePlayer(playerID);
                    //TODO: Add other release functions
                    ChatUtils.releaseMessage(sender, peacekeeper.userTable.getUsername(playerID));
                }
            }
        });
        return true;
    }
}
