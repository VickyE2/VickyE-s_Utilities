/* Licensed under Apache-2.0 2024. */
package org.v_utls.effects;

import org.bukkit.entity.LivingEntity;

public interface CustomEffect {
  void applyEffect(LivingEntity entity, int durationInSeconds, int level);

  void stopEffect(LivingEntity entity);

  boolean isEntityAffected(LivingEntity entity);

  EffectType getEffectType();
}
