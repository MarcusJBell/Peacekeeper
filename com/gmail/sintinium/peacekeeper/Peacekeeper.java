package com.gmail.sintinium.peacekeeper;

import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.db.tables.*;
import com.gmail.sintinium.peacekeeper.hooks.EssentialsHook;
import com.gmail.sintinium.peacekeeper.hooks.ProtocolLib;
import com.gmail.sintinium.peacekeeper.hooks.ScoreboardStatsHook;
import com.gmail.sintinium.peacekeeper.hooks.SuperTrailsHook;
import com.gmail.sintinium.peacekeeper.io.ConfigFile;
import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.listeners.JoinListener;
import com.gmail.sintinium.peacekeeper.listeners.MuteListener;
import com.gmail.sintinium.peacekeeper.listeners.VanishListeners;
import com.gmail.sintinium.peacekeeper.manager.CommandManager;
import com.gmail.sintinium.peacekeeper.queue.DatabaseQueueManager;
import com.gmail.sintinium.peacekeeper.utils.BanUtils;
import com.gmail.sintinium.peacekeeper.utils.PunishmentHelper;
import lib.PatPeter.SQLibrary.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peacekeeper extends JavaPlugin {

    public static String appealUrl;
    public SQLite database;
    public UserTable userTable;
    public PlayerReportTable reportTable;
    public PlayerRecordTable recordTable;
    public PlayerMuteTable muteTable;
    public PlayerBanTable banTable;
    public CommandManager commandManager;
    public PunishmentHelper punishmentHelper;

    public ConversationListener conversationListener;
    public DatabaseQueueManager databaseQueueManager;

    public EssentialsHook essentialsHook;
    public SuperTrailsHook superTrailsHook;
    public ScoreboardStatsHook scoreboardStatsHook;

    public ConfigFile configFile;

    // Gets online players from incomplete username. Ex: If the player Sintinium is online and the CommandSender types 'Sint' it will return Sintinium
    public static Player getPlayer(String name) {
        List<Player> players = Bukkit.getServer().matchPlayer(name);
        if (players.isEmpty()) {
            return null;
        } else {
            return players.get(0);
        }
    }

    public static Player getExactPlayer(String name) {
        return Bukkit.getServer().getPlayer(name);
    }

    @Override
    public void onEnable() {
        if (!loadDependencies()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        punishmentHelper = new PunishmentHelper(this);
        commandManager = new CommandManager(this);
        commandManager.registerDefaults();
        databaseQueueManager = new DatabaseQueueManager(this);

        configFile = new ConfigFile(this);
        configFile.loadConfiguration();

        registerListeners();
        loadDatabase();
        initializeTables();
    }

    @Override
    public void onDisable() {
        getLogger().info("Finishing up tasks...");
        long closeTime = System.currentTimeMillis();
        boolean messaged = false;
        while (!databaseQueueManager.canExit()) {
            if (System.currentTimeMillis() - closeTime >= 20000) {
                getLogger().log(Level.SEVERE, "Couldn't finish task queue, force closing database thread.");
                break;
            }
            if (System.currentTimeMillis() - closeTime >= 5000 && !messaged) {
                getLogger().info("Finishing tasks taking longer than expected...");
                messaged = true;
            }
        }
        database.close();
    }

    // Load database from file
    public void loadDatabase() {
        database = new SQLite(Logger.getLogger("Minecraft"), "[Peacekeeper]", getDataFolder().getAbsolutePath(), "Peacekeeper");
        try {
            database.open();
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().info("Shutting down Peacekeeper, reason: couldn't open SQLite database connection");
            getPluginLoader().disablePlugin(this);
        }
    }

    public void initializeTables() {
        userTable = new UserTable(this);
        banTable = new PlayerBanTable(this);
        recordTable = new PlayerRecordTable(this);
        muteTable = new PlayerMuteTable(this);
    }

    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VanishListeners(this), this);
        Bukkit.getPluginManager().registerEvents(new MuteListener(this), this);
        Bukkit.getPluginManager().registerEvents(conversationListener = new ConversationListener(this), this);
    }

    public boolean loadDependencies() {
        if (getServer().getPluginManager().getPlugin("SQLibrary") == null) {
            getLogger().log(Level.SEVERE, "COULDN'T LOAD SQLIBRARY FOR DATABASE PLEASE INSURE YOU HAVE THE PLUGIN INSTALLED.");
            return false;
        } else if (getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().log(Level.SEVERE, "COULDN'T LOAD PROTOCOLLIB PLEASE INSURE YOU HAVE THE PLUGIN INSTALLED.");
            return false;
        }

        ProtocolLib.setupProtocolLib(this);
        scoreboardStatsHook = new ScoreboardStatsHook(this);
        scoreboardStatsHook.loadPlugin();
        return true;
    }

    // Handles a ban/Updates ban info and returns the BanData
    public BanData handleBan(int playerID) {
        //TODO: Get appeal url from yml
        BanData highestBan = BanUtils.getHighestBan(this, playerID);
        if (highestBan == null) return null;

        if (highestBan.banLength != null && (highestBan.banTime + highestBan.banLength) - System.currentTimeMillis() <= 0) {
            this.banTable.deleteBan(highestBan.banID);
            return handleBan(playerID);
        }
        return highestBan;
    }

    // Handles ban by getting player's ID from player object
    public BanData handleBan(@Nonnull Player player) {
        if (!userTable.doesPlayerExist(player)) return null;
        Integer playerID = userTable.getPlayerIDFromUUID(player.getUniqueId().toString());
        if (playerID == null) {
            return null;
        }
        return this.handleBan(playerID);
    }

}
