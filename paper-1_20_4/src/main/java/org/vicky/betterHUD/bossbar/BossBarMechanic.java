/* Licensed under Apache-2.0 2024. */
package org.vicky.betterHUD.bossbar;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.vicky.mythic.BaseMechanic;

public abstract class BossBarMechanic implements BaseMechanic {
  protected final Plugin plugin;
  protected final String bossType;
  protected final String bossName;
  protected final String popupName;

  public BossBarMechanic(MythicLineConfig config, Plugin plugin) {
    this.bossType = config.getString(new String[] {"type", "t"}, "Unknown");
    this.bossName = config.getString(new String[] {"name", "n"}, "Unknown");
    this.popupName =
        config.getString(new String[] {"popup", "p", "popup_name", "pn"}, "default_bossbar");
    this.plugin = plugin;
  }

  public abstract void applyEffect(LivingEntity target, SkillMetadata data);
}
