package com.gmail.sintinium.peacekeeper.io;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigFile {

    Peacekeeper peacekeeper;

    public ConfigFile(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    public void loadConfiguration() {
        File file = peacekeeper.getDataFolder();
        File conf = new File(file, "config.yml");
        try {
            if (!conf.exists()) {
                file.mkdirs();
                conf.createNewFile();
                peacekeeper.getConfig().set("Peacekeeper.AppealURL", "examp.le/appeals");
                peacekeeper.getConfig().save(conf);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileConfiguration config = peacekeeper.getConfig();
        Peacekeeper.appealUrl = config.getString("Peacekeeper.AppealURL");
    }

}
