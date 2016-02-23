package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.listeners.VanishListeners;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public class VanishCommand extends BaseCommand {

    public Set<String> vanishedPlayers = new HashSet<>();

    public VanishCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length > 0) {
            Player player = Peacekeeper.getPlayer(args[0]);
            if (player != null) {
                boolean state = vanishPlayer(player);
                sender.sendMessage(ChatColor.YELLOW + "Player - " + args[0] + " is now " + (state ? "Vanished" : "Un-Vanished"));
                return true;
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Player: " + args[0] + " not found");
                return true;
            }
        }

        if (sender instanceof Player) {
            boolean state = vanishPlayer((Player) sender);
            sender.sendMessage(ChatColor.YELLOW + "You are now " + (state ? "Vanished" : "Un-Vanished"));
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Must be a player to use this command");
        }

        return true;
    }

    public boolean vanishPlayer(Player player) {
        if (vanishedPlayers.contains(player.getName())) {
            vanishedPlayers.remove(player.getName());
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            VanishListeners.unhidePlayer(player);
        } else {
            vanishedPlayers.add(player.getName());
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
            VanishListeners.hidePlayer(player);
        }
        return vanishedPlayers.contains(player.getName());
    }

}
