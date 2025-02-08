/* Licensed under Apache-2.0 2024. */
package org.vicky.effects;

import org.bukkit.entity.LivingEntity;

/**
 * CustomEffect defines a contract for applying and managing custom effects on living entities.
 * <p>
 * Implementations of this interface should provide logic for applying an effect to a {@link LivingEntity}
 * for a specified duration and level, stopping the effect, and querying whether an entity is currently affected.
 * Additionally, an implementation must provide its corresponding {@link EffectType}.
 * </p>
 */
public interface CustomEffect {

  /**
   * Applies the effect to the given living entity.
   *
   * @param entity            the {@link LivingEntity} to which the effect is applied
   * @param durationInSeconds the duration of the effect in seconds
   * @param level             the level (or intensity) of the effect
   */
  void applyEffect(LivingEntity entity, int durationInSeconds, int level);

  /**
   * Stops the effect on the given living entity.
   *
   * @param entity the {@link LivingEntity} on which the effect should be stopped
   */
  void stopEffect(LivingEntity entity);

  /**
   * Determines whether the given living entity is currently affected by this effect.
   *
   * @param entity the {@link LivingEntity} to check
   * @return true if the entity is affected by this effect; false otherwise
   */
  boolean isEntityAffected(LivingEntity entity);

  /**
   * Retrieves the type of this effect.
   *
   * @return an {@link EffectType} representing the effect type
   */
  EffectType getEffectType();
}
