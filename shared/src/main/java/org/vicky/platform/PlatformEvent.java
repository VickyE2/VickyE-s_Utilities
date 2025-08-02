package org.vicky.platform;

public interface PlatformEvent {
    /**
     * Returns the name of the event (optional).
     */
    default String getEventName() {
        return this.getClass().getSimpleName();
    }

    /**
     * If the event is cancelable.
     */
    default boolean isCancelable() {
        return false;
    }

    /**
     * If cancelable, is it currently cancelled.
     */
    default boolean isCancelled() {
        return false;
    }

    /**
     * If cancelable, cancel or uncancel.
     */
    default void setCancelled(boolean cancelled) {
        // No-op by default
    }
}