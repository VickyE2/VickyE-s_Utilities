/* Licensed under Apache-2.0 2024. */
package org.vicky.betterHUD;

import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.bukkit.events.MythicMobDeathEvent;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.vicky.mythic.BaseMechanic;
import org.vicky.utilities.ContextLogger.ContextLogger;

public class BossbarMechanic implements BaseMechanic, Listener {
  private LivingEntity mob;
  private final Plugin plugin;
  private UUID Id;
  public String name;
  public final int thresholdDistance;
  public final ContextLogger logger =
      new ContextLogger(ContextLogger.ContextType.FEATURE, "BOSSBARS");

  /**
   * Constructs a BaseBossbar with the given parameters.
   *
   * @param config The passed Mythic config by the user
   */
  public BossbarMechanic(MythicLineConfig config, Plugin plugin) {
    thresholdDistance = config.getInteger(new String[] {"threshold_distance", "td"}, 100);
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    this.plugin = plugin;
  }

  public LivingEntity getMob() {
    return mob;
  }

  public UUID getId() {
    return mob.getUniqueId();
  }

  /**
   * Applies the specific effect to the target entity.
   * <p>
   * Implementing classes must define what happens when the mechanic is cast.
   * </p>
   *
   * @param target the Bukkit LivingEntity that the effect is applied to
   * @param data   the SkillMetadata providing context for the skill cast
   */
  @Override
  public void applyEffect(LivingEntity target, SkillMetadata data) {
    attachToMob(target);
  }

  /**
   * Attaches this bossbar mechanic to a mob.
   *
   * @param mob The mob (e.g., a MythicMob) to map the bossbar to.
   */
  public void attachToMob(LivingEntity mob) {
    this.mob = mob;
    this.Id = mob.getUniqueId();
    this.name = mob.name().toString();
    logger.printBukkit(
        "Attaching Bossbar to entity: "
            + name
            + " Id: "
            + Id
            + " mob: "
            + mob.getType().toString().toLowerCase(),
        ContextLogger.LogType.PENDING);
    BossbarManager.registerBossbar(this.Id, this);
  }

  /**
   * Updates the bossbar based on the mob's current health.
   * This method would be called periodically (for example, in a scheduled task).
   */
  public void updateForMob() {
    if (mob == null || !mob.isValid()) {
      BossbarManager.unregisterBossbar(this.Id);
    }
  }

  @EventHandler
  public void onEntityKilledEvent(MythicMobDeathEvent e) {
    UUID primate = e.getMob().getEntity().getBukkitEntity().getUniqueId();
    if (this.mob == null) {
      return;
    }
    if (primate != this.Id) return;

    HandlerList.unregisterAll(this);
    BossbarManager.unregisterBossbar(this.Id);
  }
}
