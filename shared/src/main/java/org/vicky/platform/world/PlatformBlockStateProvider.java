package org.vicky.platform.world;

public interface PlatformBlockStateProvider {
    PlatformBlockState<?> getBlockState(String type);
}

