/* Licensed under Apache-2.0 2024. */
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
import org.vicky.utilities.DatabaseManager.dao_s.DatabasePlayerDAO;
import org.vicky.utilities.DatabaseManager.dao_s.MusicPieceDAO;
import org.vicky.utilities.DatabaseManager.dao_s.MusicPlayerDAO;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;
import org.vicky.utilities.DatabaseManager.templates.MusicPlayer;

public class SpawnListener implements Listener {
  private final CustomDamageHandler customDamageHandler;
  private static final String[] defaultSongs = {"vicky_utils_symphony1"};

  public SpawnListener(CustomDamageHandler customDamageHandler) {
    this.customDamageHandler = customDamageHandler;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    if (new DatabasePlayerDAO().findById(player.getUniqueId()).isEmpty()) {
      new ContextLogger(ContextLogger.ContextType.FEATURE, "HIBERNATE-PLAYER")
          .printBukkit(
              ANSIColor.colorize(
                  "cyan[Creating instanced player for new player: ]" + player.getName()));
      DatabasePlayer instancedDatabasePlayer = new DatabasePlayer();
      instancedDatabasePlayer.setId(player.getUniqueId());
      instancedDatabasePlayer.setFirstTime(true);
      new DatabasePlayerDAO().save(instancedDatabasePlayer);
    } else {
      DatabasePlayer instancedDatabasePlayer =
          new DatabasePlayerDAO().findById(player.getUniqueId()).get();
      instancedDatabasePlayer.setFirstTime(false);
      new DatabasePlayerAPI()
          .updatePlayer(player.getUniqueId().toString(), instancedDatabasePlayer);
    }
    if (new MusicPlayerDAO().findById(player.getUniqueId()).isEmpty()) {
      new ContextLogger(ContextLogger.ContextType.FEATURE, "HIBERNATE-PLAYER")
          .printBukkit(
              ANSIColor.colorize(
                  "cyan[Creating instanced music player for new player: ]" + player.getName()));
      MusicPlayer instancedMusicPlayer = new MusicPlayer();
      instancedMusicPlayer.setDatabasePlayer(
          new DatabasePlayerDAO().findById(player.getUniqueId()).get());
      for (String song : defaultSongs) {
        instancedMusicPlayer.addPiece(new MusicPieceDAO().findById(song));
      }
      new MusicPlayerDAO().save(instancedMusicPlayer);
    }
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    customDamageHandler.resetCustomDamageCauses(player); // Reset custom damage causes
  }
}
