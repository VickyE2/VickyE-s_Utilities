package org.v_utls.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.MetadataValue;
import org.v_utls.effects.CustomEffect;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.v_utls.utilities.DeathMessages.getMessages;

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

        // Check if the entity has the custom damage metadata
        if (entity.hasMetadata("customDamageCause") && entity instanceof Player) {
            for (MetadataValue value : entity.getMetadata("customDamageCause")) {
                String customCause = value.asString();

                // Get the messages for the custom cause from DeathMessages
                List<String> messages = getMessages(customCause);

                if (!messages.isEmpty()) {
                    // Broadcast a random death message for the specific cause
                    String deathMessage = messages.get(new Random().nextInt(messages.size()));
                    event.getEntity().getServer().broadcastMessage(deathMessage.replace("{player}", entity.getName()));
                }
            }
        }
        for (Map.Entry<String, CustomEffect> entry : customEffects.entrySet()) {
            CustomEffect effect = entry.getValue();
            if (effect.isEntityAffected(entity)) {
                effect.stopEffect(entity);  // Stop the effect when the entity dies
            }
        }
    }
}
