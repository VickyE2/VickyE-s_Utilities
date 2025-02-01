/* Licensed under Apache-2.0 2024. */
package org.vicky.global;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.handlers.CustomDamageHandler;
import org.vicky.mythic.MechanicRegistrar;
import org.vicky.utilities.ConfigManager;
import org.vicky.utilities.DatabaseManager.HibernateDatabaseManager;
import org.vicky.utilities.DatabaseManager.SQLManager;
import org.vicky.utilities.PlaceholderStorer;
import org.vicky.utilities.StringStore;

public class Global {
  public static PlaceholderStorer placeholderStorer;
  public static StringStore stringStorer;
  public static CustomDamageHandler customDamageHandler;
  public static MechanicRegistrar mechanicRegistrar;
  public static SQLManager databaseManager;
  public static ConfigManager configManager;

  public static List<JavaPlugin> hookedPlugins = new ArrayList<>();
}
