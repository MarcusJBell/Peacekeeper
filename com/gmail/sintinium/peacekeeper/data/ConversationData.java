package com.gmail.sintinium.peacekeeper.data;

import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;

import java.util.ArrayList;
import java.util.List;

public class ConversationData {

    public List<String> strings;
    public List<String> missedMessages;
    public ConversationListener.ConversationType conversationType;

    public String reason;
    public String punishedUUID, punishedUsername;
    public Integer playerID, adminID;

    public boolean banConversation = false, muteConversation = false;

    public ConversationData(List<String> strings, ConversationListener.ConversationType conversationType) {
        this.strings = strings;
        this.conversationType = conversationType;
        missedMessages = new ArrayList<>();
    }

    public void setupSuspendConversation(Integer playerID, String reason, String punishedUsername) {
        this.playerID = playerID;
        this.reason = reason;
        this.punishedUsername = punishedUsername;
        banConversation = true;
    }

    public void setupMuteConversation(Integer playerID, String reason, String uuid) {
        this.playerID = playerID;
        this.reason = reason;
        this.punishedUUID = uuid;
        this.muteConversation = true;
    }

}
