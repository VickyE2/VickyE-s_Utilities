package org.v_utls.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.v_utls.utilities.CustomDamageEvent;

import java.util.Random;

public class DeathMessageListener implements Listener {

    private final Plugin plugin;

    public DeathMessageListener(Plugin plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Check if the entity has the custom damage metadata
        if (entity.hasMetadata("customDamageCause") && entity instanceof Player) {
            for (MetadataValue value : entity.getMetadata("customDamageCause")) {
                String customCause = value.asString();

                if (customCause.equals("bleeding")){
                    String[] bleedingMessages = {
                            entity.getName() + " bled!",
                            entity.getName() + " couldn't stop bleeding.",
                            entity.getName() + " was drained by bleeding.",
                            entity.getName() + " bled to death"
                    };
                    event.getEntity().getServer().broadcastMessage(bleedingMessages[new Random().nextInt(bleedingMessages.length)]);
                }
            }
        }
    }
}

