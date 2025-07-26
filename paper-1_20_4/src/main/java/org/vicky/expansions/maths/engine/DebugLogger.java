/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;

public class DebugLogger {
  public final Map<UUID, Boolean> toggles = new HashMap<>();

  public void log(Player player, String message) {
    if (toggles.getOrDefault(player.getUniqueId(), false))
      player.sendMessage("[MIM DEBUG] " + message);
  }
}
