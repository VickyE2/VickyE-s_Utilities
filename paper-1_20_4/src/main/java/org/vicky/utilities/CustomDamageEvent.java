/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomDamageEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();
	private final Entity entity;
	private final double damage;
	private final String customCause;

	public CustomDamageEvent(Entity entity, double damage, String customCause) {
		this.entity = entity;
		this.damage = damage;
		this.customCause = customCause;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	public Entity getEntity() {
		return entity;
	}

	public double getDamage() {
		return damage;
	}

	public String getCustomCause() {
		return customCause;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}
}
