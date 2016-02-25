package com.gmail.sintinium.peacekeeper.manager;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.commands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;

import java.util.HashSet;
import java.util.Set;

public class CommandManager {

    public Set<PluginCommand> commands;
    public MuteCommand muteCommand;
    public SuspendCommand suspendCommand;
    public VanishCommand vanishCommand;
    public SuperVanishCommand superVanishCommand;
    private Peacekeeper peacekeeper;

    public CommandManager(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        this.commands = new HashSet<>();
    }

    public void registerDefaults() {
        registerCommand("peacekeepercancel", new PeacekeeperCancelCommand(peacekeeper));
        registerCommand("release", new ReleaseCommand(peacekeeper));
        registerCommand("suspend", suspendCommand = new SuspendCommand(peacekeeper));
        registerCommand("mute", muteCommand = new MuteCommand(peacekeeper));
        registerCommand("ipban", new IPBanCommand(peacekeeper));
        registerCommand("playerinfo", new PlayerInfoCommand(peacekeeper));
        registerCommand("vanish", vanishCommand = new VanishCommand(peacekeeper));
        registerCommand("supervanish", superVanishCommand = new SuperVanishCommand(peacekeeper));
    }

    public void registerCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = peacekeeper.getCommand(commandName);
        command.setExecutor(executor);
        command.setPermissionMessage(ChatColor.DARK_RED + "You do not have permission to use this command.");
        commands.add(command);
    }
}
