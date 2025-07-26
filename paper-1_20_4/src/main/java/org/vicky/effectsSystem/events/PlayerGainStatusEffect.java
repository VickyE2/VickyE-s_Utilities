/* Licensed under Apache-2.0 2024. */
package org.vicky.effectsSystem.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.vicky.effectsSystem.StatusEffect;

public class PlayerGainStatusEffect extends PlayerEvent implements Cancellable {
  private static final HandlerList handlers = new HandlerList();
  private final StatusEffect effect;
  private final double initialDuration;
  private boolean isCancelled = false;

  public PlayerGainStatusEffect(Player player, StatusEffect statusEffect, double initialDuration) {
    super(player);
    this.effect = statusEffect;
    this.initialDuration = initialDuration;
  }

  public StatusEffect getEffect() {
    return effect;
  }

  public double getInitialDuration() {
    return initialDuration;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return handlers;
  }

  /**
   * Gets the cancellation state of this event. A cancelled event will not
   * be executed in the server, but will still pass to other plugins
   *
   * @return true if this event is cancelled
   */
  @Override
  public boolean isCancelled() {
    return isCancelled;
  }

  /**
   * Sets the cancellation state of this event. A cancelled event will not
   * be executed in the server, but will still pass to other plugins.
   *
   * @param cancel true if you wish to cancel this event
   */
  @Override
  public void setCancelled(boolean cancel) {
    isCancelled = cancel;
  }
}
