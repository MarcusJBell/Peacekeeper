package com.gmail.sintinium.peacekeeper.utils;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.BanData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerBanTable;

public class BanUtils {

    // Creates the ban message the player will see when they are kicked from the server
    public static String generateBanMessage(Peacekeeper peacekeeper, BanData banData) {
        String message = "";
        if (banData.type == null) {
            return "";
        }
        String adminName;
        if (banData.adminId == null) {
            adminName = "AutoModerator";
        } else {
            adminName = peacekeeper.userTable.getUsername(banData.adminId);
        }
        if (banData.type == PlayerBanTable.PLAYER) {
            if (banData.banLength == null) {
                message += "§4You have been permanently banned from this server by: " + "§4" + adminName + "\n";
                message += "§4Reason: §e" + banData.reason + "\n";
            } else {
                message += "§4You've been suspended from this server by: " + "§4" + adminName + "\n";
                message += "§4Reason: §e" + banData.reason + "\n";
                message += "§4Your suspension will end in: ";
                message += "§4" + TimeUtils.millsToString((banData.banTime + banData.banLength) - System.currentTimeMillis()) + "\n";
            }
        } else if (banData.type == PlayerBanTable.IP) {
            message += "§4You're IP Banned from this server by: " + "§4" + adminName + "\n";
            message += "§4Reason: §e" + banData.reason + "\n";
        }

        if (Peacekeeper.appealUrl != null)
            message += "§eAppeal at: " + Peacekeeper.appealUrl;
        return message;
    }

    // Creates the ban message the player will see when they are kicked from the server that doesn't need to be on the sql thread
    public static String generateSyncedBanMessage(BanData banData, String adminName) {
        String message = "";
        if (adminName == null) {
            adminName = "AutoModerator";
        }
        if (banData.type == null) {
            return "";
        }
        if (banData.type == PlayerBanTable.PLAYER) {
            if (banData.banLength == null) {
                message += "§4You have been suspended until approved appeal by: " + "§4" + adminName + "\n";
                message += "§4Reason: §e" + banData.reason + "\n";
            } else {
                message += "§4You have been suspended from this server by: " + "§4" + adminName + "\n";
                message += "§4Reason: §e" + banData.reason + "\n";
                message += "§4Your suspension will end in: ";
                message += "§4" + TimeUtils.millsToString((banData.banTime + banData.banLength) - System.currentTimeMillis()) + "\n";
            }
        } else if (banData.type == PlayerBanTable.IP) {
            message += "§4You have been IP Banned from this server by: " + "§4" + adminName + "\n";
            message += "§4Reason: §e" + banData.reason + "\n";
        }

        if (Peacekeeper.appealUrl != null)
            message += "§eAppeal at: " + Peacekeeper.appealUrl;
        return message;
    }

    // Gets highest ban. Ex. If it's an IP ban they will see that instead of a 1d suspension
    public static BanData getHighestBan(Peacekeeper peacekeeper, int playerID) {
        if (!peacekeeper.userTable.doesPlayerExist(playerID)) return null;
        int[] bans = peacekeeper.banTable.getPlayersBans(playerID, peacekeeper.userTable.getIP(playerID));
        if (bans == null || bans.length == 0) return null;
        int highest = -1;
        BanData highestBan = null;
        for (int i : bans) {
            BanData banData = peacekeeper.banTable.getBanData(i);
            Integer priority = banData.type;
            if (priority == null) continue;
            if (priority > highest) {
                highestBan = banData;
            }
        }

        return highestBan;
    }

}
