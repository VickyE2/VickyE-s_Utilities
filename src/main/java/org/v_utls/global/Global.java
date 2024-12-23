/* Licensed under Apache-2.0 2024. */
package org.v_utls.global;

import org.bukkit.plugin.java.JavaPlugin;
import org.v_utls.handlers.CustomDamageHandler;
import org.v_utls.mythic.MechanicRegistrar;
import org.v_utls.utilities.PlaceholderStorer;
import org.v_utls.utilities.StringStore;

import java.util.List;

public class Global {
  public static PlaceholderStorer placeholderStorer;
  public static StringStore stringStorer;
  public static CustomDamageHandler customDamageHandler;
  public static MechanicRegistrar mechanicRegistrar;

  public static List<JavaPlugin> hookedPlugins;
}
