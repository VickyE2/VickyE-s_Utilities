/* Licensed under Apache-2.0 2024. */
package org.vicky.afk.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when a player is detected as AFK.
 */
public class PlayerAFKEvent extends Event {
  private static final HandlerList handlers = new HandlerList();
  private final Player player;

  public PlayerAFKEvent(Player player) {
    this.player = player;
  }

  public Player getPlayer() {
    return player;
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }
}
