package org.vicky.forge.forgeplatform.item;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Player;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.network.registeredpackets.UpdateInspectStatePacket;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InspectManager {
    // Thread-safe set of player UUIDs currently inspecting
    private static final Set<UUID> INSPECTING_PLAYERS = ConcurrentHashMap.newKeySet();

    public static void setInspecting(Player player, boolean isInspecting) {
        if (isInspecting) {
            INSPECTING_PLAYERS.add(player.getUUID());
        } else {
            INSPECTING_PLAYERS.remove(player.getUUID());
        }
        LogUtils.getLogger().debug("Player {} is Inspecting {}", player.getDisplayName(), isInspecting);
        PacketHandler.sendToAllClient(new UpdateInspectStatePacket(player.getUUID(), isInspecting));
    }

    public static boolean isInspecting(Player player) {
        return INSPECTING_PLAYERS.contains(player.getUUID());
    }
}