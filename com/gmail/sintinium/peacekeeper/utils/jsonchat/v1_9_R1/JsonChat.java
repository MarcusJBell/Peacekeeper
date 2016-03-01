package com.gmail.sintinium.peacekeeper.utils.jsonchat.v1_9_R1;

import net.minecraft.server.v1_9_R1.IChatBaseComponent;
import net.minecraft.server.v1_9_R1.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class JsonChat implements com.gmail.sintinium.peacekeeper.utils.jsonchat.JsonChat {

    @Override
    public void tellRawMessage(Player player, String message) {
        IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(message);
        PacketPlayOutChat packet = new PacketPlayOutChat(component);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

}
