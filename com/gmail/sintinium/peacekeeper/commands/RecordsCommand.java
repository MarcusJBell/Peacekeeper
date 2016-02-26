package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.RecordData;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RecordsCommand extends BaseCommand {

    public Map<CommandSender, List<RecordData>> viewingPlayers;
    int pageLength = 7;

    public RecordsCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
        viewingPlayers = new ConcurrentHashMap<>();
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String s, final String[] args) {
        // records <page>, /records id <recordID>, /records player <username|UUID|IP>
        if (args.length == 1) {
            if (!viewingPlayers.containsKey(sender)) {
                sender.sendMessage(ChatColor.DARK_RED + "You're currently not viewing any records.");
                usage(sender);
                return true;
            } else if (!StringUtils.isNumeric(args[0])) {
                sender.sendMessage(ChatColor.DARK_RED + "Page must be a number");
                usage(sender);
                return true;
            }
            peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
                @Override
                public void runTask() {
                    SortedMap<Integer, String> strings = recordDataToPages(viewingPlayers.get(sender));
                    ChatUtils.paginate(sender, strings, Integer.parseInt(args[0]), pageLength, "Next page: /records " + (Integer.parseInt(args[0]) + 1));
                    sender.sendMessage(ChatColor.DARK_AQUA + "For detailed record info do /records id <recordID>");
                }
            });
            return true;
        }
        if (args.length < 2) {
            usage(sender);
            return true;
        }

        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                if (args[0].equalsIgnoreCase("id")) {
                    if (!StringUtils.isNumeric(args[1])) {
                        usage(sender);
                        return;
                    }
                    RecordData data = peacekeeper.recordTable.getRecordData(Integer.parseInt(args[1]));
                    if (data == null) {
                        sender.sendMessage(ChatColor.DARK_RED + "Record ID: " + ChatColor.RED + args[1] + ChatColor.DARK_RED + " was not found in database");
                        return;
                    }
                    advancedDataToChat(sender, data);
                    return;
                }

                List<RecordData> recordDatas = null;
                if (args[0].equalsIgnoreCase("player")) {
                    Integer playerID = peacekeeper.userTable.getPlayerIDFromUsername(args[1]);
                    if (playerID != null)
                        recordDatas = peacekeeper.recordTable.getPlayerRecords(playerID);
                } else if (args[0].equalsIgnoreCase("uuid")) {
                    if (!CommandUtils.isUUID(args[1])) {
                        sender.sendMessage(ChatColor.DARK_RED + "Not a valid UUID");
                        return;
                    }
                    Integer playerID = peacekeeper.userTable.getPlayerIDFromUUID(args[1]);
                    if (playerID != null)
                        recordDatas = peacekeeper.recordTable.getPlayerRecords(playerID);
                } else if (args[0].equalsIgnoreCase("ip")) {
                    if (!CommandUtils.isIP(args[1])) {
                        if (!CommandUtils.isUUID(args[1])) {
                            sender.sendMessage(ChatColor.DARK_RED + "Not a valid IP");
                            return;
                        }
                        recordDatas = peacekeeper.recordTable.getIPRecords(args[1]);
                    }
                } else {
                    usage(sender);
                    return;
                }
                if (recordDatas == null) {
                    sender.sendMessage(ChatColor.RED + args[1] + ChatColor.DARK_RED + " not found in database");
                    return;
                }
                boolean preExisting = viewingPlayers.containsKey(sender);
                viewingPlayers.put(sender, recordDatas);
                ChatUtils.paginate(sender, recordDataToPages(recordDatas), 1, pageLength, "Next page: /records 2");
                sender.sendMessage(ChatColor.DARK_AQUA + "For detailed record info do /records id <recordID>");

                // Remove user from map after 10 minutes to clear memory
                if (!preExisting) {
                    Bukkit.getScheduler().runTaskLater(peacekeeper, new Runnable() {
                        @Override
                        public void run() {
                            viewingPlayers.remove(sender);
                        }
                    }, 12000L);
                }
            }
        });


        return true;
    }

    public void usage(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_RED + "/records 'ID|Player|UUID|IP' <ID|Player|UUID|IP>");
        sender.sendMessage(ChatColor.DARK_RED + "To view a page of already searched do: /records <page>");
    }

    public SortedMap<Integer, String> recordDataToPages(List<RecordData> datas) {
        SortedMap<Integer, String> map = new TreeMap<>(Collections.reverseOrder());
        for (int i = 0; i < datas.size(); i++) {
            map.put(i, recordDataToStringFromDB(datas.get(i)));
        }
        return map;
    }

    /**
     * Returns the data as a string. Must be ran on DatabaseQueue Thread
     *
     * @param data RecordData to process
     * @return returns RecordData as a string
     */
    public String recordDataToStringFromDB(RecordData data) {
        String result = "";
        result += "RecordID:" + ChatColor.AQUA + data.recordID + ChatColor.DARK_AQUA + ", ";
        if (data.playerID != null)
            result += "Player:" + ChatColor.AQUA + "'" + peacekeeper.userTable.getUsername(data.playerID) + "'" + ChatColor.DARK_AQUA + ", ";
        else
            result += "IP:" + ChatColor.AQUA + "'" + data.ip + "'" + ChatColor.DARK_AQUA + ", ";
        result += "Type:" + ChatColor.AQUA + data.getTypeName();
        return result;
    }

    public String advancedDataToChat(CommandSender sender, RecordData data) {
        String result = "";
        sender.sendMessage(ChatColor.DARK_AQUA + "---- Advanced Record Info ----");
        sender.sendMessage(ChatColor.DARK_AQUA + "RecordID: " + ChatColor.AQUA + data.recordID);

        if (data.playerID != null)
            sender.sendMessage(ChatColor.DARK_AQUA + "Player: " + ChatColor.AQUA + peacekeeper.userTable.getUsername(data.playerID));
        else
            sender.sendMessage(ChatColor.DARK_AQUA + "IP: '" + ChatColor.RED + data.ip + "'");

        if (data.adminID != null)
            sender.sendMessage(ChatColor.DARK_AQUA + "Admin: " + ChatColor.AQUA + peacekeeper.userTable.getUsername(data.adminID));
        else
            sender.sendMessage(ChatColor.DARK_AQUA + "Admin: " + ChatColor.AQUA + "CONSOLE");

        sender.sendMessage(ChatColor.DARK_AQUA + "Type: " + ChatColor.AQUA + data.getTypeName());
        sender.sendMessage(ChatColor.DARK_AQUA + "Record Created: " + ChatColor.AQUA + TimeUtils.formatTime(data.time));

        if (data.length != null)
            sender.sendMessage(ChatColor.DARK_AQUA + "Length: " + ChatColor.AQUA + TimeUtils.millsToString(data.length));
        else
            sender.sendMessage(ChatColor.DARK_AQUA + "Length: " + ChatColor.RED + "FOREVER");

        sender.sendMessage(ChatColor.DARK_AQUA + "Reason: " + ChatColor.AQUA + "\"" + data.reason + "\"");
        String stockReason = null;
        TimeManager.TimeResult timeResult = null;
        if (data.getTypeName().equalsIgnoreCase("Mute")) {
            timeResult = peacekeeper.timeManager.configMap.get(TimeManager.MUTE).get(data.stockID - 1);
            stockReason = timeResult.description;
        } else if (data.getTypeName().equalsIgnoreCase("Suspension")) {
            timeResult = peacekeeper.timeManager.configMap.get(TimeManager.SUSPEND).get(data.stockID - 1);
            stockReason = timeResult.description;
        }
        if (stockReason != null)
            sender.sendMessage(ChatColor.DARK_AQUA + "Stock Reason: " + ChatColor.AQUA + "\"" + stockReason + "\"" + ChatColor.DARK_AQUA + " Default Length: " + ChatColor.AQUA + timeResult.length);

        return result;
    }

}
