/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform;

import net.minecraftforge.common.MinecraftForge;
import org.vicky.forge.forgeplatform.useables.ForgeEvent;
import org.vicky.platform.events.PlatformEvent;
import org.vicky.platform.events.PlatformEventFactory;
import org.vicky.platform.exceptions.UnsupportedEventException;

public class ForgeEventFactory implements PlatformEventFactory {
    public static final ForgeEventFactory INSTANCE =
            new ForgeEventFactory();

    private ForgeEventFactory() {}

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PlatformEvent> T firePlatformEvent(T event) throws UnsupportedEventException {
        if (event instanceof ForgeEvent forgeEvent) {
            return (T) MinecraftForge.EVENT_BUS.fire(forgeEvent.getEvent());
        } else {
            throw new IllegalArgumentException(
                    "Expected event of type `ForgeEvent` got " + event.getClass().getSimpleName());
        }
    }
}
