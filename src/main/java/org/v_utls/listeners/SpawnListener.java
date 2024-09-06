package org.v_utls.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.v_utls.handlers.CustomDamageHandler;

import java.util.List;
import java.util.Random;

public class SpawnListener implements Listener {
    private final CustomDamageHandler customDamageHandler;


    public SpawnListener(CustomDamageHandler customDamageHandler) {
        this.customDamageHandler = customDamageHandler;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        customDamageHandler.resetCustomDamageCauses(player); // Reset custom damage causes
    }
}
