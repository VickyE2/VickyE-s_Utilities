/* Licensed under Apache-2.0 2024. */
package org.vicky.mythic.mechanics;

import static org.vicky.global.Global.bossbarManager;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.vicky.betterHUD.bossbar.BossBarMechanic;

public class BossBarRegistrationMechanic extends BossBarMechanic {
  private final int range;

  public BossBarRegistrationMechanic(MythicLineConfig config, Plugin plugin) {
    super(config, plugin);
    this.range = config.getInteger(new String[] {"range", "r", "detection_range", "dr"}, 50);
  }

  @Override
  public void applyEffect(LivingEntity target, SkillMetadata data) {
    if (bossbarManager.registerBoss(bossName, bossType, range, popupName, target)) {
      target.setMetadata("bossBarAttainableName", new FixedMetadataValue(plugin, bossName));
      target.setMetadata("bossBarAttainableType", new FixedMetadataValue(plugin, bossType));
      target.setMetadata("bossBarAttainablePhase", new FixedMetadataValue(plugin, 1));
    }
  }
}
