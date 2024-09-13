package org.v_utls.mythic;

import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.plugin.Plugin;
import org.v_utls.effects.Bleeding; // Import your Bleeding class here

public class BleedingMechanic implements ITargetedEntitySkill {

    private final int duration;
    private final int level;
    private final Plugin plugin;

    public BleedingMechanic(MythicLineConfig config, Plugin plugin) {
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.level = config.getInteger(new String[]{"level", "l"}, 0);
        this.plugin = plugin; // Store the plugin instance
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        LivingEntity bukkitTarget = (LivingEntity) BukkitAdapter.adapt(target);

        // Create an instance of your Bleeding class with the plugin instance
        Bleeding bleedingEffect = new Bleeding(plugin);
        bleedingEffect.applyEffect(bukkitTarget, duration, level);

        return SkillResult.SUCCESS;
    }
}

