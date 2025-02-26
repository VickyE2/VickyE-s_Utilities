/* Licensed under Apache-2.0 2024. */
package org.vicky.mythic.mechanics;

import io.lumine.mythic.api.config.MythicLineConfig;
import org.bukkit.plugin.Plugin;
import org.vicky.effectsSystem.effects.Bleeding;
import org.vicky.effectsSystem.CustomEffectMechanic;

public class BleedingMechanic extends CustomEffectMechanic {
  public BleedingMechanic(MythicLineConfig config, Plugin plugin) {
    super(config, plugin, new Bleeding(plugin));
  }
}
