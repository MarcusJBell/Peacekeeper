package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.builders.JsonBuilder;
import com.gmail.sintinium.peacekeeper.data.RecordData;
import com.gmail.sintinium.peacekeeper.db.tables.PlayerRecordTable;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
import com.gmail.sintinium.peacekeeper.utils.CommandUtils;
import com.gmail.sintinium.peacekeeper.utils.CraftBukkitUtils;
import com.gmail.sintinium.peacekeeper.utils.TimeUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
        if (args.length == 1 && !args[0].equalsIgnoreCase("all")) {
            if (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help")) {
                fullUsage(sender);
                return true;
            }
            if (!viewingPlayers.containsKey(sender)) {
                if (StringUtils.isNumeric(args[0]))
                    sender.sendMessage(ChatColor.DARK_RED + "You're currently not viewing any records.");
                else
                    usage(sender);
                return true;
            } else if (!StringUtils.isNumeric(args[0])) {
                usage(sender);
                return true;
            }

            peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
                @Override
                public void runTask() {
                    SortedMap<Integer, String> strings = recordDataToPages(viewingPlayers.get(sender), sender instanceof Player);
                    ChatUtils.paginate(sender, strings, Integer.parseInt(args[0]), pageLength, sender instanceof Player, "Next page: /records " + (Integer.parseInt(args[0]) + 1));
                    sender.sendMessage(ChatColor.DARK_AQUA + "For detailed record info do /records id <RecordID>");
                }
            });
            return true;
        }
        if (args.length > 0 && args[0].equalsIgnoreCase("all")) {
        } else if (args.length < 2) {
            usage(sender);
            return true;
        }

        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                List<RecordData> recordDatas = null;
                if (args.length > 0 && args[0].equalsIgnoreCase("all")) {
                    List<RecordData> tempData = peacekeeper.recordTable.getAllRecords();
                    if (tempData.isEmpty()) {
                        sender.sendMessage(ChatColor.YELLOW + "There are currently no records in the database.");
                        return;
                    }
                    recordDatas = tempData;
                } else {
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
                    } else if (args[0].equalsIgnoreCase("player") || args[0].equalsIgnoreCase("p")) {
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
                            sender.sendMessage(ChatColor.DARK_RED + "Not a valid IP");
                            return;
                        }
                        recordDatas = peacekeeper.recordTable.getIPRecords(args[1]);
                    } else if (args[0].equalsIgnoreCase("del")) {
                        if (!sender.hasPermission("peacekeeper.command.records.delete")) {
                            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use this command.");
                            return;
                        }
                        clearCached();
                        if (!StringUtils.isNumeric(args[1])) {
                            sender.sendMessage("Record ID must be number");
                            return;
                        }
                        Integer recordID = Integer.parseInt(args[1]);
                        RecordData data = peacekeeper.recordTable.getRecordData(recordID);
                        if (data == null) {
                            sender.sendMessage(ChatColor.DARK_RED + "Record ID " + ChatColor.RED + recordID + " not found in database");
                            return;
                        }
                        peacekeeper.recordTable.removeRecord(recordID);
                        ChatUtils.broadcast(ChatColor.DARK_RED + sender.getName() + " has deleted a record with the ID of: " + ChatColor.RED + recordID);
                        return;
                    } else if (args[0].equalsIgnoreCase("delall")) {
                        if (!sender.hasPermission("peacekeeper.command.records.deleteall")) {
                            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use this command.");
                            return;
                        }
                        clearCached();
                        if (CommandUtils.isIP(args[1])) {
                            if (!peacekeeper.recordTable.doesValueExist("IP", args[1])) {
                                sender.sendMessage(ChatColor.DARK_RED + "IP " + ChatColor.RED + args[1] + " has no records");
                                return;
                            }
                            peacekeeper.recordTable.clearIPRecords(args[1]);
                            ChatUtils.broadcast(ChatColor.DARK_RED + sender.getName() + " has deleted all records for IP: " + ChatColor.RED + args[1]);
                            return;
                        }
                        Integer playerID = peacekeeper.userTable.getPlayerIDFromUsername(args[1]);
                        if (playerID == null) {
                            ChatUtils.playerNotFoundMessage(sender, args[1]);
                            return;
                        }
                        String username = peacekeeper.userTable.getUsername(playerID);
                        peacekeeper.recordTable.clearPlayersRecords(playerID);
                        ChatUtils.broadcast(ChatColor.DARK_RED + sender.getName() + " has deleted all records for user: " + ChatColor.RED + username);
                        return;
                    } else {
                        usage(sender);
                        return;
                    }
                }
                if (recordDatas == null) {
                    sender.sendMessage(ChatColor.DARK_RED + "No records found for " + args[0] + " with the name/id of " + args[1]);
                    sender.sendMessage(ChatColor.YELLOW + "Tip: " + "Use % to approximate results. Ex: b% is Bob! %obb% is Bobby!");
                    return;
                }
                boolean preExisting = viewingPlayers.containsKey(sender);
                viewingPlayers.put(sender, recordDatas);
                ChatUtils.paginate(sender, recordDataToPages(recordDatas, sender instanceof Player), 1, pageLength, sender instanceof Player, "Next page: /records 2");
                sender.sendMessage(ChatColor.DARK_AQUA + "For detailed record info do /records id <RecordID>");

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
        sender.sendMessage(ChatColor.DARK_AQUA + "Use '" + ChatColor.AQUA + "/records ?" + ChatColor.DARK_AQUA + "' for help and usages");
    }

    public void fullUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + "---- Record Help ----");
        sendUsageElement(sender, "/records all", "Shows all records");
        sendUsageElement(sender, "/records id <ID>", "Shows advanced info for the given record ID");
        sendUsageElement(sender, "/records p <Player>", "Shows all records for player");
        sendUsageElement(sender, "/records uuid <UUID>", "Shows all records for given UUID");
        sendUsageElement(sender, "/records ip <IP>", "Shows all records for given IP");

        if (sender.hasPermission("peacekeeper.command.records.delete"))
            sendUsageElement(sender, "/records del <ID>", "Deletes record with given ID");

        if (sender.hasPermission("peacekeeper.command.records.deleteall"))
            sendUsageElement(sender, "/records delall <Player|IP>", "Deletes all records for a Player");

        if (sender.hasPermission("peacekeeper.command.records.delete") || sender.hasPermission("peacekeeper.command.records.deleteall")) {
            sender.sendMessage(ChatColor.DARK_RED + "WARNING: Deleting records will affect all future bans/mutes for affected user");
        }
    }

    public void sendUsageElement(CommandSender sender, String command, String description) {
        sender.sendMessage(ChatColor.DARK_AQUA + command + ": " + ChatColor.AQUA + description);
    }

    public SortedMap<Integer, String> recordDataToPages(List<RecordData> datas, boolean player) {
        SortedMap<Integer, String> map = new TreeMap<>(Collections.reverseOrder());
        for (int i = 0; i < datas.size(); i++) {
            if (!player) {
                map.put(i, recordDataToStringFromDB(datas.get(i)));
                continue;
            }
            map.put(i, recordDataFromDB(datas.get(i)).toString());
        }
        return map;
    }

    public JsonBuilder recordDataFromDB(RecordData data) {
        JsonBuilder builder = new JsonBuilder();
        builder.withText("").withHoverEvent(JsonBuilder.HoverAction.SHOW_TEXT, advancedDataToString(data)).withClickEvent(JsonBuilder.ClickAction.RUN_COMMAND, "/records id " + data.recordID);
        builder.withText("RecordID: ").withColor(ChatColor.DARK_AQUA).withText("" + data.recordID).withColor(ChatColor.AQUA).withText(", ").withColor(ChatColor.DARK_AQUA);
//        result += "RecordID:" + ChatColor.AQUA + data.recordID + ChatColor.DARK_AQUA + ", ";
        if (data.type != PlayerRecordTable.IP)
            builder.withText("Player: ").withColor(ChatColor.DARK_AQUA).withText("'" + peacekeeper.userTable.getUsername(data.playerID) + "'").withColor(ChatColor.AQUA);
//            result += "Player:" + ChatColor.AQUA + "'" + peacekeeper.userTable.getUsername(data.playerID) + "'" + ChatColor.DARK_AQUA + ", ";
        else
            builder.withText("IP: ").withColor(ChatColor.DARK_AQUA).withText("'" + data.ip + "'").withColor(ChatColor.AQUA);
//            result += "IP:" + ChatColor.AQUA + "'" + data.ip + "'" + ChatColor.DARK_AQUA + ", ";
        builder.withText(", Type: ").withColor(ChatColor.DARK_AQUA).withText(data.getTypeName()).withColor(ChatColor.AQUA);
//        result += "Type:" + ChatColor.AQUA + data.getTypeName();
        return builder;
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
        if (data.type != PlayerRecordTable.IP)
            result += "Player:" + ChatColor.AQUA + "'" + peacekeeper.userTable.getUsername(data.playerID) + "'" + ChatColor.DARK_AQUA + ", ";
        else
            result += "IP:" + ChatColor.AQUA + "'" + data.ip + "'" + ChatColor.DARK_AQUA + ", ";
        result += "Type:" + ChatColor.AQUA + data.getTypeName();
        return result;
    }

    public String advancedDataToString(RecordData data) {
        StringBuilder builder = new StringBuilder();

//        builder.append("§3").append("RecordID: ").append("§b").append(data.recordID).append("\n");
//
//        if (data.playerID != null)
//            builder.append("§3").append("Player: ").append("§b").append(peacekeeper.userTable.getUsername(data.playerID)).append("\n");
//        else
//            builder.append("§3").append("IP: '").append(ChatColor.RED).append(data.ip).append("'").append("\n");

        if (data.adminID != null)
            builder.append("§3").append("Admin: ").append("§b").append(peacekeeper.userTable.getUsername(data.adminID)).append("\n");
        else
            builder.append("§3").append("Admin: ").append("§b").append("CONSOLE").append("\n");

//        builder.append("§3").append("Type: ").append("§b").append(data.getTypeName()).append("\n");
        builder.append("§3").append("Record Created: ").append("§b").append(TimeUtils.millsToString(System.currentTimeMillis() - data.time, 3)).append(" ago");
        if (data.type != PlayerRecordTable.WARNING) {
            if (data.length != null)
                builder.append("\n").append("§3").append("Length: ").append("§b").append(TimeUtils.millsToString(data.length, 3));
            else
                builder.append("\n").append("§3").append("Length: ").append("§c").append("FOREVER");
        }
        String stockReason = data.category;
        if (stockReason != null && !stockReason.equalsIgnoreCase("null"))
            builder.append("\n").append("§3").append("Category: ").append("§b").append("\\\"").append(stockReason).append("\\\"");
        return builder.toString();
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
        if (sender instanceof Player) {
            String createJSON = "[\"\",{\"text\":\"Record created: \",\"color\":\"dark_aqua\"},{\"text\":\"" + TimeUtils.formatTime(data.time) + "\",\"color\":\"aqua\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + TimeUtils.millsToString(System.currentTimeMillis() - data.time) + " ago" + "\",\"color\":\"gold\"}]}}}]";
            CraftBukkitUtils.tellRawMessage((Player) sender, createJSON);
        } else
            sender.sendMessage(ChatColor.DARK_AQUA + "Record Created: " + ChatColor.AQUA + TimeUtils.formatTime(data.time));

        if (data.type != PlayerRecordTable.WARNING) {
            if (data.length != null)
                sender.sendMessage(ChatColor.DARK_AQUA + "Length: " + ChatColor.AQUA + TimeUtils.millsToString(data.length));
            else
                sender.sendMessage(ChatColor.DARK_AQUA + "Length: " + ChatColor.RED + "FOREVER");
        }

        sender.sendMessage(ChatColor.DARK_AQUA + "Reason: " + ChatColor.AQUA + "\"" + data.reason + "\"");
        String stockReason = data.category;
        if (stockReason != null && !stockReason.equalsIgnoreCase("null"))
            sender.sendMessage(ChatColor.DARK_AQUA + "Category: " + ChatColor.AQUA + "\"" + stockReason + "\"");

        return result;
    }

    public void clearCached() {
        viewingPlayers.clear();
    }

}
