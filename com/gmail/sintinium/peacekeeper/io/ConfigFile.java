package com.gmail.sintinium.peacekeeper.io;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigFile {

    Peacekeeper peacekeeper;

    public ConfigFile(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    public void loadConfiguration() {
        FileConfiguration config = peacekeeper.getConfig();
        Peacekeeper.appealUrl = config.getString("Peacekeeper.AppealURL");
    }

}
