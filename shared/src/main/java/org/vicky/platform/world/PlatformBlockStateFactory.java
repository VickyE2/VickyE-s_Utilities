package org.vicky.platform.world;

public interface PlatformBlockStateFactory {
    PlatformBlockState<?> getBlockState(String type);
}

