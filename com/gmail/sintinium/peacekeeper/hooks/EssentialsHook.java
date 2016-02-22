package com.gmail.sintinium.peacekeeper.hooks;

import com.earth2me.essentials.Essentials;
import com.gmail.sintinium.peacekeeper.Peacekeeper;

public class EssentialsHook {

    public Peacekeeper peacekeeper;
    public Essentials essentials;

    public EssentialsHook(Peacekeeper peacekeeper) {
        essentials = (Essentials) peacekeeper.getServer().getPluginManager().getPlugin("Essentials");
        if (essentials == null) return;
        this.peacekeeper = peacekeeper;
    }

}
