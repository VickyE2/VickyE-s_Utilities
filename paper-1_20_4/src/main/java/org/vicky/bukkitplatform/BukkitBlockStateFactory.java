/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.vicky.bukkitplatform.useables.BukkitBlockState;
import org.vicky.platform.world.PlatformBlockState;
import org.vicky.platform.world.PlatformBlockStateFactory;

public class BukkitBlockStateFactory implements PlatformBlockStateFactory {
    @Override
    public PlatformBlockState<BlockData> getBlockState(String type) {
        return BukkitBlockState.from(Bukkit.createBlockData(type).getMaterial());
    }
}
