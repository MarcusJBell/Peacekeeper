package com.gmail.sintinium.peacekeeper.listeners;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.HashMap;
import java.util.UUID;

public class CreativePatchListener implements Listener {

    public final int TOTALTIME = 1500;
    public HashMap<UUID, Long> timers;

    public CreativePatchListener() {
        timers = new HashMap<>();
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getFrom().getWorld() != e.getTo().getWorld() && e.getPlayer().getGameMode() == GameMode.CREATIVE) {
            timers.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

//    @EventHandler
//    public void onInteract(PlayerInteractEvent e) {
//        if (e.getItem() != null && e.getItem().getType() == Material.POTION) {
//            Potion potion = Potion.fromItemStack(e.getItem());
//            PotionMeta meta = (PotionMeta) e.getItem().getItemMeta();
//            List<PotionEffect> effects = meta.getCustomEffects();
//            for (int i = 0; i < effects.size(); i++) {
//                if (effects.get(i).getAmplifier() >= 125) {
//                    e.getPlayer().getInventory().remove(e.getItem());
//                    e.setCancelled(true);
//                    Bukkit.getConsoleSender().sendMessage(ChatColor.DARK_RED + "WARNING: " + ChatColor.RESET + e.getPlayer().getName() + " USED ILLEGAL POTION " + Arrays.toString(effects.toArray()));
//                    break;
//                }
//            }
//        }
//    }

//    @EventHandler
//    public void onThrow(PotionSplashEvent e) {
//        Player p;
//        if (e.getEntity().getShooter() instanceof Player) {
//            p = (Player) e.getEntity().getShooter();
//        } else {
//            return;
//        }
//
//        Collection<PotionEffect> potionTypes = e.getEntity().getEffects();
////        if (potionTypes.isEmpty()) {
////            e.setCancelled(true);
////        }
//        Iterator iterator = potionTypes.iterator();
//        while (iterator.hasNext()) {
//            PotionEffect effect = (PotionEffect) iterator.next();
//            p.sendMessage(effect.getAmplifier() + "");
//        }
//    }

    @EventHandler
    public void onSlotClick(InventoryClickEvent e) {
        UUID id = e.getWhoClicked().getUniqueId();
        if (timers.containsKey(id)) {
            if (System.currentTimeMillis() - timers.get(id) < TOTALTIME) {
                e.setCancelled(true);
            } else {
                timers.remove(id);
            }
        }
    }

}
