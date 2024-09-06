package org.v_utls.handlers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

public class CustomDamageHandler {
    private final Plugin plugin;

    public CustomDamageHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void applyCustomDamage(Entity entity, double damage, String customCause) {
        // Check if the entity is a LivingEntity
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;

            // Set metadata for the custom damage cause
            livingEntity.setMetadata("customDamageCause", new FixedMetadataValue(plugin, customCause));

            // Apply damage
            livingEntity.damage(damage);
        }
    }

    public void resetCustomDamageCauses(Player player) {
        player.removeMetadata("customDamageCause", plugin);
    }
}

