package com.gmail.sintinium.peacekeeper.data;

import java.util.UUID;

public class PlayerData {

    public Integer playerID;
    public String username;
    public UUID uuid;
    public String ip;

    public PlayerData(Integer playerID, String username, UUID uuid, String ip) {
        this.playerID = playerID;
        this.username = username;
        this.uuid = uuid;
        this.ip = ip;
    }

}
