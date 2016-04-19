package com.gmail.sintinium.peacekeeper.manager;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TimeManager {

    public static final String SUSPEND = "suspend", MUTE = "mute", REPORT = "report";
    public Map<String, ArrayList<TimeResult>> configMap;
    private Peacekeeper peacekeeper;
    private String key = "Types.";

    public TimeManager(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        configMap = new HashMap<>();
    }

    public void reloadDefaultConfig() {
        try {
            Reader defConfigStream = new InputStreamReader(peacekeeper.getResource("TimeConfig.yml"), "UTF8");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(defConfigStream);
            File file = new File(peacekeeper.getDataFolder(), "TimeConfig.yml");
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        File file = peacekeeper.getDataFolder();
        if (!file.exists())
            file.mkdir();
        File config = new File(peacekeeper.getDataFolder(), "TimeConfig.yml");
        if (!config.exists()) {
            reloadDefaultConfig();
        }
        return config;
    }

    public void loadTimes() {
        try {
            File file = getFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            loadType(config, key + ".Suspend", SUSPEND, true);
            loadType(config, key + ".Mute", MUTE, true);
            loadType(config, key + ".Report", REPORT, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadType(FileConfiguration config, String parentKey, String type, boolean time) {
        ArrayList<TimeResult> results = new ArrayList<>();
        for (String key : config.getConfigurationSection(parentKey).getKeys(false)) {
            String actualKey = parentKey + "." + key;
            String description = config.getString(actualKey + ".description");
            String length = null;
            if (time)
                length = config.getString(actualKey + ".length");
            boolean warn = false;
            if (config.contains(actualKey + ".warn")) {
                warn = config.getBoolean(actualKey + ".warn");
            }
            TimeResult result = new TimeResult(description, length);
            result.shouldWarn = warn;
            results.add(result);
        }
        configMap.put(type, results);
    }

    public class TimeResult {
        public String description;
        public String length;
        public long timeLength;
        public boolean shouldWarn;

        public TimeResult(String description, String length) {
            this.description = description;
            this.length = length;
            timeLength = TimeUtils.stringToMillis(length);
        }

    }

}
