/* Licensed under Apache-2.0 2024. */
package org.vicky.listeners;

import static org.vicky.utilities.DeathMessages.getMessages;

import java.util.*;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.vicky.effectsSystem.CustomEffect;

public class DeathListener implements Listener {

  // Map to hold all custom effects by their name (e.g., "bleeding", "burning")
  private final Map<String, CustomEffect> customEffects;

  // Constructor to register custom effects
  public DeathListener(Map<String, CustomEffect> customEffects) {
    this.customEffects = customEffects;
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    LivingEntity killer = entity.getKiller();

    // Check if the entity has the custom damage metadata
    if (entity.hasMetadata("customDamageCause") && entity instanceof Player) {
      for (MetadataValue value : entity.getMetadata("customDamageCause")) {
        String customCause = value.asString();

        // Get the messages for the custom cause from DeathMessages
        Map<String, Boolean> messages = getMessages(customCause);

        if (!messages.isEmpty()) {
          String deathMessage;
          int messageIndex;
          List<String> possibleMessages = getPossibleMessages(killer, messages);
          messageIndex = new Random().nextInt(possibleMessages.size());
            deathMessage = possibleMessages.get(messageIndex);
            event
                    .getEntity()
                    .getServer()
                    .broadcastMessage(deathMessage.replace("{player}", entity.getName()).replace("{killer}", killer.getName()));
        }
        break;
      }
    }
    for (Map.Entry<String, CustomEffect> entry : customEffects.entrySet()) {
      CustomEffect effect = entry.getValue();
      if (effect.isEntityAffected(entity)) {
        effect.stopEffect(entity); // Stop the effect when the entity dies
      }
    }
  }

  @NotNull
  private static List<String> getPossibleMessages(LivingEntity killer, Map<String, Boolean> messages) {
    List<String> possibleMessages = new ArrayList<>();
    if (killer != null) {
      for (Map.Entry<String, Boolean> message : messages.entrySet()) {
        if (message.getValue()) {
          possibleMessages.add(message.getKey());
        }
      }
    }
    else {
      for (Map.Entry<String, Boolean> message : messages.entrySet()) {
        if (!message.getValue()) {
            possibleMessages.add(message.getKey());
        }
      }
    }
    return possibleMessages;
  }
}
