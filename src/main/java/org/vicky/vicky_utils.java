/* Licensed under Apache-2.0 2024. */
package org.vicky;

import static org.vicky.global.Global.*;
import static org.vicky.utilities.DatabaseManager.SQLManager.generator;

import dev.jorel.commandapi.CommandAPICommand;
import jakarta.persistence.EntityManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.vicky.afk.AFKManager;
import org.vicky.effectsSystem.CustomEffect;
import org.vicky.effectsSystem.effects.Bleeding;
import org.vicky.expansions.PlaceholderExpansions;
import org.vicky.global.Global;
import org.vicky.guiparent.GuiCreator;
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
import org.vicky.utilities.DatabaseManager.templates.ExtendedPlayerBase;
import org.vicky.utilities.DatabaseManager.templates.Theme;
import org.vicky.utilities.DatabaseManager.templates.ThemeRegistry;
import org.vicky.utilities.DatabaseManager.utils.Hbm2DdlAutoType;
import org.vicky.utilities.Theme.ThemeSelectionGuiListener;
import org.vicky.utilities.Theme.ThemeStorer;
import org.vicky.utilities.Theme.ThemeUnzipper;

@SuppressWarnings({"deprecation", "UnstableApiUsage"})
public final class vicky_utils extends JavaPlugin {

  public static vicky_utils plugin;
  private static ClassLoader loader;
  private static final Map<String, String> pendingDBTemplates = new HashMap<>();
  private static final Map<String, String> pendingDBTemplatesUtils = new HashMap<>();
  private static final Map<String, CustomEffect> pendingEffects = new HashMap<>();
  boolean exceptionOccurred = false;

  public static vicky_utils getPlugin() {
    return plugin;
  }

  @Override
  public void onLoad() {
    try {
      loader = this.getClassLoader();
      config = new Config(this);
      config.addConfig(
          "defaults.AFKKickMessage",
          new GuiCreator.AllowedString(
              "You have been kicked for being AFK too long...This is for your own safety and to"
                  + " save server resources :D"));
      config.addConfig(
          "defaults.BTKMessages",
          new GuiCreator.AllowedList<>(
              List.of("You are no longer AFK... now do something", "Welcome back...")));
      config.addConfig(
          "defaults.AFKMessages",
          new GuiCreator.AllowedList<>(
              List.of("Your lazy ahh went to do smfn...", "Annnnnd AFK....")));
      config.addConfig("defaults.AFKKickTimer", new GuiCreator.AllowedInteger(-1));
      config.addConfig("defaults.NoAFKDamage", new GuiCreator.AllowedBoolean(true));
      config.addConfig("defaults.AFKKickThreshold", new GuiCreator.AllowedInteger(150000));
      config.addConfig("defaults.AFKThreshold", new GuiCreator.AllowedInteger(300000));
      config.addConfig("defaults.BFKInvulnerableTime", new GuiCreator.AllowedInteger(10000));
      config.addConfig("defaults.AllowBFKInvulnerable", new GuiCreator.AllowedBoolean(true));
      sqlManager =
          new SQLManagerBuilder()
              .addMappingClass(DatabasePlayer.class)
              .addMappingClass(Theme.class)
              .addMappingClass(ThemeRegistry.class)
              .addMappingClass(ExtendedPlayerBase.class)
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
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
        && Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
      this.getDataFolder().mkdirs();
      Bukkit.getLogger()
          .info(
              ANSIColor.colorizeMixed(
                  String.format(
                      """
gradient-30deg-right-#AA0000-#DDDD00-#00DDDD-#0000AA[
 _  _  __  ___  __ _  _  _  _ ____    _  _  ____  __  __    __  ____  __  ____  ____
/ )( \\(  )/ __)(  / )( \\/ )(// ___)  / )( \\(_  _)(  )(  )  (  )(_  _)(  )(  __)/ ___)
\\ \\/ / )(( (__  )  (  )  /   \\___ \\  ) \\/ (  )(   )( / (_/\\ )(   )(   )(  ) _) \\___ \\
 \\__/ (__)\\___)(__\\_)(__/    (____/  \\____/ (__) (__)\\____/(__) (__) (__)(____)(____/]
dark_gray[version %s]                                                    dark_gray[by %s]
""",
                      this.getDescription().getVersion(),
                      this.getDescription()
                          .getAuthors()
                          .toString()
                          .replace("[", "")
                          .replace("]", ""))));
      plugin = this;
      classLoader.getLoaders().add(this.getClassLoader());
      Thread.currentThread().setContextClassLoader(classLoader);

      globalConfigManager = new ConfigManager(this, "./global/global_configs.yml");
      globalConfigManager.loadConfigValues();
      config.registerConfigs();

      afkManager = new AFKManager(this);

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

      new CommandAPICommand("afk")
          .withAliases("goakf", "ibegone", "cya")
          .executesPlayer(
              executionInfo -> {
                afkManager.handleAfkCommand(executionInfo.sender());
              })
          .register(this);

      try {
        storer = new ThemeStorer();
        themeUnzipper = new ThemeUnzipper(this);

        themeUnzipper.downloadThemes();
      } catch (IOException e) {
        getLogger().severe("Failed to initialise themes...");
        throw new RuntimeException(e);
      }

      try {
        Map<String, CustomEffect> effects = new HashMap<>(pendingEffects);

        Bleeding bleeding = new Bleeding(this);
        effects.put("bleeding", bleeding);

        DeathMessages.add("bleeding", "{player} bled to death.", false);
        DeathMessages.add("bleeding", "{player} couldn't stop bleeding and died.", false);
        DeathMessages.add("bleeding", "{player} bled to death whilst fighting {killer}", true);

        getServer().getPluginManager().registerEvents(new DeathListener(effects), this);
        getServer().getPluginManager().registerEvents(new SpawnListener(customDamageHandler), this);

        getLogger()
            .info(
                ANSIColor.colorize(
                    "Effects and DeathMessages have been successfully Registered.",
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
      themeSelectionListener = new ThemeSelectionGuiListener(this);

      new PlaceholderExpansions(this).register();

      afkManager.startActivityChecker();

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
      getLogger()
          .info(
              ANSIColor.colorize(
                  "orange[Extracting folder:] yellow["
                      + folderPath
                      + "] orange[to] yellow["
                      + destinationFolder
                      + "]"));
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

  public static void hookDependantPlugin(@NotNull JavaPlugin plugin, @NotNull ClassLoader loader) {
    Bukkit.getLogger()
        .info(
            ANSIColor.colorize("New plugin hooked successfully: green[" + plugin.getName() + "]"));
    hookedPlugins.add(plugin);
    classLoader.getLoaders().add(loader);
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
    // Header colored in cyan.
    String header = ANSIColor.colorize("cyan[Hooked Plugins: ]");
    String plugins =
        hookedPlugins.stream()
            .map(
                plugin -> {
                  String rainbowMarker = "rainbow-purple-pink" + "[" + plugin.getName() + "]";
                  return "   - " + ANSIColor.rainbowColorize(rainbowMarker);
                })
            .collect(Collectors.joining("\n")); // Join with a newline between each entry.

    return header + "\n" + plugins;
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
  @Deprecated(forRemoval = true)
  public static void registerTemplatePackage(String jarName, String packageName) {
    pendingDBTemplates.put(packageName, jarName);
    new ContextLogger(ContextLogger.ContextType.FEATURE, "HIBERNATE-TEMPLATE")
        .printBukkit("Added template package " + ANSIColor.colorize("yellow[" + packageName + "]"));
  }

  public static void addTemplateClass(Class<? extends DatabaseTemplate> clazz) {
    sqlManager.addMappingClass(clazz);
  }

  @SafeVarargs
  public static void addTemplateClasses(Class<? extends DatabaseTemplate>... clazzez) {
    sqlManager.addMappingClasses(List.of(clazzez));
  }

  /**
   * Called by dependent plugins to register their custom player effects.
   */
  public static void registerCustomEffect(String effectName, CustomEffect customEffect) {
    pendingEffects.put(effectName, customEffect);
    new ContextLogger(ContextLogger.ContextType.SYSTEM, "EFFECTS")
        .printBukkit("Added custom effect" + ANSIColor.colorize("yellow[" + effectName + "]"));
  }

  /**
   * Called by dependent plugins to register their mapping class utilities like Enums, Classes and Objects.
   */
  @Deprecated(forRemoval = true)
  public static void registerTemplateUtilityPackage(String jarName, String packageName) {
    pendingDBTemplatesUtils.put(packageName, jarName);
    new ContextLogger(ContextLogger.ContextType.FEATURE, "HIBERNATE-TEMPLATE-UTIL")
        .printBukkit(
            "Added template utility package " + ANSIColor.colorize("yellow[" + packageName + "]"));
  }

  public SQLManager getSQLManager() {
    return sqlManager;
  }

  public EntityManager getEntityManager() {
    return HibernateUtil.getEntityManager();
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

  public static AFKManager getAfkManager() {
    return afkManager;
  }

  public static ConfigManager getGlobalCOnfigManager() {
    return globalConfigManager;
  }

  public static Config getConfigMapper() {
    return config;
  }

  public static void addClassLoader(ClassLoader clazzLoader) {
    classLoader.getLoaders().add(clazzLoader);
  }

  public ClassLoader getLoader() {
    return loader;
  }
}
