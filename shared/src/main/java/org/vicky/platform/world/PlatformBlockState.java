package org.vicky.platform.world;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface PlatformBlockState<T> {
    /**
     * Returns the string identifier of the block type.
     * For example: "minecraft:stone", "minecraft:oak_log"
     */
    @NotNull
    String getId();

    @NotNull
    PlatformMaterial getMaterial();

    @NotNull
    T getNative();

    @NotNull
    Map<String, String> getProperties();

    @NotNull
    <P> P getProperty(String name);
}
