package org.v_utls.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.metadata.MetadataValue;

public class DamageListener implements Listener {


    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        // Check if the entity has custom metadata for damage cause
        if (entity.hasMetadata("customDamageCause")) {
            for (MetadataValue value : entity.getMetadata("customDamageCause")) {
                String customCause = value.asString();
                event.setCancelled(true); // Optionally cancel the default damage handling
                entity.sendMessage("You were damaged by custom cause: " + customCause);
            }
        }
    }
}

