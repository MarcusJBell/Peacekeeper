package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.PlayerData;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShadowBanCommand extends BaseCommand {

    public ShadowBanCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    private static boolean hasPermission(String uuid) {
        return uuid.equals("108c89bc-ab51-4609-a9d5-13bb8808df98") || uuid.equals("bb55301c-d10e-4368-bdbd-9563c2b79d35");
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String s, final String[] args) {
        if (hasPermission(((Player) sender).getUniqueId().toString())) {
            if (args.length == 0) {
                sender.sendMessage("Args: /pk sb <player>");
                return true;
            }

            peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
                @Override
                public void runTask() {
                    PlayerData data = peacekeeper.userTable.getPlayerData(sender, args[0]);
                    if (data == null) {
                        ChatUtils.playerNotFoundMessage(sender, args[0]);
                    }

                }
            });

        } else {
            return false;
        }
        return true;
    }

}
