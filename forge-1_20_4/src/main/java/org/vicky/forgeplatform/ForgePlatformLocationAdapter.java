package org.vicky.forgeplatform;

import net.minecraft.world.level.Level;
import org.vicky.platform.PlatformLocationAdapter;
import org.vicky.platform.world.PlatformLocation;
import org.vicky.forgeplatform.useables.ForgePlatformWorldAdapter;
import org.vicky.forgeplatform.useables.ForgeVec3;

public class ForgePlatformLocationAdapter implements PlatformLocationAdapter {

    public static PlatformLocation from(ForgeVec3 loc) {
        return new PlatformLocation(new ForgePlatformWorldAdapter(loc.getForgeWorld()), loc.getX(), loc.getY(), loc.getZ());
    }

    public static ForgeVec3 to(PlatformLocation location) {
        ForgePlatformWorldAdapter adapter = (ForgePlatformWorldAdapter) location.getWorld();
        return new ForgeVec3(adapter.world(),location.getX(), location.getY(), location.getZ());
    }

    @Override
    public ForgeVec3 toNative(PlatformLocation location) {
        ForgePlatformWorldAdapter adapter = (ForgePlatformWorldAdapter) location.getWorld();
        Level world = adapter.world();
        return new ForgeVec3(world, location.getX(), location.getY(), location.getZ());
    }

    @Override
    public PlatformLocation fromNative(Object nativeLocation) {
        if (!(nativeLocation instanceof ForgeVec3 loc))
            throw new IllegalArgumentException("Expected ForgeVec3 Location");
        return new PlatformLocation(new ForgePlatformWorldAdapter(loc.getForgeWorld()), loc.getX(), loc.getY(), loc.getZ());
    }
}
