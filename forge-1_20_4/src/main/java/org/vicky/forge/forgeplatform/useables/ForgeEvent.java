/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.useables;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import org.vicky.forge.forgeplatform.ForgeEventFactory;
import org.vicky.forge.weather.ForgeWeatherChangeEvent;
import org.vicky.platform.events.PlatformCancellableEvent;
import org.vicky.platform.events.PlatformEvent;

import net.minecraftforge.eventbus.api.Event;
import org.vicky.platform.events.PlatformEvents;

import java.util.Map;

public class ForgeEvent implements PlatformCancellableEvent {
	private final String name;
	private final Event event;

	private static final Map<Class<?>, Class<? extends Event>> EVENT_MAP = Map.of(
			PlatformEvents.PlayerTickEvent.class, TickEvent.PlayerTickEvent.class,
			PlatformEvents.PlayerEvent.class, PlayerEvent.class,
			PlatformEvents.LivingEntityEvent.class, LivingEvent.class,
			PlatformEvents.LivingEntityTickEvent.class, LivingEvent.LivingTickEvent.class,
			PlatformEvents.WorldEvent.class, LevelEvent.class,
			PlatformEvents.WeatherChangeEvent.class, ForgeWeatherChangeEvent.class
	);

	public ForgeEvent(Event event) {
		this.name = event.getClass().getSimpleName();
		this.event = event;
	}

	public static Class<? extends net.minecraftforge.eventbus.api.Event> getForgeEventClass(Class<?> platformType) {
		Class<? extends net.minecraftforge.eventbus.api.Event> result =
				EVENT_MAP.get(platformType);

		if (result == null) {
			throw new UnsupportedOperationException("Unsupported event: " + platformType.getName());
		}

		return result;
	}

	public static <T extends PlatformEvent> T wrap(net.minecraftforge.eventbus.api.Event event) {
		return (T) new ForgeEvent(event);
	}

	public Event getEvent() {
		return event;
	}

	@Override
	public String getEventName() {
		return name;
	}

	@Override
	public boolean isCancelable() {
		return event.isCancelable();
	}

	@Override
	public boolean isCancelled() {
		return event.isCanceled();
	}

	@Override
	public void setCancelled(boolean cancelled) {
		event.setCanceled(cancelled);
	}
}
