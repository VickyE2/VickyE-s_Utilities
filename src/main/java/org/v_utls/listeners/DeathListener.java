package org.v_utls.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.v_utls.effects.Bleeding;

public class DeathListener implements Listener {

    private final Bleeding bleeding;

    public DeathListener(Bleeding bleeding) {
        this.bleeding = bleeding;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        bleeding.stopBleeding(entity); // Stop the bleeding effect
    }
}
