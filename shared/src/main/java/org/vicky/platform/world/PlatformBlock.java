package org.vicky.platform.world;

import org.vicky.platform.utils.Vec3;

public interface PlatformBlock<T> {
    boolean isSolid();

    PlatformMaterial getMaterial();
    PlatformLocation getLocation();

    default Vec3 getPosition() {
        return new Vec3(getLocation().x, getLocation().y, getLocation().y);
    }

    PlatformBlockState<T> getBlockState();

    void setBlockState(PlatformBlockState<T> state);
}
