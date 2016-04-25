package com.gmail.sintinium.peacekeeper.filter;

import java.util.ArrayList;
import java.util.List;

public class FilteredPlayer {

    public List<Long> blockedTimes;

    public FilteredPlayer() {
        this.blockedTimes = new ArrayList<>();
    }
}
