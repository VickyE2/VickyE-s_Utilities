/* Licensed under Apache-2.0 2024. */
package org.vicky.effectsSystem;

import static org.vicky.kotlinUtils.UtilsKt.getStatusEffectData;

import java.io.InputStream;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.vicky.betterHUD.events.StatusEffectTickEvent;
import org.vicky.effectsSystem.enums.EffectType;
import org.vicky.effectsSystem.events.EntityGainStatusEffect;
import org.vicky.effectsSystem.events.PlayerGainStatusEffect;
import org.vicky.utilities.BukkitHex;

public abstract class StatusEffect {
  protected final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();
  protected final Plugin plugin;
  protected final Map<UUID, Double> durations = new HashMap<>();
  protected final Map<UUID, Integer> levels = new HashMap<>();
  private final InputStream icon;
  private final int ticksAsOne;

  public StatusEffect(Plugin plugin, InputStream icon, int ticksAsOne) {
    this.plugin = plugin;
    this.icon = icon;
    this.ticksAsOne = ticksAsOne;
  }

  /**
   * This should fire a {@link org.vicky.betterHUD.events.StatusEffectTickEvent} per tick or update
   *
   * @param entity The entity in context to apply the effect to
   * @param durationInSeconds The duration ins seconds the effect should last for
   * @param level The strength of the effect being applied
   */
  public void apply(LivingEntity entity, int durationInSeconds, int level) {
    UUID uuid = entity.getUniqueId();
    if (levels.get(uuid) != null) {
      if (levels.get(uuid) > level) {
        if (entity instanceof Player player) {
          player.sendMessage(
              BukkitHex.colorize("red[unable to add effect as a stronger one exists]"));
        }
        return;
      }
    }
    setEffectInfo(entity, durationInSeconds, level); // sets + refreshes info

    // Cancel an existing task if any
    BukkitRunnable existingTask = activeTasks.get(uuid);
    if (existingTask != null) existingTask.cancel();

    BukkitRunnable task =
        new BukkitRunnable() {
          double remaining = durationInSeconds;
          final int originalDuration = durationInSeconds;

          @Override
          public void run() {
            if (remaining <= 0 || entity.isDead() || !entity.isValid()) {
              clearEffectInfo(entity);
              stopEffect(entity);
              cancel();
              return;
            }

            entity.sendMessage(getStatusEffectData(entity, (JavaPlugin) plugin).toString());
            Bukkit.getPluginManager()
                .callEvent(
                    new StatusEffectTickEvent(entity, getKey(), remaining, originalDuration));
            onTick(entity, remaining, level); // ⬅️ This is what subclasses override

            if (originalDuration != -1) {
              durations.put(uuid, remaining);
              remaining -= ((double) ticksAsOne / 20);
            }
          }
        };

    activeTasks.put(uuid, task);
    if (entity instanceof Player player)
      Bukkit.getPluginManager()
          .callEvent(new PlayerGainStatusEffect(player, this, durationInSeconds));
    else
      Bukkit.getPluginManager()
          .callEvent(new EntityGainStatusEffect(entity, this, durationInSeconds));
    task.runTaskTimer(plugin, 0L, ticksAsOne);
  }

  /**
   * This is what should happen per tick of the event
   *
   * @param entity The entity in context to apply the effect to
   * @param remainingSeconds The duration left in case of time-based effects
   * @param level The strength of the effect being applied
   */
  protected abstract void onTick(LivingEntity entity, double remainingSeconds, int level);

  /**
   * Stops the effect manually or when expired.
   */
  public abstract void stopEffect(LivingEntity entity);

  /**
   * The key used to track this effect in metadata or registries.
   */
  public abstract String getKey();

  /**
   * Whether this is a harmful, beneficial, or neutral effect.
   */
  public abstract EffectType getEffectType();

  // public abstract double getDurationForLevel(int level);

  /**
   * Checks if the effect is active based on metadata or other markers.
   */
  public boolean isEntityAffected(LivingEntity entity) {
    for (MetadataValue val : entity.getMetadata(getKey())) {
      if (val.getOwningPlugin() == plugin && val.asBoolean()) return true;
    }
    return false;
  }

  public double getDuration(LivingEntity entity) {
    return durations.getOrDefault(entity.getUniqueId(), 0.0);
  }

  public int getLevel(LivingEntity entity) {
    return levels.getOrDefault(entity.getUniqueId(), 0);
  }

  public double getDuration(UUID entityUuid) {
    return durations.getOrDefault(entityUuid, 0.0);
  }

  public int getLevel(UUID entityUuid) {
    return levels.getOrDefault(entityUuid, 0);
  }

  protected void setEffectInfo(LivingEntity entity, double duration, int level) {
    durations.put(entity.getUniqueId(), duration);
    levels.put(entity.getUniqueId(), level);
    markAffected(entity, duration);
  }

  protected void clearEffectInfo(LivingEntity entity) {
    UUID id = entity.getUniqueId();
    durations.remove(id);
    levels.remove(id);
    BukkitRunnable task = activeTasks.remove(id);
    if (task != null) task.cancel();
    clearEffectMetadata(entity);
  }

  /**
   * Mark the entity as affected using metadata.
   */
  private void markAffected(LivingEntity entity, double durationInSeconds) {
    List<String> effects = new ArrayList<>();
    if (entity.hasMetadata("statusEffects")) {
      List<MetadataValue> metadataValues = entity.getMetadata("statusEffects");
      for (MetadataValue mv : metadataValues) {
        if (mv.getOwningPlugin() == plugin && mv.value() instanceof List) {
          //noinspection unchecked
          effects.addAll((List<String>) mv.value());
        }
      }
    }
    effects.add(getKey());
    entity.setMetadata("statusEffects", new FixedMetadataValue(plugin, effects));
    Map<String, Double> durations = new HashMap<>();
    if (entity.hasMetadata("statusEffectsDurations")) {
      List<MetadataValue> metadataValues = entity.getMetadata("statusEffectsDurations");
      for (MetadataValue mv : metadataValues) {
        if (mv.getOwningPlugin() == plugin && mv.value() instanceof Map) {
          // noinspection unchecked
          durations.putAll((Map<String, Double>) mv.value());
        }
      }
    }
    // Add or update the effect duration
    durations.put(getKey(), durationInSeconds);
    // Store back the updated map as metadata
    entity.setMetadata("statusEffectsDurations", new FixedMetadataValue(plugin, durations));
  }

  public boolean isEffectInfinite(LivingEntity entity) {
    return durations.get(entity.getUniqueId()) == -1;
  }

  /**
   * Clear effect metadata.
   */
  private void clearEffectMetadata(LivingEntity entity) {
    // Clear from "statusEffects" list
    if (entity.hasMetadata("statusEffects")) {
      List<MetadataValue> metadataValues = entity.getMetadata("statusEffects");
      for (MetadataValue mv : metadataValues) {
        if (mv.value() != null)
          if (mv.getOwningPlugin() == plugin && mv.value() instanceof List) {
            //noinspection unchecked
            List<String> effects = new ArrayList<>((List<String>) mv.value());
            effects.remove(getKey());
            entity.setMetadata("statusEffects", new FixedMetadataValue(plugin, effects));
            break; // Only handle your plugin's metadata once
          }
      }
    }

    // Clear from "statusEffectsDurations" map
    if (entity.hasMetadata("statusEffectsDurations")) {
      List<MetadataValue> metadataValues = entity.getMetadata("statusEffectsDurations");
      for (MetadataValue mv : metadataValues) {
        if (mv.value() != null)
          if (mv.getOwningPlugin() == plugin && mv.value() instanceof Map) {
            //noinspection unchecked
            Map<String, Double> durations = new HashMap<>((Map<String, Double>) mv.value());
            durations.remove(getKey());
            entity.setMetadata("statusEffectsDurations", new FixedMetadataValue(plugin, durations));
            break; // Only handle your plugin's metadata once
          }
      }
    }
  }

  public InputStream getIcon() {
    return icon;
  }
}
