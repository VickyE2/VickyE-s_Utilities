package org.vicky.platform.events;

public record EventDescriptor(
        Class<? extends PlatformEvent> eventType,
        boolean cancellable
) {
}
