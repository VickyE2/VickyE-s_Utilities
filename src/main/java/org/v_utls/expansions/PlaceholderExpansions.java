package org.v_utls.expansions;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.v_utls.vicky_utils;
import org.v_utls.utilities.metaDataChecker_TF;

public class PlaceholderExpansions extends PlaceholderExpansion{

    private final vicky_utils plugin;

        public PlaceholderExpansions(vicky_utils plugin) {
            this.plugin = plugin;
        }

        // This method must return true for the placeholder expansion to work
        @Override
        public boolean canRegister() {
            return true;
        }

        // Return the identifier that you'll use in the placeholder (e.g., %myeffects_<placeholder>%)
        @Override
        public String getIdentifier() {
            return "vicky_utils";
        }

        // Return the author of the placeholder expansion
        @Override
        public String getAuthor() {
            return plugin.getDescription().getAuthors().toString();
        }

        // Return the version of the placeholder expansion
        @Override
        public String getVersion() {
            return plugin.getDescription().getVersion();
        }

        // This method is called when the placeholder is requested
        @Override
        public String onPlaceholderRequest(Player player, @NotNull String identifier) {
            if (player == null) {
                return "";
            }
            if (identifier.equals("isBleeding")){
                boolean isBleeding = metaDataChecker_TF.hasMetaData(player, "isBleeding_v");
                return Boolean.toString(isBleeding);
            }

            // Return null if the placeholder is not handled
            return null;
        }

}
