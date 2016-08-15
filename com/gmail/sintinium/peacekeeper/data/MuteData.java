package com.gmail.sintinium.peacekeeper.data;

public class MuteData {

    public Integer muteID;
    public Long muteTime;
    public Integer mutedUser;
    public Integer adminId;
    public Long muteLength;
    public Integer recordId;
    public String adminName = null;
    public long lastTime;
    private String reason;

    public MuteData(String adminName, Integer muteID, Long muteTime, Integer mutedUser, String reason, Integer adminId, Long muteLength, Integer recordId) {
        this.adminName = adminName;
        this.muteID = muteID;
        this.muteTime = muteTime;
        this.mutedUser = mutedUser;
        this.reason = reason;
        this.adminId = adminId;
        this.muteLength = muteLength;
        this.recordId = recordId;
    }
}
