/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.mythic.mechanics;

import static org.vicky.global.Global.bossbarManager;

import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.vicky.betterHUD.bossbar.BossBarMechanic;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;

public class BossBarPhaseChangingMechanic extends BossBarMechanic {
	protected final int bossPhase;

	public BossBarPhaseChangingMechanic(MythicLineConfig config, Plugin plugin) {
		super(config, plugin);
		this.bossPhase = config.getInteger(new String[]{"phase", "p"}, 1);
	}

	public void applyEffect(LivingEntity target, SkillMetadata data) {
		if (bossbarManager.isBossRegistered(bossName)) {
			target.setMetadata("bossBarAttainablePhase", new FixedMetadataValue(plugin, bossPhase));
		} else {
			bossbarManager.logger.print("The boss " + bossName + " isn't registered and try changing a bossbar phase",
					true);
		}
	}
}
