package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class PeacekeeperCommand extends BaseCommand {

    public PeacekeeperCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 1) {
            return false;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("peacekeeper.command.peacekeeper.reload")) {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use this command.");
                return true;
            }
            peacekeeper.reloadConfig();
            sender.sendMessage(ChatColor.DARK_AQUA + "Configuration reloaded");
            return true;
        }

        if (args[0].equalsIgnoreCase("sv")) {
            String[] _args = new String[args.length - 1];
            System.arraycopy(args, 1, _args, 0, args.length - 1);
            if (!peacekeeper.commandManager.superVanishCommand.onCommand(sender, command, s, _args)) {
                usage(sender);
                return true;
            } else {
                return true;
            }
        }
        usage(sender);
        return true;
    }

    public void usage(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_RED + "Usage: /pk reload to reload config files");
    }

}
