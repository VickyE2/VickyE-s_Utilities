/* Licensed under Apache-2.0 2024. */
package org.vicky;

import static org.vicky.global.Global.*;
import static org.vicky.kotlinUtils.UtilsKt.mortalise;
import static org.vicky.utilities.DatabaseManager.SQLManager.generator;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandPermission;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.vicky.afk.AFKManager;
import org.vicky.betterHUD.bossbar.BossbarManager;
import org.vicky.betterHUD.vickyUtilsCompat.VickyUtilsCompat;
import org.vicky.deathMessage.DeathCause;
import org.vicky.deathMessage.DeathCauseRegistry;
import org.vicky.ecosystem.server.CommunicatorServer;
import org.vicky.effectsSystem.EffectRegistry;
import org.vicky.effectsSystem.StatusEffect;
import org.vicky.effectsSystem.effects.Bleeding;
import org.vicky.effectsSystem.enums.EffectArrangementType;
import org.vicky.expansions.BossbarPlaceholderExpansion;
import org.vicky.expansions.EffectsPlaceholderExpansion;
import org.vicky.expansions.maths.MathsPlaceholderExpansions;
import org.vicky.guiparent.CancelAllGuiListener;
import org.vicky.guiparent.DefaultGuiListener;
import org.vicky.handlers.CustomDamageHandler;
import org.vicky.listeners.DeathListener;
import org.vicky.listeners.SpawnListener;
import org.vicky.music.MusicRegistry;
import org.vicky.music.utils.MusicBuilder;
import org.vicky.music.utils.MusicPiece;
import org.vicky.music.utils.Sound;
import org.vicky.mythic.MythicRegistrar;
import org.vicky.utilities.*;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.HibernateDatabaseManager;
import org.vicky.utilities.DatabaseManager.HibernateUtil;
import org.vicky.utilities.DatabaseManager.SQLManager;
import org.vicky.utilities.DatabaseManager.SQLManagerBuilder;
import org.vicky.utilities.DatabaseManager.templates.*;
import org.vicky.utilities.DatabaseManager.utils.Hbm2DdlAutoType;
import org.vicky.utilities.PermittedObjects.*;
import org.vicky.utilities.Theme.ThemeSelectionGuiListener;
import org.vicky.utilities.Theme.ThemeStorer;
import org.vicky.utilities.Theme.ThemeUnzipper;

@SuppressWarnings({"deprecation", "UnstableApiUsage"})
public final class vicky_utils extends JavaPlugin {
  private static final Map<String, String> pendingDBTemplates = new HashMap<>();
  private static final Map<String, String> pendingDBTemplatesUtils = new HashMap<>();
  private static final Map<StatusEffect, List<DeathCause>> pendingEffects = new HashMap<>();
  public static vicky_utils plugin;
  private static Listener listener;
  private static ClassLoader loader;
  boolean exceptionOccurred = false;

  public static vicky_utils getPlugin() {
    return plugin;
  }

  @Deprecated(forRemoval = true)
  public static void hookDependantPlugin(@NotNull JavaPlugin plugin, @NotNull ClassLoader loader) {
    Bukkit.getLogger()
        .info(
            ANSIColor.colorize("New plugin hooked successfully: green[" + plugin.getName() + "]"));
    hookedPlugins.add(plugin);
    classLoader.getLoaders().add(loader);
  }

  @Deprecated(forRemoval = true)
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

  @Deprecated(forRemoval = true)
  public static String getHookedDependantPlugins() {
    // Header colored in cyan.
    String header = ANSIColor.colorize("cyan[Hooked Plugins: ]");
    String plugins =
        hookedPlugins.stream()
            .map(
                plugin -> {
                  String rainbowMarker = "gradient-#FC0072-#990099" + "[" + plugin.getName() + "]";
                  return "   - " + ANSIColor.gradientColorize(rainbowMarker);
                })
            .collect(Collectors.joining("\n"));
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
  public static void registerStatusEffect(
      List<DeathCause> deathMessages, StatusEffect StatusEffect) {
    pendingEffects.put(StatusEffect, deathMessages);
    new ContextLogger(ContextLogger.ContextType.SYSTEM, "EFFECTS")
        .printBukkit(
            "Added custom effect" + ANSIColor.colorize("yellow[" + StatusEffect.getKey() + "]"));
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

  public static AFKManager getAfkManager() {
    return afkManager;
  }

  public static MythicRegistrar getMechanicRegistrar() {
    return MythicRegistrar.getInstance();
  }

  public static ConfigManager getGlobalConfigManager() {
    return globalConfigManager;
  }

  public static Config getConfigMapper() {
    return config;
  }

  public static VickyUtilsCompat getCompact() {
    return utilsCompact;
  }

  public static void addClassLoader(ClassLoader clazzLoader) {
    classLoader.getLoaders().add(clazzLoader);
  }

  @Override
  public void onLoad() {
    try {
      plugin = this;
      loader = this.getClassLoader();
      utilsCompact = new VickyUtilsCompat();
      doDefaults();
      CommunicatorServer.startupCommunicationServer();
      CommunicatorServer.getInstance()
          .register("Vicky-s_Utilities", loader, new VickyUtilsCommunicator());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onEnable() {
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
        && Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {
      Bukkit.getLogger()
          .info(
              ANSIColor.colorizeMixed(
                  String.format(
                      """
gradient-10deg-right-#AA0000-#DDDD00[
 _  _  __  ___  __ _  _  _  _ ____    _  _  ____  __  __    __  ____  __  ____  ____
/ )( \\(  )/ __)(  / )( \\/ )(// ___)  / )( \\(_  _)(  )(  )  (  )(_  _)(  )(  __)/ ___)
\\ \\/ / )(( (__  )  (  )  /   \\___ \\  ) \\/ (  )(   )( / (_/\\ )(   )(   )(  ) _) \\___ \\
 \\__/ (__)\\___)(__\\_)(__/    (____/  \\____/ (__) (__)\\____/(__) (__) (__)(____)(____/]
                                                                         dark_gray[%s]""",
                      this.getDescription().getVersion())));
      this.getDataFolder().mkdirs();
      listener = new Listener() {};
      utilsCompact.register();
      Thread.currentThread().setContextClassLoader(classLoader);
      prepareGlobals();
      registerTypeGuiListenerInstances();
      MainLogger logger = new MainLogger(this);
      List<String> folders = new ArrayList<>();
      folders.add("themes");
      // Kotlin object, called from Java
      Bukkit.getPluginManager().registerEvents(new EffectRegistry.EffectRegistryListener(), this);
      Bukkit.getScheduler()
          .runTaskTimer(this, org.vicky.musicPlayer.MusicPlayer.INSTANCE::tickAll, 1L, 1L);

      registerMusicBuiltins();

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
        File homeFolder = new File(getDataFolder() + "/themes/");
        if (!homeFolder.exists()) {
          homeFolder.mkdir();
        }
        extractFolderFromJar(folderToExtract, homeFolder);
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
        getInBuiltEffects();
        Map<StatusEffect, List<DeathCause>> effects = new HashMap<>(pendingEffects);
        for (var effect : effects.entrySet()) {
          EffectRegistry.getInstance(EffectRegistry.class)
              .registerMechanicEffect(effect.getKey(), null);
          for (var message : effect.getValue()) {
            DeathCauseRegistry.INSTANCE.registerCause(effect.getKey().getKey(), message);
          }
        }
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
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

      EffectRegistry.getInstance(EffectRegistry.class).writeAllToDisk();
      MythicRegistrar.getInstance().registerAll();
      themeSelectionListener = new ThemeSelectionGuiListener(this);
      new EffectsPlaceholderExpansion(this).register();
      new BossbarPlaceholderExpansion(this).register();
      new MathsPlaceholderExpansions(this).register();

      // BetterHudAPI.inst()
      //     .getTriggerManager()
      //     .addTrigger("boss_entity_near_player", (fun) -> new BossEntityTrigger());

      afkManager.startActivityChecker();

      placeholderStorer.storePlaceholder(
          "vicky_utils",
          "isBleeding",
          this.getName(),
          "Returns weather the player is currently being affected by the custom BLEEDING mechanic"
              + " of mythic mobs.");

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
                logger.getPlaceholders();
                getLogger()
                    .info(ANSIColor.colorize("Getting Registered Mechanics", ANSIColor.CYAN));
                logger.getMechanics();
              });
      registerCommands();
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

  private void registerMusicBuiltins() {
    var registry = MusicRegistry.getInstance(MusicRegistry.class);
    List<MusicPiece> pieces = new ArrayList<>();
    var symphony1Builder = new MusicBuilder();

    pieces.addAll(
        List.of(
            new MusicPiece(
                "vicky_utils_symphony1",
                "Symphony 1",
                List.of(
                    symphony1Builder.ofScore(
                        Sound.PIANO,
                        "C+,D+,E+,C+,D+,E+,C+,D+,E+,C++—C+,C++—D+,C++—E+,"
                            + "@[cello1][B],D+,E+,B,D+,E+,B,D+,E+,B++—B,B++—D+,B++—E+,"
                            + "@[cello2][A],D+,E+,A,D+,E+,A,D+,E+,A++—A,A++—D+,A++—E+,"
                            + "@[cello3][G],D+,E+,G,D+,E+,G,D+,E+,G++—C++,G++—G+,G++—E+,@[cello4][G++—C+],"
                            + "D+—G,E+—C,C+,D+—G,E+—C,C+,D+—G,E+—C,C+,D+—G,E+—C,"
                            + "B,G—D+,B-—E+,B,G—D+,B-—E+,B,G—D+,B-—E+,B,G—D+,B-—E+,"
                            + "A,E—D+,A-—E+,A,E—D+,A-—E+,A,E—D+,A-—E+,A,E—D+,A-—E+,"
                            + "G,G-—D+,G—E+,G,G-—D+,G—E+,G,G-—D+,G—E+,C++,G+,E+,"
                            + "@[instrujoin][C+—C-],G-—G++,C—C++,C-—C++,G-—G++,C—C++,C-—C++,G-—G++,C—C++,C-—C++,G-—G++,C—C++,"
                            + "@[dinstru][D-—B++],G-—G++,B-—D++,D-—B++,G-—G++,B-—D++,D-—B++,G-—G++,B-—D++,D-—B++,G-—G++,B-—D++,"
                            + "@[cinstru][G--—A++],D-—D++,G-—G+,G--—A++,D-—D++,G-—G+,G--—A++,D-—D++,G-—G+,G--—A++,D-—D++,G-—G+,"
                            + "@[ginstru][G-—G++],B-—G+,D—B+,G-—G++,B-—G+,D—B+,G-—G++,B-—G+,D—B+,"
                            + "G,B,D+,C+,D+—G,E+—C,D+—G,E+—C,D+—G,E+—C,C+—G,"
                            + "C+,B,C+,D+,B-—E+,G—D+,B-—E+,G—D+,B-—E+,G—D+,B-—E+,B-—C+,"
                            + "B,A,B,C+,E—D+,A-—E+,E—D+,A-—E+,E—D+,A-—E+,E—D+,A-—G,"
                            + "A,G,A,B,G-—D+,G—E+,G-—D+,G—E+,G-—D+,G—E+,G-—D+,G—E+,"
                            + "G,F,E,G,F--—A+,G-—G+,F--—A+,G-—G+,F--—A+,G-—G+,F--—A+,G-—F+,"
                            + "G+,F+,E+,G+,G--—F+,G+,G--—F+,G+,G--—F+,G+,G--—D+,"
                            + "C++,B+,A+,B+,C++—F--,B+—F-,F--—C++,F-—B+,F--—C++,F-—B+,F--—C++,F-—A+,"
                            + "D++,C++,B+,C++,G--—D++,G-—G+,G--—D++,G-—G+,G--—D++,G-—G+,G--—D++,G-—G+,C++—E++—G++",
                        (236 * 9),
                        1),
                    symphony1Builder.ofScore(
                        Sound.VIOLIN,
                        "C+->@cello1,B+->@cello2,A+->@cello3,G+->@cello4,.->@instrujoin,[G,C+,G,D+,G,F+]*2,.->@dinstru,[G,A,G,C+,G,D]*2,.->@cinstru,A,B,C,.->@ginstru,B,C,D",
                        (12 * 9),
                        0.8f),
                    symphony1Builder.ofScore(
                        Sound.BRASS,
                        ".->@instrujoin,C-->@dinstru,D-->@cinstru,G--->@ginstru",
                        (236 * 9),
                        0.9f)),
                new String[] {"VickyE2"},
                "BLUES",
                0xBB004D)));

    for (var piece : pieces) registry.register(piece);
  }

  private void registerTypeGuiListenerInstances() {
    CancelAllGuiListener cL = new CancelAllGuiListener(this);
    Bukkit.getPluginManager().registerEvents(cL, this);
    DefaultGuiListener dL = new DefaultGuiListener(this);
    Bukkit.getPluginManager().registerEvents(dL, this);
  }

  private void registerCommands() {
    new CommandAPICommand("afk")
        .withAliases("goakf", "ibegone", "cya")
        .executesPlayer(
            executionInfo -> {
              afkManager.handleAfkCommand(executionInfo.sender());
            })
        .register(this);

    new CommandAPICommand("play")
        .withAliases("play_piece")
        .withArguments(
            new StringArgument("music_piece")
                .replaceSuggestions(
                    ArgumentSuggestions.strings(
                        info ->
                            MusicRegistry.getInstance(MusicRegistry.class)
                                .getRegisteredEntities()
                                .stream()
                                .map(MusicPiece::key)
                                .filter(k -> k.startsWith(info.currentArg().toLowerCase()))
                                .toArray(String[]::new))))
        .executesPlayer(
            info -> {
              String musicPiece = info.args().get("music_piece").toString();
              MusicRegistry.getInstance(MusicRegistry.class).playPiece(musicPiece, info.sender());
            })
        .register(this);

    new CommandAPICommand("toggleMusic")
        .executesPlayer(
            info -> {
              MusicRegistry.getInstance(MusicRegistry.class).getPlayer().togglePause(info.sender());
            })
        .register(this);

    new CommandAPICommand("music_piece")
        .withArguments(
            List.of(
                new StringArgument("action")
                    .replaceSuggestions(ArgumentSuggestions.strings("page"))
                    .setOptional(true)))
        .executesPlayer(
            info -> {
              String action =
                  info.args().get("action") != null ? info.args().get("action").toString() : "";
              if (action.equals("page")) {
                info.sender()
                    .sendMessage(
                        MusicRegistry.getInstance(MusicRegistry.class)
                            .renderMusicPage(info.sender(), 0));
              }
            })
        .register(this);

    new CommandAPICommand("mortalise")
        .withPermission(CommandPermission.OP)
        .executesPlayer(
            p -> {
              mortalise(p.sender());
            })
        .register(this);

    new CommandAPICommand("veffect")
        .withAliases("status_effect", "se")
        .withArguments(
            List.of(
                new EntitySelectorArgument.OneEntity("entity"),
                new StringArgument("context_effect")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(
                            info ->
                                EffectRegistry.getInstance(EffectRegistry.class)
                                    .getRegisteredEntities()
                                    .stream()
                                    .map(StatusEffect::getKey)
                                    .filter(k -> k.startsWith(info.currentArg().toLowerCase()))
                                    .toArray(String[]::new))),
                new IntegerArgument("duration_in_seconds").setOptional(true),
                new IntegerArgument("level").setOptional(true)))
        .executes(
            info -> {
              String effectName = (String) info.args().get("context_effect");
              Object optionalDuration = info.args().get("duration_in_seconds");
              Object optionalLevel = info.args().get("level");
              int duration = optionalDuration != null ? (Integer) optionalDuration : 10;
              int level = optionalLevel != null ? (Integer) optionalLevel : 1;
              if (level > 225) {
                info.sender()
                    .sendMessage(BukkitHex.colorize("red[Your level cannot be more than 225...]"));
                return;
              }
              EffectRegistry.getInstance(EffectRegistry.class)
                  .apply(effectName, (LivingEntity) info.args().get("entity"), duration, level);
            })
        .register(this);
  }

  private void getInBuiltEffects() {
    pendingEffects.put(
        new Bleeding(plugin),
        List.of(
            new DeathCause(
                "vicky_utils_bleeding",
                "{player} succumbed to their wounds.",
                false,
                "still bleeding badly",
                1),
            new DeathCause(
                "vicky_utils_bleeding", "{player} bled out.", false, "leaving a trail behind", 1),
            new DeathCause(
                "vicky_utils_bleeding",
                "{player} bled to death while trying to outrun {killer}",
                true,
                "gushing blood as they fled from {killer}",
                1),
            new DeathCause(
                "vicky_utils_bleeding",
                "{player} collapsed in a pool of their own blood.",
                false,
                "weakened by blood loss",
                1)));
  }

  private void prepareGlobals() {
    globalConfigManager = new ConfigManager(this, "global/global_configs.yml");
    globalConfigManager.loadConfigValues();
    config.registerConfigs();
    afkManager = new AFKManager(this);
    placeholderStorer = new PlaceholderStorer();
    stringStorer = new StringStore();
    sqlManager.configureSessionFactory();
    sqlManager.startDatabase();
    databaseManager = new HibernateDatabaseManager();
    bossbarManager.registerListener(this);
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
    CommunicatorServer.getInstance().shutdownAll();
  }

  private void prepareConfiguration() {
    config = new Config(this);
    config.addConfig(
        "defaults.AFKKickMessage",
        new AllowedString(
            "You have been kicked for being AFK too long...This is for your own safety and to"
                + " save server resources :D"));
    config.addConfig(
        "defaults.BTKMessages",
        new AllowedList<>(List.of("You are no longer AFK... now do something", "Welcome back...")));
    config.addConfig(
        "defaults.AFKMessages",
        new AllowedList<>(List.of("Your lazy ahh went to do smfn...", "Annnnnd AFK....")));
    config.addConfig("defaults.NoAFKDamage", new AllowedBoolean(true));
    config.addConfig("defaults.AFKKickThreshold", new AllowedInteger(-1));
    config.addConfig("defaults.AFKThreshold", new AllowedInteger(300000));
    config.addConfig("defaults.BFKInvulnerableTime", new AllowedInteger(10000));
    config.addConfig("defaults.AllowBFKInvulnerable", new AllowedBoolean(true));
    config.addConfig("defaults.MaxBossBarAmount", new AllowedInteger(3));
    config.addConfig("defaults.MaxBossDetectionRange", new AllowedInteger(100));
    config.addConfig("effect_hud.max-popups", new AllowedInteger(20));
    config.addConfig("effect_hud.arrangementType", new AllowedEnum<>(EffectArrangementType.LEFT));
  }

  private void doDefaults() {
    MythicRegistrar.initialize(this);
    new EffectRegistry(MythicRegistrar.getInstance(), this);
    new MusicRegistry();
    customDamageHandler = new CustomDamageHandler(this);
    bossbarManager = new BossbarManager();
    prepareConfiguration();
    sqlManager =
        new SQLManagerBuilder()
            .addMappingClass(DatabasePlayer.class)
            .addMappingClass(Theme.class)
            .addMappingClass(ThemeRegistry.class)
            .addMappingClass(MusicPlaylist.class)
            .addMappingClass(MusicPlayer.class)
            .addMappingClass(org.vicky.utilities.DatabaseManager.templates.MusicPiece.class)
            .addMappingClass(ExtendedPlayerBase.class)
            .setUsername(generator.generate(20, true, true, true, false))
            .setPassword(generator.generatePassword(30))
            .setShowSql(false)
            .setFormatSql(false)
            .setDialect("org.hibernate.community.dialect.SQLiteDialect")
            .setDdlAuto(Hbm2DdlAutoType.UPDATE)
            .build();
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

  public SQLManager getSQLManager() {
    return sqlManager;
  }

  public EntityManager getEntityManager() {
    return HibernateUtil.getEntityManager();
  }

  public HibernateDatabaseManager getDatabaseManager() {
    return databaseManager;
  }

  public ClassLoader getLoader() {
    return loader;
  }

  @NotNull
  public static Listener getGlobalListener() {
    return listener;
  }
}
