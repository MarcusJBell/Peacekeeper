package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PeacekeeperCommand extends BaseCommand {

    public PeacekeeperCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if (args.length < 1) {
            usage(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("peacekeeper.command.peacekeeper.reload")) {
                sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use this command.");
                return true;
            }
            if (sender instanceof Player) {
                if (!peacekeeper.conversationListener.conversations.containsKey(sender)) {
                    sender.sendMessage(ChatColor.DARK_AQUA + "Configuration reloaded");
                }
            } else
                sender.sendMessage(ChatColor.DARK_AQUA + "Configuration reloaded");
            peacekeeper.loadConfig();
            return true;
        }

        String[] _args = new String[args.length - 1];
        System.arraycopy(args, 1, _args, 0, args.length - 1);

        if (args[0].equalsIgnoreCase("sv")) {
            if (peacekeeper.commandManager.superVanishCommand.onCommand(sender, command, s, _args)) {
                return true;
            }
        }

        if (args[0].equalsIgnoreCase("pt")) {
            if (peacekeeper.commandManager.powerToolCommand.onCommand(sender, command, s, args)) {
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
