/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.useables;

import net.minecraft.world.level.CommonLevelAccessor;
import org.vicky.platform.world.PlatformLocation;
import org.vicky.platform.world.PlatformWorld;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class ForgeVec3 extends PlatformLocation {
	private final CommonLevelAccessor world;
	private final PlatformWorld<BlockState, CommonLevelAccessor> platworld;

	public ForgeVec3(CommonLevelAccessor level, double x, double y, double z, float yaw, float pitch) {
		super(new ForgePlatformWorldAdapter(level), x, y, z);
		this.yaw = yaw;
		this.pitch = pitch;
		this.world = level;
		this.platworld = new ForgePlatformWorldAdapter(level);
	}

	public ForgeVec3(CommonLevelAccessor level, BlockPos pos, float yaw, float pitch) {
		super(new ForgePlatformWorldAdapter(level), pos.getX(), pos.getY(), pos.getZ());
		this.yaw = yaw;
		this.pitch = pitch;
		this.world = level;
		this.platworld = new ForgePlatformWorldAdapter(level);
	}

	public PlatformWorld<BlockState, CommonLevelAccessor> getWorld() {
		return platworld;
	}

	public CommonLevelAccessor getForgeWorld() {
		return world;
	}
	@Override public String toString() { return "ForgeVec3[" + x + "," + y + "," + z + "]"; }
}
