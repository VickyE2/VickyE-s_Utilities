package org.vicky.platform.world;

import org.jetbrains.annotations.NotNull;
import org.vicky.platform.utils.ResourceLocation;

public interface PlatformBlockStateFactory {
    @NotNull PlatformBlockState<?> getBlockState(String type);
    default @NotNull PlatformBlockState<?> getBlockState(ResourceLocation type) {
        return getBlockState(type.asString());
    }
}

