package com.gmail.sintinium.peacekeeper.utils;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.RecordData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class PunishmentHelper {

    // Milliseconds in one 30 day month
    public static final Long THIRTY_DAY_MONTH = 2592000000L;

    Peacekeeper peacekeeper;

    public PunishmentHelper(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    public PunishmentResult getTime(int playerID, Integer severity, ConversationListener.ConversationType conversationType) {
        // Formula is all bans without a 2 month gap + stockTime*1.5^offenseCount
        //TODO: Add subtype compatibility to conversations
        if (conversationType == ConversationListener.ConversationType.SUSPEND || conversationType == ConversationListener.ConversationType.MUTE) {
            return process(conversationType, playerID, severity, null);
        }
        return null;
    }

    @Nullable
    private PunishmentResult process(ConversationListener.ConversationType conversationType, int playerID, Integer type, Integer subType) {
        List<RecordData> datas = null;
        switch (conversationType) {
            case SUSPEND:
                datas = processDataForGaps(peacekeeper.recordTable.getRecordsByType(playerID, PlayerRecordTable.BAN));
                break;
            case MUTE:
                datas = processDataForGaps(peacekeeper.recordTable.getRecordsByType(playerID, PlayerRecordTable.MUTE));
                break;
        }
        if (datas == null) {
            return new PunishmentResult(getStockTime(conversationType, type, subType), 0);
        }
        long totalLastLength = getPreviousTotalLength(datas);
        long stockTime = getStockTime(conversationType, type, subType);
        int offenseCount = datas.size();
        long processedTime = totalLastLength + (stockTime * (int) Math.pow(1.5, offenseCount));
        return new PunishmentResult(processedTime, offenseCount);
    }

    @Nullable
    private List<RecordData> processDataForGaps(List<RecordData> datas) {
        if (datas == null) return null;
        List<RecordData> processedData = new ArrayList<>();
        long lastTime = System.currentTimeMillis();
        for (RecordData d : datas) {
            if (lastTime - d.time > THIRTY_DAY_MONTH * 2L) break;
            lastTime = d.time;
            processedData.add(d);
        }
        return processedData;
    }

    private long getPreviousTotalLength(List<RecordData> processedData) {
        long totalLength = 0L;
        for (RecordData d : processedData) {
            totalLength += d.length;
        }
        return totalLength;
    }

    //TODO: get stock time from file
    private long getStockTime(ConversationListener.ConversationType conversationType, Integer type, Integer subType) {
        return 1000L * 10L;
    }

    public class PunishmentResult {
        public long time;
        public int offenseCount;

        public PunishmentResult(long time, int offenseCount) {
            this.time = time;
            this.offenseCount = offenseCount;
        }
    }

}
