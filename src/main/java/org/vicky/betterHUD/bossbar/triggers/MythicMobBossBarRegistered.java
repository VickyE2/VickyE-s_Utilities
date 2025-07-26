/* Licensed under Apache-2.0 2024. */
package org.vicky.betterHUD.bossbar.triggers;

import java.util.UUID;
import kr.toxicity.hud.api.bukkit.event.BetterHudEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.vicky.utilities.RandomStringGenerator;
import org.vicky.utilities.UUIDGenerator;

public class MythicMobBossBarRegistered extends Event implements BetterHudEvent {
  private static final HandlerList handlers = BetterHudEvent.HANDLER_LIST;
  private final String name;
  private final String type;
  private final int phase;
  private final LivingEntity mob;
  private final UUID SpeciallyPreparedUniqueId;

  public MythicMobBossBarRegistered(LivingEntity mob, String name, String type, int phase) {
    this.name = name;
    this.type = type;
    this.phase = phase;
    this.mob = mob;
    this.SpeciallyPreparedUniqueId =
        UUIDGenerator.generateUUIDFromString(
            name
                + "_"
                + type
                + "_"
                + RandomStringGenerator.getInstance().generate(5, true, true, false, false));
  }

  public static @NotNull HandlerList getHandlerList() {
    return handlers;
  }

  public LivingEntity getMob() {
    return mob;
  }

  public String getName() {
    return name;
  }

  public int getPhase() {
    return phase;
  }

  @Override
  public @NotNull String getEventName() {
    return "MythicMobBossBarRegisteredEvent";
  }

  public String getType() {
    return type;
  }

  public UUID getSpeciallyPreparedUniqueId() {
    return SpeciallyPreparedUniqueId;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }
}
