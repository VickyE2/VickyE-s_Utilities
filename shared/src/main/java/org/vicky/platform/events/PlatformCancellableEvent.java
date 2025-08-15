package org.vicky.platform.events;

public interface PlatformCancellableEvent extends PlatformEvent {
    /**
     * If the event is cancelable.
     */
    default boolean isCancelable() {
        return true;
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
