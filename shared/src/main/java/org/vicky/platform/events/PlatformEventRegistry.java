package org.vicky.platform.events;

import org.vicky.platform.exceptions.UnsupportedEventException;

public interface PlatformEventRegistry {
    void registerEvent(EventDescriptor event) throws UnsupportedEventException;
    EventDescriptor getDescriptor(Class<? extends PlatformEvent> type);
}
