package com.gmail.sintinium.peacekeeper.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.plugin.Plugin;

public class EssentialsHook {

    public Essentials essentials = null;

    public EssentialsHook(Peacekeeper peacekeeper) {
        Plugin plugin = peacekeeper.getServer().getPluginManager().getPlugin("Essentials");
        if (plugin != null) {
            essentials = (Essentials) plugin;
        }
    }

    public void updateUser(String name) {
        User user = essentials.getUser(name);
        user.updateActivity(true);
    }

}
