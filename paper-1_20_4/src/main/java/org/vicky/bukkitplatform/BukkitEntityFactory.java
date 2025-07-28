/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.vicky.bukkitplatform.useables.BukkitArrowAdapter;
import org.vicky.bukkitplatform.useables.BukkitLocationAdapter;
import org.vicky.platform.PlatformEntityFactory;
import org.vicky.platform.entity.PlatformArrow;
import org.vicky.platform.world.PlatformLocation;

public class BukkitEntityFactory implements PlatformEntityFactory {
	@Override
	public PlatformArrow spawnArrowAt(PlatformLocation loc) {
		Location location = BukkitLocationAdapter.to(loc);
		return new BukkitArrowAdapter(location.getWorld().spawnArrow(location, new Vector(loc.x, loc.y, loc.z), 0, 0));
	}
}
