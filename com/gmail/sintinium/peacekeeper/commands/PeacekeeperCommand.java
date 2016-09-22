package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PeacekeeperCommand extends BaseCommand {

    public HashMap<Player, Runnable> confirmDatas = new HashMap<>();

    public PeacekeeperCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length < 1) {
            usage(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("confirm")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.DARK_RED + "This command can only be ran by players");
                return true;
            }
            Player p = (Player) sender;
            if (confirmDatas.containsKey(p)) {
                confirmDatas.get(p).run();
                confirmDatas.remove(p);
                peacekeeper.conversationListener.confirmOrCancel(p);
                sender.sendMessage(ChatColor.GREEN + "---- Confirmed ----");
            } else {
                sender.sendMessage(ChatColor.YELLOW + "You currently don't have any punishments to confirm.");
            }
            return true;
        } else if (sender instanceof Player && args[0].equalsIgnoreCase("cancel") && confirmDatas.containsKey(sender)) {
            sender.sendMessage(ChatColor.RED + "---- Cancelled ----");
            peacekeeper.conversationListener.confirmOrCancel((Player) sender);
            confirmDatas.remove(sender);
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

        usage(sender);
        return true;
    }

    private void usage(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_RED + "Usage: /pk reload to reload config files");
        sender.sendMessage(ChatColor.DARK_RED + "Usage: /pk confirm to confirm punishment");
    }

}
