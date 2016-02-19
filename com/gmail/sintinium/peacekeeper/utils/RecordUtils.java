package com.gmail.sintinium.peacekeeper.utils;

import com.gmail.sintinium.peacekeeper.Peacekeeper;

public class RecordUtils {

    public static boolean isPunished(Peacekeeper peacekeeper, int playerID) {
        return peacekeeper.banTable.isPlayerBanned(playerID) || peacekeeper.banTable.isIPBanned(peacekeeper.userTable.getIP(playerID)) || peacekeeper.muteTable.isPlayerMuted(playerID);
    }

}
