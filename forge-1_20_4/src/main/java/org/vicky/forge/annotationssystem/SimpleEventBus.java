package org.vicky.forge.annotationssystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SimpleEventBus {
    
    // Maps an Event Class type to a list of matching functional consumers
    private final Map<Class<?>, List<Consumer<?>>> listeners = new HashMap<>();

    /**
     * Explicitly registers a listener for a specific event class.
     * Replicates Forge's bus.addListener(EventClass::onEvent) style.
     */
    public <T> void addListener(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Dispatches an event instance to all registered consumers of its type.
     */
    @SuppressWarnings("unchecked")
    public <T> void post(T event) {
        List<Consumer<?>> eventConsumers = listeners.get(event.getClass());
        
        if (eventConsumers != null) {
            for (Consumer<?> consumer : eventConsumers) {
                // Safe cast because addListener guarantees type alignment
                ((Consumer<T>) consumer).accept(event);
            }
        }
    }
}
