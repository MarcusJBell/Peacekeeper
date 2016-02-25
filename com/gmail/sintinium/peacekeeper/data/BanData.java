package com.gmail.sintinium.peacekeeper.data;

public class BanData {

    public Integer banID;
    public Long banTime;
    public Integer bannedUser;
    public String ip;
    public String reason;
    public Integer adminId;
    public Long banLength;
    public Integer type;
    public Integer recordId;

    public String adminUsername;

    public BanData(Integer banID, Long banTime, Integer bannedUser, String ip, String reason, Integer adminId, Long banLength, Integer type, Integer recordId) {
        this.banID = banID;
        this.banTime = banTime;
        this.bannedUser = bannedUser;
        this.ip = ip;
        this.reason = reason;
        this.adminId = adminId;
        this.banLength = banLength;
        this.type = type;
        this.recordId = recordId;
    }

    public BanData cloneData() {
        return new BanData(banID, banTime, bannedUser, ip, reason, adminId, banLength, type, recordId);
    }

}
