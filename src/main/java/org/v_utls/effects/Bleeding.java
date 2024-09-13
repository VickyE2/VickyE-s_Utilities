package org.v_utls.effects;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.v_utls.vicky_utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Bleeding implements CustomEffect {

    private final Plugin plugin;
    private final Map<UUID, Integer> bleedingEntities = new HashMap<>();
    private final EffectType effectType = EffectType.HARMFUL;

    public Bleeding(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void applyEffect(LivingEntity entity, int durationInSeconds, int level) {
        if (bleedingEntities.containsKey(entity.getUniqueId())) {
            // Entity is already bleeding
            return;
        }

        entity.setMetadata("isBleeding_v", new FixedMetadataValue(plugin, true));
        // Add the entity to the bleeding list
        bleedingEntities.put(entity.getUniqueId(), durationInSeconds);

        // Schedule a repeating task to deal damage over time
        new BukkitRunnable() {
            int remainingDuration = durationInSeconds;

            @Override
            public void run() {
                if (remainingDuration <= 0 || !bleedingEntities.containsKey(entity.getUniqueId())) {
                    stopEffect(entity);  // Stop the bleeding
                    this.cancel();  // Stop the task
                    return;
                }

                if (!entity.isDead() && entity.isValid()) {
                    // Apply potion effect and custom damage
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, level, true, false, false));
                    applyCustomBleedingDamage(entity, level);
                }

                remainingDuration--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20L is 1 second (20 ticks)
    }

    @Override
    public void stopEffect(LivingEntity entity) {
        if (bleedingEntities.containsKey(entity.getUniqueId())) {
            entity.removeMetadata("isBleeding_v", plugin);
            bleedingEntities.remove(entity.getUniqueId());
            entity.sendMessage("Your bleeding has stopped.");
        }
    }

    @Override
    public boolean isEntityAffected(LivingEntity entity) {
        return bleedingEntities.containsKey(entity.getUniqueId());
    }

    @Override
    public EffectType getEffectType() {
        return effectType;
    }

    private void applyCustomBleedingDamage(LivingEntity entity, int level) {
        vicky_utils pluginUtils = (vicky_utils) Bukkit.getPluginManager().getPlugin(plugin.getName());
        double customDamage = calculateBleedingDamage(level);

        assert pluginUtils != null;
        pluginUtils.getCustomDamageHandler().applyCustomDamage(entity, customDamage, "bleeding");
    }

    private double calculateBleedingDamage(int level) {
        // Damage calculation formula for bleeding
        return -34 * Math.exp(-0.5 * (level * Math.cos(34.1) + level * Math.sin(39))) + 34;
    }
}
