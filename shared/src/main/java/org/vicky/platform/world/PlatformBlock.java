package org.vicky.platform.world;

import org.jetbrains.annotations.NotNull;
import org.vicky.platform.utils.IntVec3;
import org.vicky.platform.utils.Vec3;

public interface PlatformBlock<T> {
    /** This dosent mean literal solid but just not air **/
    boolean isSolid();

    @NotNull PlatformMaterial getMaterial();

    @NotNull PlatformLocation getLocation();
    default @NotNull IntVec3 getBlockPos() {
        return new IntVec3(getLocation().x, getLocation().y, getLocation().y);
    }

    @NotNull PlatformBlockState<T> getBlockState();

    void setBlockState(PlatformBlockState<T> state);
}
