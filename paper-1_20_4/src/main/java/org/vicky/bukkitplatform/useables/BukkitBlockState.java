/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.vicky.platform.world.PlatformBlockState;
import org.vicky.platform.world.PlatformMaterial;

import java.util.Map;

public class BukkitBlockState implements PlatformBlockState<BlockData> {

    private final BlockData data;

    public BukkitBlockState(BlockData data) {
        this.data = data;
    }

    public static BukkitBlockState from(Material material) {
        return new BukkitBlockState(material.createBlockData());
    }

    @Override
    public String getId() {
        return data.getAsString();
    }

    @Override
    public PlatformMaterial getMaterial() {
        return new BukkitMaterial(data.getMaterial());
    }

    @Override
    public BlockData getNative() {
        return data;
    }

    @Override
    public Map<String, String> getProperties() {
        return Map.of();
    }

    @Override
    public <P> P getProperty(String name) {
        return null;
    }
}
