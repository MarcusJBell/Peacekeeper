package com.gmail.sintinium.peacekeeper.listeners;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.event.Listener;

public class ChatSpamFilterListener implements Listener {

    //Types: blocking, filtering, off
    public String capType = "blocking", spamType = "blocking", excessiveCharType = "blocking";
    //Percentage threshold for filter to kick in
    public float caps = .90f, spam = .75f;
    //Amount of repeated characters for filter to pick up
    public int excessiveCharCount = 6;

    private Peacekeeper peacekeeper;

    public ChatSpamFilterListener(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }
}
