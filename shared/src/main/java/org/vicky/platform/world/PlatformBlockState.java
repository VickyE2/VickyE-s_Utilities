package org.vicky.platform.world;

import java.util.Map;

public interface PlatformBlockState<T> {
    /**
     * Returns the string identifier of the block type.
     * For example: "minecraft:stone", "minecraft:oak_log"
     */
    String getId();

    PlatformMaterial getMaterial();

    T getNative();

    Map<String, String> getProperties();

    <P> P getProperty(String name);
}
