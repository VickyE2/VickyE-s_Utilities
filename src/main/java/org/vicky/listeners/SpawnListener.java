/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.vicky.handlers.CustomDamageHandler;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.apis.DatabasePlayerAPI;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;

public class SpawnListener implements Listener {
  private final CustomDamageHandler customDamageHandler;

  public SpawnListener(CustomDamageHandler customDamageHandler) {
    this.customDamageHandler = customDamageHandler;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    if (!(new DatabasePlayerAPI().getPlayer(player.getUniqueId().toString()) == null)) {
      new ContextLogger(ContextLogger.ContextType.FEATURE, "HIBERNATE-PLAYER")
          .printBukkit(
              ANSIColor.colorize(
                  "cyan[Creating instanced player for new player: ]" + player.getName()));
      DatabasePlayer instancedDatabasePlayer = new DatabasePlayer();
      instancedDatabasePlayer.setId(player.getUniqueId());
      instancedDatabasePlayer.setFirstTime(true);
      new DatabasePlayerAPI().createPlayer(instancedDatabasePlayer);
    }
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    customDamageHandler.resetCustomDamageCauses(player); // Reset custom damage causes
  }
}
