/* Licensed under Apache-2.0 2024. */
package org.vicky.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.vicky.handlers.CustomDamageHandler;

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
