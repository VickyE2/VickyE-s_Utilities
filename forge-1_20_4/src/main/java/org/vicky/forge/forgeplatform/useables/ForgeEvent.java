/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.useables;

import org.vicky.platform.events.PlatformEvent;

import net.minecraftforge.eventbus.api.Event;

public class ForgeEvent implements PlatformEvent {
	private final String name;
	private final Event event;

	public ForgeEvent(Event event) {
		this.name = event.getClass().getSimpleName();
		this.event = event;
	}

	public Event getEvent() {
		return event;
	}

	@Override
	public String getEventName() {
		return name;
	}
}
