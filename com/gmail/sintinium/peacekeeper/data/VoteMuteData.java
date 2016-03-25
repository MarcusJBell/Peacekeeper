package com.gmail.sintinium.peacekeeper.data;

import com.google.common.collect.Sets;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VoteMuteData {

    public int count;
    public Set<String> votedPlayers;

    public VoteMuteData() {
        votedPlayers = Sets.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    }

}
