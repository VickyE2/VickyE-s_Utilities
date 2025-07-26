/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import org.bukkit.entity.Player;

public class metaDataChecker_TF {
  public static boolean hasMetaData(Player player, String metaData) {
    return player.hasMetadata(metaData); // Example using metadata API
  }
}
