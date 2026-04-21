package org.vicky.platform.events;

public class EventListener<T extends PlatformEvent> {
    public final Class<T> type;
    public final EventPriority priority;
    public final EventExecutor<T> executor;

    public EventListener(Class<T> type, EventPriority priority, EventExecutor<T> executor) {
        this.type = type;
        this.priority = priority;
        this.executor = executor;
    }

    @FunctionalInterface
    public interface EventExecutor<T extends PlatformEvent> {
        void execute(T event);
    }
}