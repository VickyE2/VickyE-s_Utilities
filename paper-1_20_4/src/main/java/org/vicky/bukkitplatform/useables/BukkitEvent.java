/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.bukkit.event.Event;
import org.vicky.platform.events.PlatformEvent;

public record BukkitEvent(Event event) implements PlatformEvent {
    @Override
    public String getEventName() {
        return event.getEventName();
    }
}
