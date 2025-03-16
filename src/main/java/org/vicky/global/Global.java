/* Licensed under Apache-2.0 2024. */
package org.vicky.global;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.afk.AFKManager;
import org.vicky.handlers.CustomDamageHandler;
import org.vicky.mythic.MechanicRegistrar;
import org.vicky.utilities.Config;
import org.vicky.utilities.ConfigManager;
import org.vicky.utilities.DatabaseManager.HibernateDatabaseManager;
import org.vicky.utilities.DatabaseManager.SQLManager;
import org.vicky.utilities.DatabaseManager.utils.AggregatedClassLoader;
import org.vicky.utilities.PlaceholderStorer;
import org.vicky.utilities.StringStore;
import org.vicky.utilities.Theme.ThemeSelectionGuiListener;
import org.vicky.utilities.Theme.ThemeStorer;
import org.vicky.utilities.Theme.ThemeUnzipper;

public class Global {
  public static PlaceholderStorer placeholderStorer;
  public static StringStore stringStorer;
  public static CustomDamageHandler customDamageHandler;
  public static MechanicRegistrar mechanicRegistrar;
  public static SQLManager sqlManager;
  public static HibernateDatabaseManager databaseManager;
  public static ConfigManager globalConfigManager;
  public static Config config;
  public static AFKManager afkManager;
  public static ThemeSelectionGuiListener themeSelectionListener;
  public static ThemeUnzipper themeUnzipper;
  public static ThemeStorer storer;
  public static List<JavaPlugin> hookedPlugins = new ArrayList<>();
  public static AggregatedClassLoader classLoader = new AggregatedClassLoader(new ArrayList<>());
}
