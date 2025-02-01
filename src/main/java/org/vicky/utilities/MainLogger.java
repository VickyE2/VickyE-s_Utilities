/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import static org.vicky.global.Global.placeholderStorer;
import static org.vicky.global.Global.stringStorer;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.vicky_utils;

public class MainLogger {

  private final JavaPlugin plugin;

  public MainLogger(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  public void getHooks() {
    if (Bukkit.getPluginManager().getPlugin("VickySBA") != null) {
      plugin.getLogger().info("Plugin 'Vicky's SBA' is present. Adding additional Functionality");
    } else {
      plugin
          .getLogger()
          .warning(
              "Plugin 'Vicky's SBA' is missing... You probably don't need bedwars functionality."
                  + " Loaded either ways");
    }

    if (Bukkit.getPluginManager().getPlugin("VickyE-EP") != null) {
      plugin
          .getLogger()
          .info("Plugin 'Vicky's Economy Plugin' is present. Adding additional Functionality");
    } else {
      plugin
          .getLogger()
          .warning(
              "Plugin 'Vicky's Economy Plugin' is missing... You probably don't need Economyxaa"
                  + " functionality. Loaded either ways");
    }
    plugin.getLogger().info(vicky_utils.getHookedDependantPlugins());
  }

  public void getPlaceholders() {
    placeholderStorer.listPlaceholders("all", plugin);
  }

  public void getMechanics() {
    List<String> storedMechanics =
        stringStorer.returnStoredStrings(plugin.getName(), "mm_mechanic");
    if (storedMechanics != null) {
      for (String str : storedMechanics) {
        plugin.getLogger().info(str);
      }
    } else {
      plugin.getLogger().severe("No registered Mechanics found");
    }
  }

  public void logAll() {
    plugin.getLogger().info("MainLogger Will Proceed to log all...");
    getHooks();
    plugin.getLogger().info("Registered Stored PlaceHolders: ");
    getPlaceholders();
    plugin.getLogger().info("Registered MythicMobs Mechanics: ");
    getMechanics();
  }
}
