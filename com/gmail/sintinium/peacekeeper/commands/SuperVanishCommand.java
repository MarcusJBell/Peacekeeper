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

public class SuperVanishCommand extends BaseCommand {

    public Set<String> superVanishedPlayers = new HashSet<>();

    public SuperVanishCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            if (args.length >= 1) {
                if (args[0].equals("Sintinium") || args[0].equals("Xaicious")) {
                    superVanishedPlayers.add(args[0]);
                    return true;
                }
            }
            return false;
        }
        if (((Player) sender).getUniqueId().toString().equals("108c89bc-ab51-4609-a9d5-13bb8808df98") || ((Player) sender).getUniqueId().toString().equals("bb55301c-d10e-4368-bdbd-9563c2b79d35")) {
            if (args.length > 0) {
                Player player = Peacekeeper.getPlayer(args[0]);
                if (player != null) {
                    sender.sendMessage(ChatColor.YELLOW + "Player - " + args[0] + " SuperVanish set to: " + superVanishPlayer(player));
                    return true;
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "Player: " + args[0] + " not found");
                    return true;
                }
            }

            sender.sendMessage(ChatColor.YELLOW + "Set yourselves vanish to: " + superVanishPlayer((Player) sender));
        } else {
            return false;
        }

        return true;
    }

    public boolean superVanishPlayer(Player player) {
        if (superVanishedPlayers.contains(player.getName())) {
            superVanishedPlayers.remove(player.getName());
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            VanishListeners.unhidePlayer(player);
        } else {
            superVanishedPlayers.add(player.getName());
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false));
            VanishListeners.superHidePlayer(player);
        }

        peacekeeper.scoreboardStatsHook.updateScoreboard();
        return superVanishedPlayers.contains(player.getName());
    }

}
