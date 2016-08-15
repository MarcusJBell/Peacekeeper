package com.gmail.sintinium.peacekeeper.filter;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.commands.MuteCommand;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class FilterManager implements Listener {

    public boolean filterEnabled = true;
    public String message = "";
    public long length = 0, striketime = 0;
    public int strikecount = 1;
    private HashMap<UUID, FilteredPlayer> players;
    private Peacekeeper peacekeeper;

    public FilterManager(Peacekeeper peacekeeper) {
        this.peacekeeper = peacekeeper;
        players = new HashMap<>();
        Bukkit.getScheduler().scheduleSyncRepeatingTask(peacekeeper, new Runnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, FilteredPlayer>> i = players.entrySet().iterator();
                while (i.hasNext()) {
                    Map.Entry<UUID, FilteredPlayer> e = i.next();
                    Iterator<Long> it = e.getValue().blockedTimes.iterator();
                    while (it.hasNext()) {
                        long t = it.next();
                        if (System.currentTimeMillis() - t >= striketime) {
                            it.remove();
                        }
                    }
                    if (e.getValue().blockedTimes.isEmpty()) {
                        i.remove();
                    }
                }
            }
        }, 0L, 1200L);
    }

    public void addBlocked(final Player player) {
        if (!filterEnabled) return;

        if (!players.containsKey(player.getUniqueId())) {
            players.put(player.getUniqueId(), new FilteredPlayer());
        }
        FilteredPlayer fp = players.get(player.getUniqueId());
        Iterator<Long> it = fp.blockedTimes.iterator();
        while (it.hasNext()) {
            long t = it.next();
            if (System.currentTimeMillis() - t >= striketime) {
                it.remove();
            }
        }

        fp.blockedTimes.add(System.currentTimeMillis());
        if (fp.blockedTimes.size() >= 4) {
            peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
                @Override
                public void runTask() {
                    int playerID = peacekeeper.userTable.getPlayerIDFromUUID(player.getUniqueId().toString());
                    MuteCommand.muteUser(Bukkit.getConsoleSender(), peacekeeper, player.getUniqueId().toString(), player.getName(), playerID, length, message, "Chat filter", null);
                }
            });
            fp.blockedTimes.clear();
        } else if (fp.blockedTimes.size() > 1) {
            player.sendMessage(ChatColor.YELLOW + "WARNING: " + ChatColor.DARK_RED + "You currently have " + fp.blockedTimes.size() + "/4 chat strikes.");
            ChatUtils.broadcast(ChatColor.DARK_RED + "" + fp.blockedTimes.size() + "/4 chat strikes.");
        }
    }

}
