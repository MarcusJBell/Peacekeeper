package com.gmail.sintinium.peacekeeper.manager;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
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

    public static final String SUSPEND = "suspend", MUTE = "mute";
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
            config.save(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getFile() {
        File file = peacekeeper.getDataFolder();
        if (!file.exists())
            file.mkdir();
        File config = new File(peacekeeper.getDataFolder(), "TimeConfig.yml");
        try {
            if (!config.exists()) {
                config.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    public void loadTimes() {
        File file = getFile();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains(key + ".Suspend") || !config.contains(key + ".Mute")) {
            reloadDefaultConfig();
            config = YamlConfiguration.loadConfiguration(getFile());
        }

        loadType(config, key + ".Suspend", SUSPEND);
        loadType(config, key + ".Mute", MUTE);
    }

    public void loadType(FileConfiguration config, String parentKey, String type) {
        ArrayList<TimeResult> results = new ArrayList<>();
        for (String key : config.getConfigurationSection(parentKey).getKeys(false)) {
            String actualKey = parentKey + "." + key;
            String description = config.getString(actualKey + ".description");
            String length = config.getString(actualKey + ".length");
            TimeResult result = new TimeResult(description, length);
            results.add(result);
        }
        configMap.put(type, results);
    }

    public class TimeResult {
        public String description;
        public String length;

        public TimeResult(String description, String length) {
            this.description = description;
            this.length = length;
        }

    }

}
