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

public class Bleeding {

    private final Plugin plugin;
    private final Map<UUID, Integer> bleedingentities = new HashMap<>();

    public Bleeding(Plugin plugin) {
        this.plugin = plugin;
    }

    public void applyBleeding(LivingEntity entity, int durationInSeconds, int level) {
        if (bleedingentities.containsKey(entity.getUniqueId())) {
            // entity is already bleeding
            return;
        }


        entity.setMetadata("isBleeding_v", new FixedMetadataValue(plugin, true));
        // Add the entity to the bleeding list
        bleedingentities.put(entity.getUniqueId(), durationInSeconds);

        // Schedule a repeating task to deal damage over time
        new BukkitRunnable() {
            int remainingDuration = durationInSeconds;

            @Override
            public void run() {
                if (remainingDuration <= 0 || !bleedingentities.containsKey(entity.getUniqueId())) {
                    stopBleeding(entity);  // Stop the bleeding
                    this.cancel();  // Stop the task
                    return;
                }

                if (!entity.isDead() && entity.isValid()) {
                    entity.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 20, level, true, false, false));
                    // Deal custom damage to the entity
                    vicky_utils plugin = (vicky_utils) Bukkit.getPluginManager().getPlugin("YourPluginName");
                    double linear_to_exp = -34 * Math.exp(-0.5 * (level * Math.cos(34.1) + level * Math.sin(39))) + 34;
                    assert plugin != null;
                    plugin.getCustomDamageHandler().applyCustomDamage(entity, linear_to_exp, "bleeding");
                }

                remainingDuration--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20L is 1 second (20 ticks)
    }

    public void stopBleeding(LivingEntity entity) {
        // Remove entity from the bleeding list
        if (bleedingentities.containsKey(entity.getUniqueId())) {
            entity.removeMetadata("isBleeding_v", plugin);
            bleedingentities.remove(entity.getUniqueId());
            entity.sendMessage("Your bleeding has stopped.");
        }
    }

    public boolean isentityBleeding(LivingEntity entity) {
        return bleedingentities.containsKey(entity.getUniqueId());
    }
}
