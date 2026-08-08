package org.vicky.forge.forgeplatform.item;

import net.minecraft.world.entity.player.Player;
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
    }

    public static boolean isInspecting(Player player) {
        return INSPECTING_PLAYERS.contains(player.getUUID());
    }
}