/* Licensed under Apache-2.0 2024. */
package org.vicky.effectsSystem;

import io.lumine.mythic.api.MythicPlugin;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.vicky.mythic.BaseMechanic;

/**
 * CustomEffectMechanic is a base class for creating targeted mechanics that apply a custom effect.
 * <p>
 * This class extends {@link BaseMechanic} and accepts a {@link CustomEffect} as a parameter.
 * It reads configuration parameters (such as duration and level) from a {@code MythicLineConfig}
 * instance and then uses the provided {@code CustomEffect} to apply the desired effect to the target.
 * </p>
 *
 * <p>
 * For example, if you have a bleeding effect that implements {@link CustomEffect},
 * you can instantiate a CustomEffectMechanic with that effect. When the mechanic is cast,
 * it will delegate to the custom effect’s {@code applyEffect} method.
 * </p>
 */
public class CustomEffectMechanic implements BaseMechanic {

    /** The duration (in seconds) that the effect should last. */
    private final int duration;

    /** The intensity level of the effect. */
    private final int level;

    /** The custom effect to be applied. */
    private final CustomEffect customEffect;

    /**
     * Constructs a new CustomEffectMechanic.
     *
     * @param config       the {@link MythicLineConfig} containing configuration parameters.
     *                     Expected keys include "duration" (or "d") and "level" (or "l") with default values of 100 and 0, respectively.
     * @param plugin       the {@link Plugin} instance used for logging and effect application.
     * @param customEffect the custom effect to apply; must implement {@link CustomEffect}.
     */
    public CustomEffectMechanic(MythicLineConfig config, Plugin plugin, CustomEffect customEffect) {
        this.duration = config.getInteger(new String[]{"duration", "d"}, 100);
        this.level = config.getInteger(new String[]{"level", "l"}, 0);
        this.customEffect = customEffect;
    }

    /**
     * Applies the custom effect to the target entity.
     * <p>
     * This method converts the provided target to a Bukkit {@link LivingEntity} and delegates the
     * effect application to the custom effect’s {@code applyEffect} method.
     * </p>
     *
     * @param target the Bukkit {@link LivingEntity} that the effect will be applied to.
     * @param data   the {@link SkillMetadata} providing context for the skill cast.
     */
    @Override
    public void applyEffect(LivingEntity target, SkillMetadata data) {
        customEffect.applyEffect(target, duration, level);
    }

    /**
     * Optionally, you can expose getters for duration, level, and the custom effect if needed.
     *
     * @return the configured duration of the effect.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * @return the configured intensity level of the effect.
     */
    public int getLevel() {
        return level;
    }

    /**
     * @return the {@link CustomEffect} instance used by this mechanic.
     */
    public CustomEffect getCustomEffect() {
        return customEffect;
    }
}
