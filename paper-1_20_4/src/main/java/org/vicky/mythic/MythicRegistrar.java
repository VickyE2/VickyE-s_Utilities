/* Licensed under Apache-2.0 2024. */
package org.vicky.mythic;

import static org.vicky.global.Global.stringStorer;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiFunction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.vicky.mythic.conditions.ExperiencingStatusEffect;
import org.vicky.mythic.exceptions.InvalidMechanicConstructorException;
import org.vicky.mythic.mechanics.BossBarPhaseChangingMechanic;
import org.vicky.mythic.mechanics.BossBarRegistrationMechanic;
import org.vicky.utilities.ContextLogger.ContextLogger;

/**
 * MythicRegistrar is responsible for registering custom mechanics with MythicMobs.
 * <pre>
 * It maintains a mapping from mechanic names (in uppercase) to factory functions that generate
 * instances of BaseMechanic based on a given MythicLineConfig and or with a Plugin instance.
 * When a MythicMechanicLoadEvent is fired, the registrar looks up the mechanic name in its map,
 * creates a new instance, and registers it.
 * </pre>
 */
public class MythicRegistrar {
  private static MythicRegistrar instance;
  private final Plugin plugin;
  private final ContextLogger logger =
      new ContextLogger(ContextLogger.ContextType.SUB_SYSTEM, "MYTHIC-REGISTRAR");

  /**
   * Maps a mechanic name to a factory function that creates a new BaseMechanic.
   */
  private final Map<String, BiFunction<MythicLineConfig, Plugin, BaseMechanic>> mechanicMap;

  private final Map<String, BiFunction<MythicLineConfig, Plugin, BaseCondition>> conditionMap;

  /**
   * Constructs a new MythicRegistrar for the given plugin and pre-populates the mechanic map.
   *
   * @param plugin the Plugin instance used for registering mechanics and logging
   */
  private MythicRegistrar(Plugin plugin) {
    this.plugin = plugin;
    this.mechanicMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    this.conditionMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    addMechanic("BOSSBAR", BossBarRegistrationMechanic::new);
    addMechanic("CHANGEPHASE", BossBarPhaseChangingMechanic::new);
    addCondition("EXPERIENCINGSTATUSEFFECT", (config, p) -> new ExperiencingStatusEffect(config));
  }

  /**
   * Returns the singleton instance. Must be initialized first with `initialize(plugin)`.
   *
   * @return MythicRegistrar singleton
   * @throws IllegalStateException if used before initialization
   */
  public static MythicRegistrar getInstance() {
    if (instance == null) {
      throw new IllegalStateException("MythicRegistrar has not been initialized yet!");
    }
    return instance;
  }

  /**
   * Initializes the singleton instance.
   * This should be called only once during plugin enable phase.
   *
   * @param plugin Plugin instance
   * @throws IllegalStateException if already initialized
   */
  public static void initialize(Plugin plugin) {
    if (instance != null) {
      throw new IllegalStateException("MythicRegistrar is already initialized!");
    }
    instance = new MythicRegistrar(plugin);
  }

  /**
   * Adds a new mechanic to the registrar,
   * attaching the <em>VUTL_</em> identifier to the front of it for internal purposes.
   *
   * @param mechanicName the key/name for the mechanic.
   * @param mechanic     the Class object for the mechanic implementing BaseMechanic.
   *                     This mechanic must have a constructor like this having higher priority:
   *                     <pre> {@code
   *                                                             public SomeRandomMechanic(MythicLineConfig config, Plugin plugin) {
   *                                                                ...
   *                                                             }
   *                                                             }
   *                                                         </pre>
   *                     or this with lower priority:
   *                     <pre> {@code
   *                                                             public SomeRandomMechanic(MythicLineConfig config) {
   *                                                                ...
   *                                                             }
   *                                                             }
   *                                                         </pre>
   */
  public void addMechanic(String mechanicName, Class<? extends BaseMechanic> mechanic) {
    BiFunction<MythicLineConfig, Plugin, BaseMechanic> factory =
        (config, plugin) -> {
          try {
            return mechanic
                .getConstructor(MythicLineConfig.class, Plugin.class)
                .newInstance(config, plugin);
          } catch (Exception e) {
            try {
              return mechanic.getConstructor(MythicLineConfig.class).newInstance(config);
            } catch (Exception ex) {
              throw new InvalidMechanicConstructorException(
                  "While adding mechanic", mechanicName, ex);
            }
          }
        };
    registerMechanicNames(mechanicName, factory);
  }

  /**
   * Adds a new mechanic via a factory function (lambda), no reflection required.
   *
   * @param mechanicName the key/name for the mechanic (without VUTL_ prefix)
   * @param factory      the lambda that creates a BaseMechanic
   */
  public void addMechanic(
      String mechanicName, BiFunction<MythicLineConfig, Plugin, BaseMechanic> factory) {
    registerMechanicNames(mechanicName, factory);
  }

  /**
   * Adds a new condition via a factory function (lambda), no reflection required.
   *
   * @param conditionName the key/name for the condition (without VUTL_ prefix)
   * @param factory      the lambda that creates a BaseMechanic
   */
  public void addCondition(
      String conditionName, BiFunction<MythicLineConfig, Plugin, BaseCondition> factory) {
    String base = conditionName.toUpperCase();
    conditionMap.put(base, factory);
  }

  private void registerMechanicNames(
      String name, BiFunction<MythicLineConfig, Plugin, BaseMechanic> factory) {
    String base = name.toUpperCase();
    mechanicMap.put(base, factory); // user-friendly (FOOBAR)
  }

  /**
   * Registers all necessary event listeners for mechanic loading.
   */
  public void registerAll() {
    plugin.getServer().getPluginManager().registerEvents(new RegistrarListener(), plugin);
  }

  /**
   * Inner class that listens for MythicMechanicLoadEvent events and registers the appropriate mechanic.
   */
  private class RegistrarListener implements Listener {

    /**
     * Called when a MythicMechanicLoadEvent is fired.
     * <p></p>
     * <p><em>
     * Looks up the mechanic name (converted to uppercase) in the mechanicMap. If a corresponding
     * factory is found, it creates a new instance of the mechanic using the event’s configuration
     * and registers it. Otherwise, logs a warning.
     * </em></p>
     *
     * @param event the MythicMechanicLoadEvent containing the mechanic name and configuration
     */
    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
      String name = event.getMechanicName();
      String upper = name.toUpperCase();
      if (upper.startsWith("VUTLS:")) {
        BiFunction<MythicLineConfig, Plugin, BaseMechanic> factory =
            mechanicMap.getOrDefault(upper, mechanicMap.get(upper.replace("VUTLS:", "")));
        if (factory != null) {
          BaseMechanic mechanic = factory.apply(event.getConfig(), plugin);
          event.register(mechanic);
          stringStorer.storeString(
              plugin.getName(), "mm_mechanics", "-- Registered " + upper + " mechanic!");
        } else {
          logger.printBukkit("-- Unrecognized mechanic: " + name, ContextLogger.LogType.WARNING);
        }
      }
    }

    @EventHandler
    public void onMythicConditionLoad(MythicConditionLoadEvent event) {
      String name = event.getConditionName();
      String upper = name.toUpperCase();
      String prefixed = "VUTL_" + upper;

      BiFunction<MythicLineConfig, Plugin, BaseCondition> factory =
          conditionMap.getOrDefault(upper, conditionMap.get(prefixed));

      if (factory != null) {
        BaseCondition condition = factory.apply(event.getConfig(), plugin);
        event.register(condition);
        stringStorer.storeString(
            plugin.getName(), "mm_conditions", "-- Registered " + upper + " condition!");
      } else if (upper.startsWith("VUTL_")) {
        logger.printBukkit("-- Unrecognized condition: " + name, ContextLogger.LogType.WARNING);
      }
    }
  }
}
