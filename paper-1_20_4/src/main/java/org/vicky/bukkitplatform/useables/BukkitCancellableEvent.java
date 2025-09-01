/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.vicky.platform.events.PlatformCancellableEvent;

public record BukkitCancellableEvent(Event event) implements PlatformCancellableEvent {
    @Override
    public String getEventName() {
        return event.getEventName();
    }

    @Override
    public boolean isCancelable() {
        return event instanceof Cancellable;
    }

    @Override
    public boolean isCancelled() {
        if (event instanceof Cancellable) {
            return ((Cancellable) event).isCancelled();
        }
        return false;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        if (event instanceof Cancellable) {
            ((Cancellable) event).setCancelled(cancelled);
        }
    }
}
