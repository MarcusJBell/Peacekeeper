package com.gmail.sintinium.peacekeeper.data;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.ChatColor;

public class WarnData {

    public int warnID;
    private long warnTime;
    private int playerID;
    private String reason;
    private Integer adminID;
    private int recordID;

    public WarnData(int warnID, long warnTime, int playerID, String reason, Integer adminID, int recordID) {
        this.warnID = warnID;
        this.warnTime = warnTime;
        this.playerID = playerID;
        this.reason = reason;
        this.adminID = adminID;
        this.recordID = recordID;
    }

    public String generateWarnMessage(Peacekeeper peacekeeper) {
        return ChatColor.YELLOW + "WARNING: " + ChatColor.DARK_AQUA + "You have received a warning from: " + peacekeeper.userTable.getUsername(this.adminID) + "\n" +
                ChatColor.GOLD + "Message: " + ChatColor.YELLOW + this.reason;
    }

}
