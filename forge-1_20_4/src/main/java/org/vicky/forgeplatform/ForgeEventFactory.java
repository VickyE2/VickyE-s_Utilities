package org.vicky.forgeplatform;

import net.minecraftforge.common.MinecraftForge;
import org.vicky.forgeplatform.useables.ForgeEvent;
import org.vicky.platform.events.PlatformEvent;
import org.vicky.platform.events.PlatformEventFactory;
import org.vicky.platform.exceptions.UnsupportedEventException;

public class ForgeEventFactory implements PlatformEventFactory {
    @Override
    public <T extends PlatformEvent> T firePlatformEvent(T event) throws UnsupportedEventException {
        if (event instanceof ForgeEvent forgeEvent) {
            return (T) MinecraftForge.EVENT_BUS.fire(forgeEvent.getEvent());
        } else {
            throw new IllegalArgumentException("Expected event of type `ForgeEvent` got " + event.getClass().getSimpleName());
        }
    }
}
