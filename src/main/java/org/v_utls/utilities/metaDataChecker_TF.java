package org.v_utls.utilities;

import org.bukkit.entity.Player;

public class metaDataChecker_TF {
    public static boolean hasMetaData(Player player, String metaData) {
        return player.hasMetadata(metaData); // Example using metadata API
    }
}
