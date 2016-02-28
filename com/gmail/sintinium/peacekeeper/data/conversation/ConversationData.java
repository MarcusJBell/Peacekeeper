package com.gmail.sintinium.peacekeeper.data.conversation;

import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConversationData {

    public List<TimeManager.TimeResult> results;
    public List<String> missedMessages;
    public ConversationListener.ConversationType conversationType;

    public Set<TimeManager.TimeResult> timeResults;
    public long finalTime = 0;

    public String header;
    public String reason;
    public String punishedUUID, punishedUsername;
    public Integer playerID, adminID;

    public ConversationData(List<TimeManager.TimeResult> results, ConversationListener.ConversationType conversationType, String header) {
        this.results = results;
        this.conversationType = conversationType;
        missedMessages = new ArrayList<>();
        timeResults = new HashSet<>();
        this.header = header;
    }

}
