package com.gmail.sintinium.peacekeeper.utils;

import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CraftBukkitUtils {

    public static void tellRawMessage(Player player, String message) {
        String version = Peacekeeper.craftVersion;
        String netMinecraft = "net.minecraft.server." + version + ".";
        String orgBukkit = "org.bukkit.craftbukkit." + version + ".";
        try {
            final Class<?> ppoc = Class.forName(netMinecraft + "PacketPlayOutChat");
            final Class<?> cpc = Class.forName(orgBukkit + "entity.CraftPlayer");
            final Class<?> pc = Class.forName(netMinecraft + "Packet");

            Object packetPlayerOutChat;
            {
                final Class<?> iChatClass = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");
                final Class<?> chatComponentTextClass = Class.forName("net.minecraft.server." + version + ".ChatComponentText");
                final Class<?> chatBaseComponentClass = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent$ChatSerializer");
                final Object chatComponentText = chatComponentTextClass.cast(chatBaseComponentClass.getMethod("a", String.class).invoke(null, message));
                packetPlayerOutChat = ppoc.getConstructor(iChatClass).newInstance(chatComponentText);
            }

            Object craftPlayer = cpc.cast(player);

            final Method getHandle = cpc.getDeclaredMethod("getHandle");
            Object handle = getHandle.invoke(craftPlayer);
            final Field playerConnectionField = handle.getClass().getDeclaredField("playerConnection");
            Object playerConnection = playerConnectionField.get(handle);
            final Method sendPacketMethod = playerConnection.getClass().getDeclaredMethod("sendPacket", pc);
            sendPacketMethod.invoke(playerConnection, packetPlayerOutChat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
