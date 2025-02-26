package org.vicky.betterHUD;
import org.bukkit.entity.Player;
import org.vicky.utilities.ContextLogger.ContextLogger;

import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossbarManager {
    private static final ConcurrentHashMap<UUID, BossbarMechanic> bossbars = new ConcurrentHashMap<>();
    public static final ContextLogger logger = new ContextLogger(ContextLogger.ContextType.FEATURE, "BOSSBARS");

    public static void registerBossbar(UUID mobId, BossbarMechanic bossbar) {
        if (bossbars.values().stream().noneMatch( b -> !Objects.equals(b.name, bossbar.name)))
            bossbars.put(mobId, bossbar);
        else
            logger.printBukkit("Bossbar with name: " + bossbar.name + " is already registered", ContextLogger.LogType.WARNING);
    }

    public static void unregisterBossbar(UUID bossbarId) {
        bossbars.remove(bossbarId);
    }

    // Call this periodically (e.g., via a scheduled task) to update all active bossbars.
    public static void updateAll() {
        bossbars.forEach((uuid, bossbar) -> bossbar.updateForMob());
    }

    public static BossbarMechanic getBossbar(UUID mobUUID) {
        return bossbars.getOrDefault(mobUUID, null);
    }

    /**
     * Returns the BossbarMechanic whose mob is closest to the given player.
     * If no valid bossbar is found, returns null.
     */
    public static BossbarMechanic getClosestBossbar(Player player) {
        return bossbars.values().stream()
                .filter(b -> b.getMob() != null && b.getMob().isValid())
                .min(Comparator.comparingDouble(b -> b.getMob().getLocation().distance(player.getLocation())))
                .orElse(null);
    }

    /**
     * Finds a bossbar by the mob's name.
     * Returns the first match found.
     */
    public static BossbarMechanic getBossbarByName(String mobName) {
        return bossbars.values().stream()
                .filter(b -> b.getMob() != null && b.getMob().getName().equalsIgnoreCase(mobName))
                .findFirst()
                .orElse(null);
    }
}
