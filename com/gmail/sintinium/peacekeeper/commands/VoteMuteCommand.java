package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.VoteMuteData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class VoteMuteCommand extends BaseCommand {

    public ConcurrentHashMap<String, VoteMuteData> voteMutes;

    VoteMuteCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
        voteMutes = new ConcurrentHashMap<>();
    }

    public static void broadcastVote(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("peacekeeper.commands.votemute")) {
                p.sendMessage(message);
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        return false;
    }

}
