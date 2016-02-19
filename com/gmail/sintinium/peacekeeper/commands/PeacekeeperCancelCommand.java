package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PeacekeeperCancelCommand extends BaseCommand {

    public PeacekeeperCancelCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) {
            if (peacekeeper.conversationListener.conversations.containsKey(sender)) {
                peacekeeper.conversationListener.cancelConversation((Player) sender);
            } else {
                ChatUtils.clearChat(sender);
                sender.sendMessage("You are not in a conversation.");
            }
        } else {
            sender.sendMessage("Command can only be ran by player");
        }
        return true;
    }

}
