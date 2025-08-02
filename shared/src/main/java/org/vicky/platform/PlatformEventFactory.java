package org.vicky.platform;

import org.vicky.platform.exceptions.UnsupportedEventException;

public interface PlatformEventFactory {
    <T extends PlatformEvent> T firePlatformEvent(T event) throws UnsupportedEventException;
}
