/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform;

import net.minecraft.world.level.CommonLevelAccessor;
import org.vicky.forge.forgeplatform.useables.ForgePlatformWorldAdapter;
import org.vicky.forge.forgeplatform.useables.ForgeVec3;
import org.vicky.platform.PlatformLocationAdapter;
import org.vicky.platform.world.PlatformLocation;

import net.minecraft.world.level.Level;

public class ForgePlatformLocationAdapter implements PlatformLocationAdapter<ForgeVec3> {
	@Override
	public ForgeVec3 toNative(PlatformLocation location) {
		ForgePlatformWorldAdapter adapter = (ForgePlatformWorldAdapter) location.getWorld();
		CommonLevelAccessor world = adapter.world();
		return new ForgeVec3(world, location.getX(), location.getY(), location.getZ(), location.yaw, location.pitch);
	}

	@Override
	public PlatformLocation fromNative(ForgeVec3 nativeLocation) {
		return nativeLocation;
	}

	public static ForgeVec3 toNativeS(PlatformLocation location) {
		ForgePlatformWorldAdapter adapter = (ForgePlatformWorldAdapter) location.getWorld();
		CommonLevelAccessor world = adapter.world();
		return new ForgeVec3(world, location.getX(), location.getY(), location.getZ(), location.yaw, location.pitch);
	}

	public static PlatformLocation fromNativeS(ForgeVec3 nativeLocation) {
		return nativeLocation;
	}
}
