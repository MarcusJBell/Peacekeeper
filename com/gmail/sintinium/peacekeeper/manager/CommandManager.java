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
    public PeacekeeperCommand peacekeeperCommand;
    public MuteCommand muteCommand;
    public SuspendCommand suspendCommand;
    public ReportCommand reportCommand;
    public VanishCommand vanishCommand;
    public SuperVanishCommand superVanishCommand;
    public PowerToolCommand powerToolCommand;
    public WarnCommand warnCommand;
    public VoteMuteCommand voteMuteCommand;
    private Peacekeeper peacekeeper;

    public CommandManager(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        this.commands = new HashSet<>();
    }

    public void registerDefaults() {
        registerCommand("release", new ReleaseCommand(peacekeeper));
        registerCommand("mute", muteCommand = new MuteCommand(peacekeeper));
        registerCommand("suspend", suspendCommand = new SuspendCommand(peacekeeper));
        registerCommand("report", reportCommand = new ReportCommand(peacekeeper));
        registerCommand("viewreports", new ViewReportsCommand(peacekeeper));
        registerCommand("ban", new PermBanCommand(peacekeeper));
        registerCommand("banip", new IPBanCommand(peacekeeper));
        registerCommand("playerinfo", new PlayerInfoCommand(peacekeeper));
        registerCommand("records", new RecordsCommand(peacekeeper));
        registerCommand("warn", warnCommand = new WarnCommand(peacekeeper));
//        registerCommand("vanish", vanishCommand = new VanishCommand(peacekeeper));
        vanishCommand = new VanishCommand(peacekeeper);
        registerCommand("peacekeeper", peacekeeperCommand = new PeacekeeperCommand(peacekeeper));
//        registerCommand("supervanish", superVanishCommand = new SuperVanishCommand(peacekeeper));
        superVanishCommand = new SuperVanishCommand(peacekeeper);
        powerToolCommand = new PowerToolCommand(peacekeeper);
        voteMuteCommand = new VoteMuteCommand(peacekeeper);
    }

    public void registerCommand(String commandName, CommandExecutor executor) {
        PluginCommand command = peacekeeper.getCommand(commandName);
        if (command == null) {
            peacekeeper.getLogger().warning("Could not register command.. " + commandName);
            return;
        }
        command.setExecutor(executor);
        command.setPermissionMessage(ChatColor.DARK_RED + "You do not have permission to use this command.");
        commands.add(command);
    }
}
