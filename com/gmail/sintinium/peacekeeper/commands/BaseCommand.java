package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

abstract class BaseCommand implements CommandExecutor {

    Peacekeeper peacekeeper;

    BaseCommand(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    @Override
    public abstract boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings);

}
