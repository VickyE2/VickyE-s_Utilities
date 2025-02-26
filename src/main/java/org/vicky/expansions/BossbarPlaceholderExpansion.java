package org.vicky.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.vicky.betterHUD.BossbarManager;
import org.vicky.betterHUD.BossbarMechanic;

public class BossbarPlaceholderExpansion extends PlaceholderExpansion {

    private final String author;
    private final String version;

    public BossbarPlaceholderExpansion(String author, String version) {
        this.author = author;
        this.version = version;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "bossbar_entity";
    }

    @Override
    public @NotNull String getAuthor() {
        return author;
    }

    @Override
    public @NotNull String getVersion() {
        return version;
    }

    /**
     * Expects placeholder format: bossbar_entity_<mobName>_<property>
     * where property is one of: max_health, current_health, distance_from_player
     */
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (identifier.isEmpty()) return "";

        int underscoreIndex = identifier.indexOf('_');
        if (underscoreIndex == -1) return "";

        String mobName = identifier.substring(0, underscoreIndex);
        String property = identifier.substring(underscoreIndex + 1);

        BossbarMechanic bossbar;
        if (mobName.equalsIgnoreCase("closest")) {
            bossbar = BossbarManager.getClosestBossbar(player);
        } else {
            bossbar = BossbarManager.getBossbarByName(mobName);
        }
        if (bossbar == null) return "";

        if (property.equals("exists")) {
            return String.valueOf(bossbar.getMob() != null);
        }

        LivingEntity mob = bossbar.getMob();
        if (mob == null || !mob.isValid()) return "";
        double distance;

        switch (property) {
            case "max_health":
                return String.valueOf(mob.getMaxHealth());
            case "current_health":
                return String.valueOf(mob.getHealth());
            case "distance_from_player":
                distance = mob.getLocation().distance(player.getLocation());
                return String.format("%.2f", distance);
            case "isWithinThresholdDistance":
                distance = mob.getLocation().distance(player.getLocation());
                double threshold = bossbar.thresholdDistance;
                return String.valueOf(distance < threshold);
            default:
                return "";
        }
    }
}
