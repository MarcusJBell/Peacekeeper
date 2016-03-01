package com.gmail.sintinium.peacekeeper.utils.jsonchat.v1_8_R3;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class JsonChat implements com.gmail.sintinium.peacekeeper.utils.jsonchat.JsonChat {

    @Override
    public void tellRawMessage(Player player, String message) {
        IChatBaseComponent component = IChatBaseComponent.ChatSerializer.a(message);
        PacketPlayOutChat packet = new PacketPlayOutChat(component);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
