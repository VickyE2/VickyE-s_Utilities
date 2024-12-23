/* Licensed under Apache-2.0 2024. */
package org.v_utls;

import static org.v_utls.global.Global.*;

import java.io.File;
import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.v_utls.effects.Bleeding;
import org.v_utls.effects.CustomEffect;
import org.v_utls.expansions.PlaceholderExpansions;
import org.v_utls.global.Global;
import org.v_utls.handlers.CustomDamageHandler;
import org.v_utls.listeners.DeathListener;
import org.v_utls.listeners.SpawnListener;
import org.v_utls.mythic.MechanicRegistrar;
import org.v_utls.utilities.*;

public final class vicky_utils extends JavaPlugin {

  public static vicky_utils plugin;

  boolean exceptionOccurred = false;

  public static vicky_utils getPlugin() {
    return plugin;
  }

  @Override
  public void onEnable() {

    // Plugin startup logic
    if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
        && Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {

      Global.placeholderStorer = new PlaceholderStorer();
      Global.stringStorer = new StringStore();
      Global.customDamageHandler = new CustomDamageHandler(this);
      Global.mechanicRegistrar = new MechanicRegistrar(this);

      MainLogger logger1 = new MainLogger(this);

      getLogger().info("Rendering Vicky's Utilities Accessible");

      if (Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
        FileManager fileManager = new FileManager(this);
        getLogger().info("ItemsAdder is present. Extracting Default Files");
        List<String> files = List.of("contents/vicky_utls/");
        fileManager.extractDefaultIAAssets(files);
      } else {
        getLogger().warning("ItemsAdder isn't present. Defaulting to basic settings");
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
  }

  public CustomDamageHandler getCustomDamageHandler() {
    return customDamageHandler;
  }


  public static void hookDependantPlugin(@NotNull JavaPlugin plugin){
    getPlugin().getLogger().info(ANSIColor.colorize("New plugin hooked successfully: green[" + plugin.getName() + "]" ));
    hookedPlugins.add(plugin);
  }
  public static void unhookDependantPlugin(@NotNull JavaPlugin plugin){
    boolean removed = hookedPlugins.remove(plugin);
    if (removed) {
      getPlugin().getLogger().info(ANSIColor.colorize("Plugin purple[" + plugin.getName() + "] has been unhooked" ));
    }else {
      getPlugin().getLogger().warning(ANSIColor.colorize("Plugin bold[" + plugin.getName() + "] wasn't found among hooked plugins. Please contact the plugin developers if this isn't a development environment."));
    }
  }
  public static String getHookedDependantPlugins(){
    StringBuilder plugins = new StringBuilder();
    plugins.append(ANSIColor.colorize("cyan[Hooked Plugins: ] \n"));
    for (JavaPlugin plugin : hookedPlugins) {
      plugins.append(ANSIColor.colorize("   - purple[" + plugin.getName() + "]"));
    }
    return  plugins.toString();
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
}
