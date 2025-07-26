/* Licensed under Apache-2.0 2024. */
package org.vicky.handlers;

import java.util.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CustomDamageHandler {
  private final Plugin plugin;
  public static final String metadataKey = "customDamageCauseVUTL";

  // Tracks active removal timers: entity UUID -> (cause ID -> runnable)
  private final Map<UUID, Map<String, BukkitRunnable>> activeTimers = new HashMap<>();

  public CustomDamageHandler(Plugin plugin) {
    this.plugin = plugin;
  }

  public void applyCustomDamage(LivingEntity entity, double damage, String customCause) {
    applyCustomDamage(entity, damage, customCause, 100L); // default: 5s
  }

  public void applyCustomDamage(
      LivingEntity entity, double damage, String customCause, long durationTicks) {
    UUID uuid = entity.getUniqueId();

    // Get current causes from metadata
    List<String> list = new ArrayList<>();
    if (entity.hasMetadata(metadataKey)) {
      for (MetadataValue mv : entity.getMetadata(metadataKey)) {
        if (mv.getOwningPlugin() == plugin && mv.value() instanceof List<?>) {
          list.addAll((List<String>) mv.value());
        }
      }
    }

    if (!list.contains(customCause)) list.add(customCause);
    entity.setMetadata(metadataKey, new FixedMetadataValue(plugin, list));
    entity.damage(damage);

    // Cancel and replace existing runnable for this cause
    activeTimers
        .computeIfAbsent(uuid, k -> new HashMap<>())
        .compute(
            customCause,
            (cause, existing) -> {
              if (existing != null) existing.cancel();

              BukkitRunnable removal =
                  new BukkitRunnable() {
                    @Override
                    public void run() {
                      if (!entity.isValid() || entity.isDead()) {
                        cleanup(uuid, customCause);
                        return;
                      }

                      // Remove the cause from metadata
                      List<String> current = new ArrayList<>();
                      if (entity.hasMetadata(metadataKey)) {
                        for (MetadataValue mv : entity.getMetadata(metadataKey)) {
                          if (mv.getOwningPlugin() == plugin && mv.value() instanceof List) {
                            current.addAll((List<String>) mv.value());
                          }
                        }
                      }

                      if (current.remove(customCause)) {
                        entity.setMetadata(metadataKey, new FixedMetadataValue(plugin, current));
                      }

                      cleanup(uuid, customCause);
                    }
                  };
              removal.runTaskLater(plugin, durationTicks);
              return removal;
            });
  }

  public void resetCustomDamageCauses(Player player) {
    UUID uuid = player.getUniqueId();
    player.removeMetadata(metadataKey, plugin);
    Map<String, BukkitRunnable> timers = activeTimers.remove(uuid);
    if (timers != null) {
      timers.values().forEach(BukkitRunnable::cancel);
    }
  }

  private void cleanup(UUID uuid, String cause) {
    Map<String, BukkitRunnable> timers = activeTimers.get(uuid);
    if (timers != null) {
      timers.remove(cause);
      if (timers.isEmpty()) {
        activeTimers.remove(uuid);
      }
    }
  }
}
