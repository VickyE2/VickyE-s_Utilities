/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.navigation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.platform.entity.AbstractPathNode;
import org.vicky.platform.utils.IntVec3;

import net.minecraft.world.level.pathfinder.Node;

public class ForgePlatformNode implements AbstractPathNode {
	public final Node ordinal;

	private ForgePlatformNode(Node nav) {
		this.ordinal = nav;
	}

	public static ForgePlatformNode from(Node nav) {
		return new ForgePlatformNode(nav);
	}

	@Override
	public int getX() {
		return ordinal.x;
	}

	@Override
	public int getY() {
		return ordinal.y;
	}

	@Override
	public int getZ() {
		return ordinal.z;
	}

	@Override
	public float distanceTo(@Nullable AbstractPathNode abstractPathNode) {
		if (abstractPathNode instanceof ForgePlatformNode f)
			return ordinal.distanceTo(f.ordinal);
		return 0;
	}

	@Override
	public @NotNull IntVec3 asVec() {
		return ForgeHacks.toVicky(ordinal.asBlockPos());
	}
}
