package org.vicky.forgeplatform;

import net.minecraft.world.level.Level;
import org.vicky.forgeplatform.useables.ForgePlatformWorldAdapter;
import org.vicky.forgeplatform.useables.ForgeVec3;
import org.vicky.platform.PlatformLocationAdapter;
import org.vicky.platform.world.PlatformLocation;

public class ForgePlatformLocationAdapter implements PlatformLocationAdapter<ForgeVec3> {
    @Override
    public ForgeVec3 toNative(PlatformLocation location) {
        ForgePlatformWorldAdapter adapter = (ForgePlatformWorldAdapter) location.getWorld();
        Level world = adapter.world();
        return new ForgeVec3(world, location.getX(), location.getY(), location.getZ(), location.yaw, location.pitch);
    }

    @Override
    public PlatformLocation fromNative(ForgeVec3 nativeLocation) {
        return nativeLocation;
    }
}
