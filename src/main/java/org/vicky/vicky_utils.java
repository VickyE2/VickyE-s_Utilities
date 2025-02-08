/* Licensed under Apache-2.0 2024-2025. */
package org.vicky;

import static org.vicky.global.Global.*;
import static org.vicky.utilities.DatabaseManager.SQLManager.generator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.vicky.effects.Bleeding;
import org.vicky.effects.CustomEffect;
import org.vicky.expansions.PlaceholderExpansions;
import org.vicky.global.Global;
import org.vicky.handlers.CustomDamageHandler;
import org.vicky.listeners.DeathListener;
import org.vicky.listeners.SpawnListener;
import org.vicky.mythic.MechanicRegistrar;
import org.vicky.utilities.*;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.HibernateDatabaseManager;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.SQLManager;
import org.vicky.utilities.DatabaseManager.SQLManagerBuilder;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;
import org.vicky.utilities.DatabaseManager.templates.Theme;
import org.vicky.utilities.DatabaseManager.templates.ThemeRegistry;
import org.vicky.utilities.DatabaseManager.utils.Hbm2DdlAutoType;
import org.vicky.utilities.Theme.ThemeStorer;
import org.vicky.utilities.Theme.ThemeUnzipper;

public final class vicky_utils extends JavaPlugin {

  public static vicky_utils plugin;
  private ClassLoader loader;
  private static final Map<String, String> pendingDBTemplates = new HashMap<>();
  private static final Map<String, String> pendingDBTemplatesUtils = new HashMap<>();
  boolean exceptionOccurred = false;

  public static vicky_utils getPlugin() {
    return plugin;
  }

  @Override
  public void onLoad() {
    try {
      loader = Bukkit.getPluginManager().getPlugin("Vicky-s_Utilities").getClass().getClassLoader();
      sqlManager =
          new SQLManagerBuilder()
              .addMappingClass(DatabasePlayer.class)
              .addMappingClass(Theme.class)
              .addMappingClass(ThemeRegistry.class)
              .setUsername(generator.generate(20, true, true, true, false))
              .setPassword(generator.generatePassword(30))
              .setShowSql(false)
              .setFormatSql(false)
              .setDialect("org.hibernate.community.dialect.SQLiteDialect")
              .setDdlAuto(Hbm2DdlAutoType.UPDATE)
              .build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onEnable() {

    // Plugin startup logic
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
        && Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {

      plugin = this;

      configManager = new ConfigManager(this, "./global/global_configs.yml");
      configManager.loadConfigValues();
      Config config = new Config(this);
      config.registerConfigs();

      Global.placeholderStorer = new PlaceholderStorer();
      Global.stringStorer = new StringStore();
      Global.customDamageHandler = new CustomDamageHandler(this);
      Global.mechanicRegistrar = new MechanicRegistrar(this);

      sqlManager.configureSessionFactory();
      sqlManager.startDatabase();
      databaseManager = new HibernateDatabaseManager();
      MainLogger logger1 = new MainLogger(this);

      List<String> folders = new ArrayList<>();
      folders.add("themes");

      getLogger().info(ANSIColor.colorize("purple[Rendering Vicky's Utilities Accessible]"));

      if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
        FileManager fileManager = new FileManager(this);
        getLogger().info("ItemsAdder is present. Extracting Default Files");
        List<String> files = List.of("contents/vicky_utls/");
        fileManager.extractDefaultIAAssets(files);
      } else {
        getLogger().warning("ItemsAdder isn't present. Defaulting to basic settings");
      }

      for (String folderToExtract : folders) {
        File desti = new File(getDataFolder() + "/themes/");
        if (!desti.exists()) {
          desti.mkdir();
        }
        extractFolderFromJar(folderToExtract, desti);
      }

      try {
        storer = new ThemeStorer();
        themeUnzipper = new ThemeUnzipper(this);

        themeUnzipper.downloadThemes();
      } catch (IOException e) {
        getLogger().severe("Failed to initialise themes...");
        throw new RuntimeException(e);
      }

      try {
        Map<String, CustomEffect> effects = new HashMap<>();

        Bleeding bleeding = new Bleeding(this);
        effects.put("bleeding", bleeding);

        DeathMessages.add("bleeding", "{player} bled to death.");
        DeathMessages.add("bleeding", "{player} couldn't stop the bleeding.");

        getServer().getPluginManager().registerEvents(new DeathListener(effects), this);
        getServer().getPluginManager().registerEvents(new SpawnListener(customDamageHandler), this);

        getLogger()
            .info(
                ANSIColor.colorize(
                    "Effects and DeathMessages have been sucessfully Registered.",
                    ANSIColor.PURPLE));
      } catch (Exception e) {
        exceptionOccurred = true;
        getLogger()
            .info(
                ANSIColor.colorize(
                    "Unable to register Effects and DeathMessages..... Error:" + e.getMessage(),
                    ANSIColor.RED_BOLD));
      } finally {
        if (exceptionOccurred) {
          getLogger()
              .info(
                  ANSIColor.colorize(
                      "Continuing with Loading Some Errors Might Occur", ANSIColor.LIGHT_RED));
        }
      }

      mechanicRegistrar.registerAll();

      new PlaceholderExpansions(this).register();

      placeholderStorer.storePlaceholder(
          "vicky_utils",
          "isBleeding",
          this.getName(),
          "Returns weather the player is currently being affected by the custom BLEEDING mechanic"
              + " of mythic mobs.");

      MainLogger logger = new MainLogger(this);
      logger.getHooks();

      getServer()
          .getScheduler()
          .scheduleSyncDelayedTask(
              this,
              () -> {
                getLogger()
                    .info(
                        ANSIColor.colorize(
                            "Placeholders from Plugin and its Addons have been registered: ",
                            ANSIColor.CYAN));
                logger1.getPlaceholders();
                getLogger()
                    .info(ANSIColor.colorize("Getting Registered Mechanics", ANSIColor.CYAN));
                logger1.getMechanics();
              });

    } else if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
      exceptionOccurred = true;
      getLogger()
          .severe(
              ANSIColor.colorize(
                  "Could not find PlaceholderAPI! This plugin is required.", ANSIColor.RED));
      Bukkit.getPluginManager().disablePlugin(this);
    } else {
      exceptionOccurred = true;
      getLogger()
          .severe(
              ANSIColor.colorize(
                  "Could not find MythicAPI! Ensure MythicMobs is Present.", ANSIColor.RED));
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    if (exceptionOccurred) {
      getLogger()
          .info(
              ANSIColor.colorize(
                  "Rendering Utilities Inaccessible. Dependant Plugins will not Work",
                  ANSIColor.RED_BOLD));
    } else {
      getLogger().info("Rendering Utilities Inaccessible");
    }
    if (HibernateUtil.getSessionFactory() != null) HibernateUtil.shutdown();
  }

  public void extractFolderFromJar(String folderPath, File destinationFolder) {
    if (!destinationFolder.exists()) {
      destinationFolder.mkdirs(); // Create the destination folder if it doesn't exist
    }

    try {
      getLogger().info("Extracting folder: " + folderPath + " to " + destinationFolder);
      URL jarUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
      JarFile jarFile = new JarFile(new File(jarUrl.toURI()));

      Enumeration<JarEntry> entries = jarFile.entries();
      boolean foundEntries = false;

      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();

        // Only process entries that are within the specified folder
        if (entry.getName().startsWith(folderPath + "/") && !entry.isDirectory()) {
          foundEntries = true; // Mark that we found at least one entry
          File destFile =
              new File(
                  destinationFolder,
                  entry.getName().substring(folderPath.length() + 1)); // Adjust index for the slash

          File parent = destFile.getParentFile();
          if (!parent.exists()) {
            parent.mkdirs(); // Create parent directories if needed
          }

          // Copy the file from the JAR to the destination
          try (InputStream is = jarFile.getInputStream(entry);
              FileOutputStream fos = new FileOutputStream(destFile)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
          }
        }
      }

      if (!foundEntries) {
        getLogger().info("No entries found in the JAR file for: " + folderPath);
      }

      jarFile.close();
    } catch (Exception e) {
      getLogger().severe("Failed to extract folder from JAR: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public CustomDamageHandler getCustomDamageHandler() {
    return customDamageHandler;
  }

  public static void hookDependantPlugin(@NotNull JavaPlugin plugin) {
    getPlugin()
        .getLogger()
        .info(
            ANSIColor.colorize("New plugin hooked successfully: green[" + plugin.getName() + "]"));
    hookedPlugins.add(plugin);
  }

  public static void unhookDependantPlugin(@NotNull JavaPlugin plugin) {
    boolean removed = hookedPlugins.remove(plugin);
    if (removed) {
      getPlugin()
          .getLogger()
          .info(ANSIColor.colorize("Plugin purple[" + plugin.getName() + "] has been unhooked"));
    } else {
      getPlugin()
          .getLogger()
          .warning(
              ANSIColor.colorize(
                  "Plugin bold["
                      + plugin.getName()
                      + "] wasn't found among hooked plugins. Please contact the plugin developers"
                      + " if this isn't a development environment."));
    }
  }

  public static String getHookedDependantPlugins() {
    StringBuilder plugins = new StringBuilder();
    plugins.append(ANSIColor.colorize("cyan[Hooked Plugins: ] \n"));
    for (JavaPlugin plugin : hookedPlugins) {
      plugins.append(ANSIColor.colorize("   - purple[" + plugin.getName() + "]"));
    }
    return plugins.toString();
  }

  public static Map<String, String> getPendingDBTemplates() {
    return pendingDBTemplates;
  }

  public static Map<String, String> getPendingDBTemplatesUtils() {
    return pendingDBTemplatesUtils;
  }

  /**
   * Called by dependent plugins to register their mapping classes.
   */
  public static void registerTemplatePackage(String jarName, String packageName) {
    pendingDBTemplates.put(packageName, jarName);
    new ContextLogger(ContextLogger.ContextType.FEATURE, "HIBERNATE-TEMPLATE")
        .printBukkit("Added template package " + ANSIColor.colorize("yellow[" + packageName + "]"));
  }

  /**
   * Called by dependent plugins to register their mapping class utilities like Enums, Classes and Objects.
   */
  public static void registerTemplateUtilityPackage(String jarName, String packageName) {
    pendingDBTemplatesUtils.put(packageName, jarName);
    new ContextLogger(ContextLogger.ContextType.FEATURE, "HIBERNATE-TEMPLATE-UTIL")
        .printBukkit(
            "Added template utility package " + ANSIColor.colorize("yellow[" + packageName + "]"));
  }

  public SQLManager getSQLManager() {
    return sqlManager;
  }

  public HibernateDatabaseManager getDatabaseManager() {
    return databaseManager;
  }

  public void createFolder(String name) {
    File folder = new File(getDataFolder(), name);
    if (!folder.exists()) {
      try {
        folder.mkdir();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public ClassLoader getLoader() {
    return loader;
  }
}
