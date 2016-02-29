package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import com.gmail.sintinium.peacekeeper.data.conversation.ReportConversationData;
import com.gmail.sintinium.peacekeeper.listeners.ConversationListener;
import com.gmail.sintinium.peacekeeper.manager.TimeManager;
import com.gmail.sintinium.peacekeeper.queue.IQueueableTask;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReportCommand extends BaseCommand {

    public ReportCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
    }

    public void submitReport(final Player player, final String message, final String category) {
        peacekeeper.databaseQueueManager.scheduleTask(new IQueueableTask() {
            @Override
            public void runTask() {
                peacekeeper.reportTable.addReport(peacekeeper.userTable.getPlayerIDFromUUID(player.getUniqueId().toString()), message, category);
            }
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be ran by players.");
            return true;
        }
        ReportConversationData data = new ReportConversationData(peacekeeper.timeManager.configMap.get(TimeManager.REPORT), ConversationListener.ConversationType.REPORT, ChatColor.DARK_AQUA + "Reporting...");
        peacekeeper.conversationListener.conversations.put((Player) sender, data);
        peacekeeper.conversationListener.sendConversationInstructions((Player) sender);
        return true;
    }

}
