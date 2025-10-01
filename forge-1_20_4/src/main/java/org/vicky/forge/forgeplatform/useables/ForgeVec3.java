/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform.useables;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.vicky.platform.world.PlatformLocation;
import org.vicky.platform.world.PlatformWorld;

public class ForgeVec3 extends PlatformLocation {
    private final Level world;
    private final PlatformWorld<BlockState, Level> platworld;

    public ForgeVec3(Level level, double x, double y, double z, float yaw, float pitch) {
        super(new ForgePlatformWorldAdapter(level), x, y, z);
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = level;
        this.platworld = new ForgePlatformWorldAdapter(level);
    }

    public ForgeVec3(Level level, BlockPos pos, float yaw, float pitch) {
        super(new ForgePlatformWorldAdapter(level), pos.getX(), pos.getY(), pos.getZ());
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = level;
        this.platworld = new ForgePlatformWorldAdapter(level);
    }

    public PlatformWorld<BlockState, Level> getWorld() {
        return platworld;
    }

    public Level getForgeWorld() {
        return world;
    }
}
