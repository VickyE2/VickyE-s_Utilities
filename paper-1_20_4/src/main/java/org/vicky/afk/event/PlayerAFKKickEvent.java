/* Licensed under Apache-2.0 2024. */
package org.vicky.afk.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player is detected as AFK.
 */
public class PlayerAFKKickEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private final Player player;
  private boolean isCancelled = false;

  public PlayerAFKKickEvent(Player player) {
    this.player = player;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public boolean isCancelled() {
    return isCancelled;
  }

  public void setCancelled(boolean cancelled) {
    isCancelled = cancelled;
  }

  public Player getPlayer() {
    return player;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }
}
