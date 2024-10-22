/* Licensed under Apache-2.0 2024. */
package org.v_utls.items_adder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiNames {

  private final Map<String, Map<String, List<String>>> pluginStringMap = new HashMap<>();

  // Method to store a string with an associated plugin name and identifier
  public void storeString(String pluginName, String identifier, String string) {
    // Get the map for the plugin, or create one if it doesn't exist
    pluginStringMap.computeIfAbsent(pluginName, k -> new HashMap<>());

    // Get the list for the identifier within the plugin's map, or create one if it doesn't exist
    pluginStringMap.get(pluginName).computeIfAbsent(identifier, k -> new ArrayList<>());

    // Add the string to the list under the identifier for the plugin
    pluginStringMap.get(pluginName).get(identifier).add(string);
  }

  // Method to return all stored strings for a given plugin and identifier
  public List<String> returnStoredStrings(String pluginName, String identifier) {
    // Get the plugin's map and then retrieve the list of strings for the identifier, or return an
    // empty list
    return pluginStringMap
        .getOrDefault(pluginName, new HashMap<>())
        .getOrDefault(identifier, new ArrayList<>());
  }

  // Method to return all stored strings for a given plugin across all identifiers
  public Map<String, List<String>> returnAllStringsForPlugin(String pluginName) {
    // Return the entire map of identifiers and their corresponding strings for the plugin
    return pluginStringMap.getOrDefault(pluginName, new HashMap<>());
  }

  // Method to list all stored strings across all plugins and identifiers
  public Map<String, Map<String, List<String>>> listAllStoredStrings() {
    return pluginStringMap;
  }
}
