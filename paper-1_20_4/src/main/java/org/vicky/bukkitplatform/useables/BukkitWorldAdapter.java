/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.bukkit.World;
import org.vicky.platform.world.PlatformWorld;

public class BukkitWorldAdapter implements PlatformWorld {
	private final World world;

	public BukkitWorldAdapter(World world) {
		this.world = world;
	}

	@Override
	public String getName() {
		return world.getName();
	}

	@Override
	public Object getNative() {
		return world;
	}

	public World getBukkitWorld() {
		return world;
	}
}
