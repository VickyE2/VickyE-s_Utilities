package org.vicky.platform.events;

import org.vicky.platform.exceptions.UnsupportedEventException;

public interface PlatformEventDispatcher {
    <T extends PlatformEvent> T firePlatformEvent(T event) throws UnsupportedEventException;
    <T extends PlatformEvent> void registerListener(
            Class<T> type, EventPriority priority, EventListener.EventExecutor<T> executor);
}