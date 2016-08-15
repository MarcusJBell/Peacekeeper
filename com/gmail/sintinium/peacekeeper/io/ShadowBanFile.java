package com.gmail.sintinium.peacekeeper.io;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class ShadowBanFile {

    private Set<String> banned;
    private Peacekeeper peacekeeper;

    public ShadowBanFile(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        banned = new HashSet<>();
        loadBans();
    }

    private void reloadDefaultConfig() {
        try {
            Reader defConfigStream = new InputStreamReader(peacekeeper.getResource("Shadowbanned.yml"), "UTF8");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(defConfigStream);
            File file = new File(peacekeeper.getDataFolder(), "Shadowbanned.yml");
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getFile() {
        File file = peacekeeper.getDataFolder();
        if (!file.exists())
            file.mkdir();
        File config = new File(peacekeeper.getDataFolder(), "Shadowbanned.yml");
        if (!config.exists()) {
            reloadDefaultConfig();
        }
        return config;
    }

    public void shadowBan(String playerUUID) {
        banned.add(playerUUID);
        saveBans();
    }

    private void loadBans() {
        File file = getFile();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        if (!config.contains("shadowbanned.uuid")) return;
        banned = new HashSet<>(config.getStringList("shadowbanned.uuid"));
    }

    private void saveBans() {
        File file = getFile();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("shadowbanned.uuid", new ArrayList<>(banned));

        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
