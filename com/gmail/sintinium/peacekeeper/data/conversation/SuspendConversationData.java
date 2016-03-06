package com.gmail.sintinium.peacekeeper.data.conversation;

import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;

import java.util.List;

public class SuspendConversationData extends ConversationData {

    public boolean updateSuspension = false;
    public Integer oldRecord = null;

    public SuspendConversationData(List<TimeManager.TimeResult> results, ConversationListener.ConversationType conversationType, String header) {
        super(results, conversationType, header);
    }

    public void setupSuspendConversation(Integer playerID, String reason, String punishedUsername) {
        this.playerID = playerID;
        this.reason = reason;
        this.punishedUsername = punishedUsername;
    }

    public void updateSuspension(int oldRecord) {
        updateSuspension = true;
        this.oldRecord = oldRecord;
    }

}
