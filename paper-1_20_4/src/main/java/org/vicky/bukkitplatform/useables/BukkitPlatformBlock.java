/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.vicky.platform.world.PlatformBlock;
import org.vicky.platform.world.PlatformBlockState;
import org.vicky.platform.world.PlatformLocation;
import org.vicky.platform.world.PlatformMaterial;

public class BukkitPlatformBlock implements PlatformBlock<BlockData> {

    private final Block block;

    public BukkitPlatformBlock(Block block) {
        this.block = block;
    }

    @Override
    public boolean isSolid() {
        return block.isSolid();
    }

    @Override
    public PlatformMaterial getMaterial() {
        return new BukkitMaterial(block.getBlockData().getPlacementMaterial());
    }

    @Override
    public PlatformLocation getLocation() {
        return BukkitLocationAdapter.from(block.getLocation());
    }

    @Override
    public PlatformBlockState<BlockData> getBlockState() {
        return new BukkitBlockState(block.getBlockData());
    }

    @Override
    public void setBlockState(PlatformBlockState<BlockData> platformBlockState) {
        block.setBlockData(platformBlockState.getNative());
    }
}
