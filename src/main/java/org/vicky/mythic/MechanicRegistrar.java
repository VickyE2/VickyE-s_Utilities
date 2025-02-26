/* Licensed under Apache-2.0 2024. */
package org.vicky.mythic;

import static org.vicky.global.Global.configManager;
import static org.vicky.global.Global.stringStorer;

import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.api.config.MythicLineConfig;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.vicky.betterHUD.BossbarMechanic;
import org.vicky.mythic.mechanics.BleedingMechanic;

/**
 * MechanicRegistrar is responsible for registering custom mechanics with MythicMobs.
 * <p>
 * It maintains a mapping from mechanic names (in uppercase) to factory functions that generate
 * instances of BaseMechanic based on a given MythicLineConfig and or with a Plugin instance.
 * When a MythicMechanicLoadEvent is fired, the registrar looks up the mechanic name in its map,
 * creates a new instance, and registers it.
 * </p>
 */
public class MechanicRegistrar {

  private final Plugin plugin;
  /**
   * Maps a mechanic name to a factory function that creates a new BaseMechanic.
   */
  private final Map<String, BiFunction<MythicLineConfig, Plugin, BaseMechanic>> mechanicMap;

  /**
   * Constructs a new MechanicRegistrar for the given plugin and pre-populates the mechanic map.
   *
   * @param plugin the Plugin instance used for registering mechanics and logging
   */
  public MechanicRegistrar(Plugin plugin) {
    this.plugin = plugin;
    this.mechanicMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    mechanicMap.put("BLEEDING", BleedingMechanic::new);
    mechanicMap.put("BOSSBAR", BossbarMechanic::new);
  }

  /**
   * Adds a new mechanic to the registrar.
   *
   * @param mechanicName the key/name for the mechanic.
   * @param mechanic the Class object for the mechanic implementing BaseMechanic.
   *                 This mechanic must have a constructor like this having higher priority:
   *                 <pre> {@code
   *                     public SomeRandomMechanic(MythicLineConfig config, Plugin plugin) {
   *                        ...
   *                     }
   *                     }
   *                 </pre>
   *                 or this with lower priority:
   *                 <pre> {@code
   *                     public SomeRandomMechanic(MythicLineConfig config) {
   *                        ...
   *                     }
   *                     }
   *                 </pre>
   */
  public void addMechanic(String mechanicName, Class<? extends BaseMechanic> mechanic) {
    mechanicMap.put(mechanicName, (config, plugin) -> {
      try {
        return mechanic.getConstructor(MythicLineConfig.class, Plugin.class)
                .newInstance(config, plugin);
      } catch (Exception e) {
        try {
          return mechanic.getConstructor(MythicLineConfig.class)
                  .newInstance(config);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
          throw new RuntimeException(ex);
        }
      }
    });
  }

  /**
   * Registers all necessary event listeners for mechanic loading.
   */
  public void registerAll() {
    plugin.getServer().getPluginManager().registerEvents(new MechanicsListener(), plugin);
  }

  /**
   * Inner class that listens for MythicMechanicLoadEvent events and registers the appropriate mechanic.
   */
  private class MechanicsListener implements Listener {

    /**
     * Called when a MythicMechanicLoadEvent is fired.
     * <p>
     * Looks up the mechanic name (converted to uppercase) in the mechanicMap. If a corresponding
     * factory is found, it creates a new instance of the mechanic using the event’s configuration
     * and registers it. Otherwise, logs a warning.
     * </p>
     *
     * @param event the MythicMechanicLoadEvent containing the mechanic name and configuration
     */
    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
      String mechanicKey = event.getMechanicName().toUpperCase();
      if (mechanicMap.containsKey(mechanicKey)) {
        BaseMechanic mechanic = mechanicMap.get(mechanicKey).apply(event.getConfig(), plugin);
        event.register(mechanic);
        stringStorer.storeString(plugin.getName(), "mm_mechanics", "-- Registered " + mechanicKey + " mechanic!");
      } else if (configManager.getBooleanValue("Debug")) {
        plugin.getLogger().warning("-- Unrecognized mechanic: " + event.getMechanicName());
      }
    }
  }
}
