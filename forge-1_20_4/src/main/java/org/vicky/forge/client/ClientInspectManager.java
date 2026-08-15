package org.vicky.forge.client;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Player;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientInspectManager {
    // Thread-safe set of player UUIDs currently inspecting
    private static final Set<UUID> INSPECTING_PLAYERS = ConcurrentHashMap.newKeySet();

    public static void setInspecting(UUID player, boolean isInspecting) {
        if (isInspecting) {
            INSPECTING_PLAYERS.add(player);
        } else {
            INSPECTING_PLAYERS.remove(player);
        }
        LogUtils.getLogger().debug("Client Player {} is Inspecting {}", player, isInspecting);
    }

    public static boolean isInspecting(Player player) {
        return INSPECTING_PLAYERS.contains(player.getUUID());
    }
}