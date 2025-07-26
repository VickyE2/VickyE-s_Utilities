/* Licensed under Apache-2.0 2024. */
package org.vicky.effectsSystem;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.mythic.BaseMechanic;

/**
 * StatusEffectMechanic is a base class for creating targeted mechanics that apply a custom effect.
 * <pre>
 * This class extends {@link BaseMechanic} and accepts a {@link StatusEffect} as a parameter.
 * It reads configuration parameters (such as duration and level) from a {@code MythicLineConfig}
 * instance and then uses the provided {@code StatusEffect} to apply the desired effect to the target.
 * </pre>
 *
 * <pre>
 * For example, if you have a bleeding effect that implements {@link StatusEffect},
 * you can instantiate a StatusEffectMechanic with that effect. When the mechanic is cast,
 * it will delegate to the custom effect’s {@code applyEffect} method.
 * </pre>
 */
public class StatusEffectMechanic implements BaseMechanic {

  /**
   * The duration (in seconds) that the effect should last.
   */
  private final int duration;

  /**
   * The intensity level of the effect.
   */
  private final int level;

  /**
   * The custom effect to be applied.
   */
  private final StatusEffect StatusEffect;

  private final JavaPlugin plugin;

  /**
   * Constructs a new StatusEffectMechanic.
   *
   * @param config       the {@link MythicLineConfig} containing configuration parameters.
   *                     Expected keys include "duration" (or "d") and "level" (or "l") with default values of 100 and 0, respectively.
   * @param plugin       the {@link Plugin} instance used for logging and effect application.
   * @param StatusEffect the custom effect to apply; must implement {@link StatusEffect}.
   */
  public StatusEffectMechanic(
      MythicLineConfig config, JavaPlugin plugin, StatusEffect StatusEffect) {
    this.duration = config.getInteger(new String[] {"duration", "d"}, 100);
    this.level = config.getInteger(new String[] {"level", "l"}, 0);
    this.StatusEffect = StatusEffect;
    this.plugin = plugin;
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
    StatusEffect.apply(target, duration, level);
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
   * @return the {@link StatusEffect} instance used by this mechanic.
   */
  public StatusEffect getStatusEffect() {
    return StatusEffect;
  }
}
