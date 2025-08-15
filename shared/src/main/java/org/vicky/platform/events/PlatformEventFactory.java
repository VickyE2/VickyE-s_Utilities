package org.vicky.platform.events;

import org.vicky.platform.exceptions.UnsupportedEventException;

public interface PlatformEventFactory {
    <T extends PlatformEvent> T firePlatformEvent(T event) throws UnsupportedEventException;
}
