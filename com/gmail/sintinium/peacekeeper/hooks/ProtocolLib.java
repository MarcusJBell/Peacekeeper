package com.gmail.sintinium.peacekeeper.hooks;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.gmail.sintinium.peacekeeper.Peacekeeper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProtocolLib {

    // Add packet listener to server list check. Hide vanished players from the list and reduce the player count but that many
    public static void setupProtocolLib(final Peacekeeper peacekeeper) {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(peacekeeper, ListenerPriority.HIGH, PacketType.Status.Server.OUT_SERVER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Status.Server.OUT_SERVER_INFO) {
                    if (peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.isEmpty()) return;
                    WrappedServerPing ping = event.getPacket().getServerPings().read(0);
                    int playerListSize = Bukkit.getOnlinePlayers().size();
                    List<WrappedGameProfile> wrappedGameProfiles = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (peacekeeper.commandManager.superVanishCommand.superVanishedPlayers.contains(p.getName())) continue;
                        WrappedGameProfile profile = WrappedGameProfile.fromPlayer(p);
                        wrappedGameProfiles.add(profile);
                    }
                    ping.setPlayers(wrappedGameProfiles);
                    int count = playerListSize;
                    for (String s : peacekeeper.commandManager.superVanishCommand.superVanishedPlayers) {
                        if (Bukkit.getPlayer(s) != null) count--;
                    }
                    ping.setPlayersOnline(count);
                }
            }
        });
    }

}
