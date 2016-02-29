package com.gmail.sintinium.peacekeeper.data.conversation;

import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;

import java.util.ArrayList;
import java.util.List;

public class ReportConversationData extends ConversationData {

    public List<String> messages;

    public ReportConversationData(List<TimeManager.TimeResult> results, ConversationListener.ConversationType conversationType, String header) {
        super(results, conversationType, header);
        messages = new ArrayList<>();
    }

    public void addMessage(String message) {
        messages.add(message.replaceAll("\\s+$", ""));
    }

    public String getFinalMessage() {
        String message = "";
        for (String s : messages) {
            message += s + " ";
        }
        return message;
    }

}
