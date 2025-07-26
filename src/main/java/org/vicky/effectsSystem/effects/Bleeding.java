/* Licensed under Apache-2.0 2024. */
package org.vicky.effectsSystem.effects;

import static org.vicky.global.Global.customDamageHandler;

import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.vicky.effectsSystem.StatusEffect;
import org.vicky.effectsSystem.enums.EffectType;

public class Bleeding extends StatusEffect {
  public Bleeding(Plugin plugin) {
    super(plugin, plugin.getResource("icons/bleeding.png"), 20);
  }

  @Override
  public String getKey() {
    return "vicky_utils_bleeding";
  }

  @Override
  public EffectType getEffectType() {
    return EffectType.HARMFUL;
  }

  /**
   * This is what should happen per tick of the event
   *
   * @param entity           The entity in context to apply the effect to
   * @param remainingSeconds The duration left in case of time-based effects
   * @param level            The strength of the effect being applied
   */
  @Override
  protected void onTick(LivingEntity entity, double remainingSeconds, int level) {
    entity.addPotionEffect(
        new PotionEffect(PotionEffectType.WEAKNESS, 20, level, true, false, false));
    applyCustomBleedingDamage(entity, level);
  }

  @Override
  public void stopEffect(LivingEntity entity) {}

  private void applyCustomBleedingDamage(LivingEntity entity, int level) {
    double damage = calculateBleedingDamage(level);
    customDamageHandler.applyCustomDamage(entity, damage, getKey());
  }

  private double calculateBleedingDamage(int level) {
    return Math.exp(0.5 * (level * Math.cos(1.4) + level * Math.sin(59)));
  }
}
