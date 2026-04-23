/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform;

import org.vicky.forge.forgeplatform.useables.ForgeEvent;
import org.vicky.platform.events.*;
import org.vicky.platform.exceptions.UnsupportedEventException;

import net.minecraftforge.common.MinecraftForge;

import java.util.HashMap;
import java.util.Map;

import static org.vicky.forge.forgeplatform.useables.ForgeHacks.fromVicky;

public class ForgeEventFactory implements PlatformEventDispatcher, PlatformEventRegistry {
	public static final ForgeEventFactory INSTANCE = new ForgeEventFactory();

	private ForgeEventFactory() {
	}

	private final Map<Class<? extends PlatformEvent>, EventDescriptor> descriptors = new HashMap<>();

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PlatformEvent> T firePlatformEvent(T event) throws UnsupportedEventException {
		if (event instanceof ForgeEvent forgeEvent) {
			return (T) MinecraftForge.EVENT_BUS.fire(forgeEvent.getEvent());
		} else {
			throw new IllegalArgumentException(
					"Expected event of type `ForgeEvent` got " + event.getClass().getSimpleName());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends PlatformEvent> void registerListener(
			Class<T> type,
			EventPriority priority,
			EventListener.EventExecutor<T> executor
	) {

		EventDescriptor descriptor = descriptors.get(type);
		if (descriptor == null) {
			throw new IllegalStateException("Event not registered: " + type.getSimpleName());
		}

		Class<? extends net.minecraftforge.eventbus.api.Event> forgeEventClass =
				ForgeEvent.getForgeEventClass(type);

		final net.minecraftforge.eventbus.api.EventPriority priorityF = fromVicky(priority);
		MinecraftForge.EVENT_BUS.addListener(
				priorityF,
				false,
				forgeEventClass,
				forgeEvent -> {
					if (!forgeEventClass.isInstance(forgeEvent)) return;

					ForgeEvent platformEvent = ForgeEvent.wrap(forgeEvent);

					executor.execute((T) platformEvent);

					// Sync cancellation
					if (descriptor.cancellable() && forgeEvent instanceof net.minecraftforge.eventbus.api.Cancelable) {
						if (platformEvent.isCancelled()) {
							platformEvent.setCancelled(true);
						}
					}
				}
		);
	}

	@Override
	public void registerEvent(EventDescriptor event) throws UnsupportedEventException {
		descriptors.put(event.eventType(), event);
	}

	@Override
	public EventDescriptor getDescriptor(Class<? extends PlatformEvent> type) {
		return descriptors.get(type);
	}
}
