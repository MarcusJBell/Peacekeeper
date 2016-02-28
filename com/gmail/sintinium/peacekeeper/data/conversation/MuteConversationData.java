package com.gmail.sintinium.peacekeeper.data.conversation;

import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;

import java.util.List;

public class MuteConversationData extends ConversationData {
    public MuteConversationData(List<TimeManager.TimeResult> results, ConversationListener.ConversationType conversationType, String header) {
        super(results, conversationType, header);
    }

    public void setupMuteConversation(Integer playerID, String reason, String uuid, String username) {
        this.playerID = playerID;
        this.reason = reason;
        this.punishedUUID = uuid;
        this.punishedUsername = username;
    }
}
