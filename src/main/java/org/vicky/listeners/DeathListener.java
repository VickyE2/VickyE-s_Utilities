/* Licensed under Apache-2.0 2024. */
package org.vicky.listeners;

import static org.vicky.vicky_utils.plugin;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.MetadataValue;
import org.vicky.deathMessage.DeathMessageBuilder;
import org.vicky.effectsSystem.EffectRegistry;
import org.vicky.effectsSystem.StatusEffect;
import org.vicky.handlers.CustomDamageHandler;

public class DeathListener implements Listener {
  public DeathListener() {}

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    LivingEntity entity = event.getEntity();
    LivingEntity killer = entity.getKiller();

    // Check if the entity has the custom damage metadata
    if (entity.hasMetadata(CustomDamageHandler.metadataKey)) {
      for (MetadataValue value : entity.getMetadata(CustomDamageHandler.metadataKey)) {
        if (value.getOwningPlugin() == plugin) {
          List<String> customCauseKey = (List<String>) value.value();

          String playerName = entity.getName();
          String killerName = (killer != null) ? killer.getName() : null;
          DeathMessageBuilder builder = new DeathMessageBuilder(playerName).setKiller(killerName);
          for (String key : customCauseKey) {
            builder.addCauseKey(key);
          }
          Component deathMessage = builder.build();

          event.deathMessage(deathMessage);
          break;
        }
      }
    }
    for (StatusEffect effect :
        EffectRegistry.getInstance(EffectRegistry.class).getRegisteredEntities()) {
      if (effect.isEntityAffected(entity)) {
        effect.stopEffect(entity);
      }
    }
  }
}
