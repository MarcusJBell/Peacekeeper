package com.gmail.sintinium.peacekeeper.utils;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.RecordData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;

import javax.annotation.Nullable;
import java.util.List;

public class PunishmentHelper {

    // Milliseconds in one 30 day month
    public static final Long THIRTY_DAY_MONTH = 2592000000L;

    Peacekeeper peacekeeper;

    public PunishmentHelper(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
    }

    @Nullable
    public Long getTime(int playerID, int severity, ConversationListener.ConversationType type) {
        //TODO: Come up with the math to calculate the length
        switch (type) {
            case SUSPEND:
                return processSuspend(playerID, severity);
            case MUTE:
                return processMute(playerID, severity);
            default:
                return null;
        }
    }

    @Nullable
    public Long processSuspend(int playerID, int severity) {
        List<RecordData> datas = peacekeeper.recordTable.getRecordsByTypeWithinTime(playerID, PlayerRecordTable.BAN, THIRTY_DAY_MONTH * 2L);
        return 0L;
    }

    @Nullable
    public Long processMute(int playerID, int severity) {
        List<RecordData> datas = peacekeeper.recordTable.getRecordsByTypeWithinTime(playerID, PlayerRecordTable.MUTE, THIRTY_DAY_MONTH * 2L);
        return 0L;
    }

}
