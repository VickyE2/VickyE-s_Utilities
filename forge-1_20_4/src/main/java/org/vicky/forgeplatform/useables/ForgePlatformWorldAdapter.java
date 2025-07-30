package org.vicky.forgeplatform.useables;

import net.minecraft.world.level.Level;
import org.vicky.platform.world.PlatformWorld;

public record ForgePlatformWorldAdapter(Level world) implements PlatformWorld {

    @Override
    public String getName() {
        return world.dimension().location().toString();
    }

    @Override
    public Object getNative() {
        return world;
    }
}
