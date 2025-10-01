/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform.useables;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.vicky.platform.world.PlatformBlock;
import org.vicky.platform.world.PlatformBlockState;
import org.vicky.platform.world.PlatformLocation;
import org.vicky.platform.world.PlatformMaterial;

public record ForgePlatformBlockAdapter(BlockPos pos, BlockState state,
                                        Level world) implements PlatformBlock<BlockState> {
    @Override
    public boolean isSolid() {
        return state.isSolid();
    }

    @Override
    public PlatformMaterial getMaterial() {
        return new ForgePlatformMaterial(state.getBlock());
    }

    @Override
    public PlatformLocation getLocation() {
        return new ForgeVec3(world, pos, 0, 0);
    }

    @Override
    public PlatformBlockState<BlockState> getBlockState() {
        return new ForgePlatformBlockStateAdapter(state);
    }

    @Override
    public void setBlockState(PlatformBlockState<BlockState> state) {
        world.setBlock(pos, state.getNative(), 3);
    }
}
