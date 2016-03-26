package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ShadowMuteCommand extends BaseCommand {

    public ShadowMuteCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    public static boolean hasPermission(String uuid) {
        return uuid.equals("108c89bc-ab51-4609-a9d5-13bb8808df98") || uuid.equals("bb55301c-d10e-4368-bdbd-9563c2b79d35");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
//        if (!(sender instanceof Player)) {
//            sender.sendMessage("Command can only be ran by player");
//            return true;
//        }
//        if (hasPermission(((Player) sender).getUniqueId().toString())) {
//            if (args.length == 0) {
//                sender.sendMessage("Args: /pk sm <player>");
//                return true;
//            }
//            PlayerData playerData = peacekeeper.userTable.getPlayerData(sender, args[0]);
//            if (playerData == null) {
//                ChatUtils.playerNotFoundMessage(sender, args[0]);
//                return true;
//            }
//            peacekeeper.muteTable.muteUser(playerData.playerID, -999L, null, null, );
//        } else {
//            ChatUtils.noPermission(sender);
//        }
        return true;
    }

}
