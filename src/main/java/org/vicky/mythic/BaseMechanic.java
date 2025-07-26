/* Licensed under Apache-2.0 2024. */
package org.vicky.mythic;

import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.entity.LivingEntity;

/**
 * BaseMechanic interface provides a foundation for creating mechanics that target a single entity.
 * <pre>
 * Implementing classes must define the {@link #applyEffect(LivingEntity, SkillMetadata)} method to
 * specify their effect logic. This interface provides a default implementation of
 * {@link #castAtEntity(SkillMetadata, AbstractEntity)} that handles the conversion from a MythicMobs
 * AbstractEntity to a Bukkit {@link LivingEntity} and then calls {@link #applyEffect(LivingEntity, SkillMetadata)}.
 * </pre>
 */
public interface BaseMechanic extends ITargetedEntitySkill {

  /**
   * Applies the specific effect to the target entity.
   * <p>
   * Implementing classes must define what happens when the mechanic is cast.
   * </p>
   *
   * @param target the Bukkit LivingEntity that the effect is applied to
   * @param data   the SkillMetadata providing context for the skill cast
   */
  void applyEffect(LivingEntity target, SkillMetadata data);

  /**
   * Default implementation of castAtEntity that converts the provided AbstractEntity to a Bukkit LivingEntity
   * and delegates to {@link #applyEffect(LivingEntity, SkillMetadata)}.
   *
   * @param data   the SkillMetadata containing context for the skill cast
   * @param target the AbstractEntity representing the target of the mechanic
   * @return {@link SkillResult#SUCCESS} if the effect is applied successfully
   */
  @Override
  default SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
    LivingEntity bukkitTarget = (LivingEntity) BukkitAdapter.adapt(target);
    applyEffect(bukkitTarget, data);
    return SkillResult.SUCCESS;
  }
}
