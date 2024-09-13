package org.v_utls.handlers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

public class DamageHandler {
    private final Plugin plugin;

    public DamageHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    public void applyCustomDamage(Entity entity, double damage, String customCause) {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            livingEntity.damage(damage); // Apply damage to the entity
            livingEntity.sendMessage("Damage Cause: " + customCause);

        }
    }
}
