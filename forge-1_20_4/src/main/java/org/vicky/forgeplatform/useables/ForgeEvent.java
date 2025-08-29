package org.vicky.forgeplatform.useables;

import net.minecraftforge.eventbus.api.Event;
import org.vicky.platform.events.PlatformEvent;

public class ForgeEvent implements PlatformEvent {
    private final String name;
    private final Event event;

    public ForgeEvent(Event event) {
        this.name = event.getClass().getSimpleName();
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public String getEventName() {
        return name;
    }
}
