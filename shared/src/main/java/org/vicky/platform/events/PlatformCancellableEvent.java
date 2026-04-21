package org.vicky.platform.events;

public interface PlatformCancellableEvent extends PlatformEvent {
    /**
     * If the event is cancelable.
     */
    boolean isCancelable();

    /**
     * If cancelable, is it currently cancelled.
     */
    boolean isCancelled();

    /**
     * If cancelable, cancel or uncancel.
     */
    void setCancelled(boolean cancelled);
}
