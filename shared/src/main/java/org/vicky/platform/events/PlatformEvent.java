package org.vicky.platform.events;

public interface PlatformEvent {
    /**
     * Returns the name of the event (optional).
     */
    default String getEventName() {
        return this.getClass().getSimpleName();
    }
}