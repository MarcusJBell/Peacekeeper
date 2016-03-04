package com.gmail.sintinium.peacekeeper.commands;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.util.*;

public class PowerToolCommand extends BaseCommand implements Listener {

    public List<PowerToolData> powerTools;

    public PowerToolCommand(Peacekeeper peacekeeper) {
        super(peacekeeper);
        powerTools = new ArrayList<>();
    }

    // WIP replacement for Essential's powertool
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }

        if (((Player) sender).getUniqueId().toString().equals("108c89bc-ab51-4609-a9d5-13bb8808df98") || ((Player) sender).getUniqueId().toString().equals("bb55301c-d10e-4368-bdbd-9563c2b79d35")) {
            if (args.length > 0) {
                args = Arrays.copyOf(ArrayUtils.remove(args, 0), args.length - 1, String[].class);
            }
            if (args.length == 0) {
                return true;
            } else if (args[0].equalsIgnoreCase("set")) {
                if (((Player) sender).getInventory().getItemInMainHand().getType() == Material.AIR) {
                    sender.sendMessage(ChatColor.DARK_RED + "Powertool cannot be set to empty hand!");
                    return true;
                }
                Block block = ((Player) sender).getTargetBlock((Set<Material>) null, 100);
                if (block.getType() != Material.COMMAND) {
                    sender.sendMessage(ChatColor.DARK_RED + "You must be looking at a command block to set a command.");
                    return true;
                }
                CommandBlock commandBlock = (CommandBlock) block.getState();
                PowerToolData data = new PowerToolData(((Player) sender).getPlayer().getUniqueId(), commandBlock.getCommand(), ((Player) sender).getInventory().getItemInMainHand().getType());
                clearItem(data.player, data.item);
                powerTools.add(data);
            } else if (args[0].equalsIgnoreCase("del")) {
                Block block = ((Player) sender).getTargetBlock((Set<Material>) null, 100);
                if (block.getType() != Material.COMMAND) {
                    sender.sendMessage(ChatColor.DARK_RED + "You must be looking at a command block to set a command.");
                    return true;
                }
                PowerToolData data = getPowerTool(((Player) sender).getUniqueId(), ((Player) sender).getInventory().getItemInMainHand().getType());
                if (data == null) {
                    sender.sendMessage(ChatColor.DARK_RED + "This item has no powertool");
                    return true;
                }
                clearItem(data.player, data.item);
                sender.sendMessage("Cleared powertool from your currently held item");
            }
        } else {
            return false;
        }
        return true;
    }

    public void onInteract(PlayerInteractEvent event) {
        PowerToolData data = getPowerTool((event.getPlayer()).getUniqueId(), (event.getPlayer()).getInventory().getItemInMainHand().getType());
        if (data != null) {
            Bukkit.getServer().dispatchCommand(event.getPlayer(), data.command);
            event.setCancelled(true);
            event.getPlayer().sendMessage("Running command...");
        }
    }

    @Nullable
    public PowerToolData getPowerTool(UUID uuid, Material item) {
        for (PowerToolData data : powerTools) {
            if (data.player.equals(uuid) && data.item == item) {
                return data;
            }
        }
        return null;
    }

    public boolean clearItem(UUID uuid, Material item) {
        PowerToolData data = getPowerTool(uuid, item);
        if (data != null) {
            powerTools.remove(data);
            return true;
        }
        return false;
    }

    public boolean containsPlayer(UUID uuid) {
        for (PowerToolData data : powerTools) {
            if (data.player.equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public class PowerToolData {
        public String command;
        public Material item;
        public UUID player;

        public PowerToolData(UUID player, String command, Material item) {
            this.player = player;
            this.command = command;
            this.item = item;
        }
    }

}
