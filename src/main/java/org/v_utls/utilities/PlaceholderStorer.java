/* Licensed under Apache-2.0 2024. */
package org.v_utls.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PlaceholderStorer {

  // Inner class to store placeholder and description
  public record PlaceholderInfo(String placeholder, String description) {}

  // Map to store placeholders by plugin name
  private final Map<String, List<PlaceholderInfo>> placeholders = new HashMap<>();

  // Overloaded method to store placeholder with default description
  public void storePlaceholder(String identifier, String placeholder, String pluginName) {
    storePlaceholder(identifier, placeholder, pluginName, "No Description Given");
  }

  // Method to store a placeholder with a description
  public void storePlaceholder(
      String identifier, String placeholder, String pluginName, String description) {
    String fullPlaceholder =
        "%" + identifier + "_" + placeholder + "%"; // Formatting like "%identifier_placeholder%"

    // If plugin doesn't exist in the map, add it
    placeholders.computeIfAbsent(pluginName, k -> new ArrayList<>());

    // Add the placeholder info (placeholder + description)
    placeholders.get(pluginName).add(new PlaceholderInfo(fullPlaceholder, description));
  }

  // Method to list placeholders based on type
  public void listPlaceholders(String type, JavaPlugin plugin) {
    // If type is "all", list all placeholders by plugin
    if (type.equalsIgnoreCase("all")) {
      for (Map.Entry<String, List<PlaceholderInfo>> entry : placeholders.entrySet()) {
        plugin
            .getLogger()
            .info(
                ANSIColor.colorize("Plugin: ", ANSIColor.CYAN_BOLD)
                    + ANSIColor.colorize(entry.getKey(), ANSIColor.RED_BOLD));
        for (PlaceholderInfo info : entry.getValue()) {
          plugin
              .getLogger()
              .info(ANSIColor.colorize("  Placeholder: " + info.placeholder(), ANSIColor.BLUE));
          plugin
              .getLogger()
              .info(ANSIColor.colorize("Description: " + info.description(), ANSIColor.LIGHT_GRAY));
        }
      }
    } else {
      // If type matches a specific plugin name, list placeholders for that plugin
      if (placeholders.containsKey(type)) {
        plugin
            .getLogger()
            .info(
                ANSIColor.colorize("Plugin: ", ANSIColor.CYAN_BOLD)
                    + ANSIColor.colorize(type, ANSIColor.RED_BOLD));
        for (PlaceholderInfo info : placeholders.get(type)) {
          plugin
              .getLogger()
              .info(ANSIColor.colorize("  Placeholder: " + info.placeholder(), ANSIColor.BLUE));
          plugin
              .getLogger()
              .info(ANSIColor.colorize("Description: " + info.description(), ANSIColor.LIGHT_GRAY));
        }
      } else {
        plugin
            .getLogger()
            .warning(
                ANSIColor.colorize(
                    "No placeholders found for plugin: '" + type + "'. Are they stored?",
                    ANSIColor.RED_BOLD));
      }
    }
  }

  // Method to list placeholders for players in chat
  public void listToPlayer(String type, Player player) {
    // If type is "all", list all placeholders by plugin
    if (type.equalsIgnoreCase("all")) {
      for (Map.Entry<String, List<PlaceholderInfo>> entry : placeholders.entrySet()) {
        player.sendMessage(ChatColor.AQUA + "Plugin: " + entry.getKey());
        for (PlaceholderInfo info : entry.getValue()) {
          player.sendMessage(ChatColor.BLUE + "  Placeholder: " + info.placeholder());
          player.sendMessage(ChatColor.GRAY + "    Description: " + info.description());
        }
      }
    } else {
      // If type matches a specific plugin name, list placeholders for that plugin
      if (placeholders.containsKey(type)) {
        player.sendMessage(ChatColor.AQUA + "Plugin: " + type);
        for (PlaceholderInfo info : placeholders.get(type)) {
          player.sendMessage(ChatColor.BLUE + "  Placeholder: " + info.placeholder());
          player.sendMessage(ChatColor.GRAY + "    Description: " + info.description());
        }
      } else {
        player.sendMessage(
            ChatColor.RED + "No placeholders found for plugin: " + type + ". Are they stored?");
      }
    }
  }
}
