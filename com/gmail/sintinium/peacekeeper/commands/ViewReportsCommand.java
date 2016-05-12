package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.ReportData;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import com.gmail.sintinium.peacekeeper.utils.ChatUtils;
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

public class ViewReportsCommand extends BaseCommand {

    public Map<CommandSender, List<ReportData>> viewingPlayers;

    int pageLength = 7;

    public ViewReportsCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
        viewingPlayers = new ConcurrentHashMap<>();
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, String s, final String[] args) {
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                if (args.length == 0) {
                    usage(sender);
                    return;
                }
                if (args[0].equalsIgnoreCase("all")) {
                    List<ReportData> reportData = peacekeeper.reportTable.getAllReports();
                    if (reportData.isEmpty()) {
                        sender.sendMessage(ChatColor.GREEN + "There are currently no reports");
                        return;
                    }
                    ChatUtils.paginate(sender, reportDataToPages(reportData), 1, pageLength, "Next page: /viewreports 2");
                    boolean preExisting = viewingPlayers.containsKey(sender);
                    viewingPlayers.put(sender, reportData);

                    // Remove user from map after 10 minutes to clear memory
                    if (!preExisting) {
                        Bukkit.getScheduler().runTaskLater(peacekeeper, new Runnable() {
                            @Override
                            public void run() {
                                viewingPlayers.remove(sender);
                            }
                        }, 12000L);
                    }
                } else if (args[0].equalsIgnoreCase("del") && args.length > 1) {
                    if (!sender.hasPermission("peacekeeper.command.viewreports.delete")) {
                        ChatUtils.noPermission(sender);
                        return;
                    }
                    if (!StringUtils.isNumeric(args[1])) {
                        sender.sendMessage(ChatColor.DARK_RED + "Please enter a number to delete");
                        return;
                    }
                    if (peacekeeper.reportTable.doesValueExist("ReportID", Integer.parseInt(args[1]))) {
                        peacekeeper.reportTable.deleteReport(Integer.parseInt(args[1]));
                        ChatUtils.broadcast(ChatColor.YELLOW + sender.getName() + ChatColor.GREEN + " has deleted report ID " + args[1]);
                        clearViewers();
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "Report with ID " + ChatColor.RED + args[1] + ChatColor.DARK_RED + " was not found in database");
                    }
                } else if (args[0].equalsIgnoreCase("delall") && args.length > 1) {
                    if (!sender.hasPermission("peacekeeper.command.viewreports.deleteall")) {
                        ChatUtils.noPermission(sender);
                        return;
                    }
                    Integer playerID = peacekeeper.userTable.getPlayerIDFromUsername(args[1]);
                    if (playerID == null) {
                        ChatUtils.playerNotFoundMessage(sender, args[1]);
                        return;
                    }
                    peacekeeper.reportTable.deleteRow("PlayerID", playerID);
                    ChatUtils.broadcast(ChatColor.YELLOW + sender.getName() + ChatColor.GREEN + " has deleted all of users: " + ChatColor.YELLOW + peacekeeper.userTable.getUsername(playerID) + ChatColor.GREEN + " reports");
                } else if (args[0].equalsIgnoreCase("id") && args.length > 1) {
                    if (!StringUtils.isNumeric(args[1])) {
                        sender.sendMessage(ChatColor.DARK_RED + "Report ID must be a number.");
                        return;
                    }
                    advancedInfo(sender, Integer.parseInt(args[1]));
                } else if (StringUtils.isNumeric(args[0])) {
                    if (!viewingPlayers.containsKey(sender)) {
                        sender.sendMessage(ChatColor.DARK_RED + "You're currently not viewing any reports");
                        sender.sendMessage(ChatColor.DARK_RED + "Do /viewreports to get started");
                        return;
                    }
                    SortedMap<Integer, String> strings = reportDataToPages(viewingPlayers.get(sender));
                    ChatUtils.paginate(sender, strings, Integer.parseInt(args[0]), pageLength, "Next page: /viewreports " + (Integer.parseInt(args[0]) + 1));
                    sender.sendMessage(ChatColor.DARK_AQUA + "To view a report do /viewreports id <ReportID>");

                } else {
                    if (args[0].equalsIgnoreCase("?"))
                        allUsage(sender);
                    else
                        sender.sendMessage(command.getUsage());
                }
            }
        });
        return true;
    }

    public void usage(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + "Use '" + ChatColor.AQUA + "/viewreports ?" + ChatColor.DARK_AQUA + "' for help and usages");
    }

    public void allUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_AQUA + "---- Reports Help ----");
        sendUsageElement(sender, "/viewreports all", "View all reports");
        sendUsageElement(sender, "/viewreports id <ReportID>", "View a report");
        sendUsageElement(sender, "/viewreports del <ReportID>", "Delete a report from database");
        sendUsageElement(sender, "/viewreports delall <Username>", "Delete all reports from given user");
    }

    public void sendUsageElement(CommandSender sender, String command, String description) {
        sender.sendMessage(ChatColor.DARK_AQUA + command + ": " + ChatColor.AQUA + description);
    }

    public SortedMap<Integer, String> reportDataToPages(List<ReportData> datas) {
        SortedMap<Integer, String> map = new TreeMap<>(Collections.reverseOrder());
        for (int i = 0; i < datas.size(); i++) {
            map.put(i, reportDataToStringFromDB(datas.get(i)));
        }
        return map;
    }

    public void advancedInfo(CommandSender sender, int reportID) {
        if (!peacekeeper.reportTable.doesValueExist("ReportID", reportID)) {
            sender.sendMessage(ChatColor.DARK_RED + "Report with ID " + ChatColor.RED + reportID + ChatColor.DARK_RED + " was not found in database");
        }
        sender.sendMessage(ChatColor.DARK_AQUA + "---- Start of report ----");
        ReportData reportData = peacekeeper.reportTable.getReport(reportID);
        sender.sendMessage(ChatColor.YELLOW + "Message: " + ChatColor.WHITE + reportData.message);
        if (sender instanceof Player) {
            String createJSON = "[\"\",{\"text\":\"Report created: \",\"color\":\"dark_aqua\"},{\"text\":\"" + TimeUtils.formatTime(reportData.time) + "\",\"color\":\"aqua\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + TimeUtils.millsToString(System.currentTimeMillis() - reportData.time) + " ago" + "\",\"color\":\"gold\"}]}}}]";
            CraftBukkitUtils.tellRawMessage((Player) sender, createJSON);
        } else
            sender.sendMessage(ChatColor.DARK_AQUA + "Record Created: " + ChatColor.AQUA + TimeUtils.formatTime(reportData.time));

        sender.sendMessage(ChatColor.DARK_AQUA + "Categories: " + ChatColor.AQUA + reportData.categories);
        sender.sendMessage(ChatColor.DARK_AQUA + "Report sent by: " + ChatColor.AQUA + peacekeeper.userTable.getUsername(reportData.playerID));
    }

    /**
     * Returns the data as a string. Must be ran on DatabaseQueue Thread
     *
     * @param data RecordData to process
     * @return returns RecordData as a string
     */
    public String reportDataToStringFromDB(ReportData data) {
        String result = "";
        String username = peacekeeper.userTable.getUsername(data.playerID);
        result += ChatColor.DARK_AQUA + "ReportID:" + ChatColor.AQUA + data.reportID + " ";
        result += ChatColor.DARK_AQUA + " Reporter:" + username;
        return result;
    }

    public void clearViewers() {
        viewingPlayers.clear();
    }

}
