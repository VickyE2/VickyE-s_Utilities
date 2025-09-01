/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import org.bukkit.Bukkit;
import org.vicky.bukkitplatform.useables.BukkitCancellableEvent;
import org.vicky.bukkitplatform.useables.BukkitEvent;
import org.vicky.platform.events.PlatformEvent;
import org.vicky.platform.events.PlatformEventFactory;
import org.vicky.platform.exceptions.UnsupportedEventException;

public class BukkitEventFactory implements PlatformEventFactory {
	@Override
	public <T extends PlatformEvent> T firePlatformEvent(T t) throws UnsupportedEventException {
		if (t instanceof BukkitEvent event) {
			Bukkit.getPluginManager().callEvent(event.event());
			return t;
		} else if (t instanceof BukkitCancellableEvent event) {
			Bukkit.getPluginManager().callEvent(event.event());
			return t;
		}
		throw new IllegalArgumentException("Expected Bukkit(Cancellable)Event got T: " + t.getClass().getName());
	}
}
