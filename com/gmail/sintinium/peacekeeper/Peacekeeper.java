package com.gmail.sintinium.peacekeeper;

import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.data.MuteData;
import com.gmail.sintinium.peacekeeper.data.conversation.ConversationData;
import com.gmail.sintinium.peacekeeper.db.tables.*;
import com.gmail.sintinium.peacekeeper.hooks.EssentialsHook;
import com.gmail.sintinium.peacekeeper.hooks.ScoreboardStatsHook;
import com.gmail.sintinium.peacekeeper.io.ConfigFile;
import com.gmail.sintinium.peacekeeper.io.LogFile;
import com.gmail.sintinium.peacekeeper.listeners.*;
import com.gmail.sintinium.peacekeeper.manager.CommandManager;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;
import com.gmail.sintinium.peacekeeper.queue.DatabaseQueueManager;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.BanUtils;
import com.gmail.sintinium.peacekeeper.utils.PunishmentHelper;
import com.gmail.sintinium.peacekeeper.utils.jsonchat.JsonChat;
import lib.PatPeter.SQLibrary.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Peacekeeper extends JavaPlugin {

    public static String appealUrl;
    public static LogFile logFile;
    public SQLite database;
    public UserTable userTable;
    public PlayerReportTable reportTable;
    public PlayerRecordTable recordTable;
    public PlayerMuteTable muteTable;
    public PlayerBanTable banTable;
    public CommandManager commandManager;
    public TimeManager timeManager;
    public PunishmentHelper punishmentHelper;
    public ConversationListener conversationListener;
    public MuteListener muteListener;
    public BanListener banListener;
    public DatabaseQueueManager databaseQueueManager;
    public ScoreboardStatsHook scoreboardStatsHook;
    public EssentialsHook essentialsHook;
    public ConfigFile configFile;
    public JsonChat jsonChat;

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
        return Bukkit.getServer().getPlayerExact(name);
    }

    @Override
    public void onEnable() {
        getServer().getScheduler().cancelTasks(this);
        if (!loadDependencies()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        punishmentHelper = new PunishmentHelper(this);
        commandManager = new CommandManager(this);
        commandManager.registerDefaults();
        databaseQueueManager = new DatabaseQueueManager(this);

        registerListeners();
        loadConfig();
        loadDatabase();
        if (!loadCompatibilities()) {
            databaseQueueManager.queue.clear();
            databaseQueueManager.bukkitTask.cancel();
            return;
        }

        databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                initializeTables();
            }
        });
    }

    @Override
    public void onDisable() {
        getLogger().info("Finishing up tasks...");
        long time = System.currentTimeMillis();
        boolean warned = false;
        databaseQueueManager.running = false;
        synchronized (databaseQueueManager.thread) {
            databaseQueueManager.thread.notify();
        }
        while (!databaseQueueManager.closed) {
            if (!warned && System.currentTimeMillis() - time > 5000) {
                Bukkit.getConsoleSender().sendMessage("Taking longer than expected...");
                Bukkit.getConsoleSender().sendMessage("Trying for 20 seconds before force shutdown");
                warned = true;
            }
            if (System.currentTimeMillis() - time > 20000) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        getServer().getScheduler().cancelTasks(this);
        database.close();
    }

    public boolean loadCompatibilities() {
        String packageName = getServer().getClass().getPackage().getName();
        String version = packageName.substring(packageName.lastIndexOf('.') + 1);
        try {
            final Class<?> clazz = Class.forName("com.gmail.sintinium.peacekeeper.utils.jsonchat." + version + ".JsonChat");
            if (JsonChat.class.isAssignableFrom(clazz)) {
                this.jsonChat = (JsonChat) clazz.getConstructor().newInstance();
            }
        } catch (Exception e) {
            e.printStackTrace();
            getLogger().severe("Could not find support for this CraftBukkit version");
            setEnabled(false);
            return false;
        }
        getLogger().info("Loading support for " + version);
        return true;
    }

    public void loadConfig() {
        configFile = new ConfigFile(this);
        configFile.loadConfiguration();
        timeManager = new TimeManager(this);
        timeManager.loadTimes();
        logFile = new LogFile(this);
        if (conversationListener != null && !conversationListener.conversations.isEmpty()) {
            for (Map.Entry<Player, ConversationData> set : conversationListener.conversations.entrySet()) {
                conversationListener.syncCancel(set.getKey());
                set.getKey().sendMessage(ChatColor.DARK_AQUA + "Configuration reloaded");
            }
        }

        //TODO: Get this from config file
        appealUrl = "http://amitycrafters.enjin.com/banappeals";
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
        reportTable = new PlayerReportTable(this);
        muteTable = new PlayerMuteTable(this);
    }

    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(banListener = new BanListener(this), this);
        Bukkit.getPluginManager().registerEvents(new JoinListener(this), this);
        Bukkit.getPluginManager().registerEvents(new VanishListeners(this), this);
        Bukkit.getPluginManager().registerEvents(muteListener = new MuteListener(this), this);
        Bukkit.getPluginManager().registerEvents(conversationListener = new ConversationListener(this), this);
    }

    public boolean loadDependencies() {
        if (getServer().getPluginManager().getPlugin("SQLibrary") == null) {
            getLogger().log(Level.SEVERE, "COULDN'T LOAD SQLIBRARY FOR DATABASE PLEASE INSURE YOU HAVE THE PLUGIN INSTALLED.");
            return false;
        }
        scoreboardStatsHook = new ScoreboardStatsHook(this);
        scoreboardStatsHook.loadPlugin();
        essentialsHook = new EssentialsHook(this);
        return true;
    }

    public MuteData handleMute(int playerID) {
        Integer muteID = muteTable.getMuteIDFromPlayerID(playerID);
        if (muteID != null) {
            MuteData muteData = muteTable.muteData(muteID);
            if (muteData != null) {
                if (muteData.muteLength != null && (muteData.muteTime + muteData.muteLength) - System.currentTimeMillis() <= 0) {
                    muteTable.unmutePlayer(muteData.mutedUser);
                }
            }
            return muteData;
        }
        return null;
    }

    // Handles a ban/Updates ban info and returns the BanData
    public BanData handleBan(int playerID) {
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
