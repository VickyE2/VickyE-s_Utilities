package org.v_utls.utilities;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceholderStorer {

    // Map to store placeholders by plugin name
    private final Map<String, List<String>> placeholders = new HashMap<>();

    // Method to store a placeholder with the plugin name
    public void storePlaceholder(String identifier, String placeholder, String pluginName) {
        String fullPlaceholder = "%" + identifier + "_" + placeholder + "%"; // Formatting like "%identifier_placeholder%"

        // If plugin doesn't exist in the map, add it
        placeholders.computeIfAbsent(pluginName, k -> new ArrayList<>());

        // Add placeholder to the plugin's list if not already present
        if (!placeholders.get(pluginName).contains(fullPlaceholder)) {
            placeholders.get(pluginName).add(fullPlaceholder);
        }
    }

    // Method to list placeholders based on type
    public void listPlaceholders(String type) {
        // If type is "all", list all placeholders by plugin
        if (type.equalsIgnoreCase("all")) {
            for (Map.Entry<String, List<String>> entry : placeholders.entrySet()) {
                Bukkit.getLogger().info("Plugin: " + entry.getKey());
                for (String placeholder : entry.getValue()) {
                    Bukkit.getLogger().info("  Placeholder: " + placeholder);
                }
            }
        } else {
            // If type matches a specific plugin name, list placeholders for that plugin
            if (placeholders.containsKey(type)) {
                Bukkit.getLogger().info("Plugin: " + type);
                for (String placeholder : placeholders.get(type)) {
                    Bukkit.getLogger().info("  Placeholder: " + placeholder);
                }
            } else {
                Bukkit.getLogger().warning("No placeholders found for plugin: " + type);
            }
        }
    }
}
