/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.bukkit.Location;
import org.bukkit.World;
import org.vicky.platform.PlatformLocationAdapter;
import org.vicky.platform.world.PlatformLocation;

public class BukkitLocationAdapter implements PlatformLocationAdapter<Location> {

	public static PlatformLocation from(Location loc) {
		return new PlatformLocation(new BukkitWorldAdapter(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ());
	}

	public static Location to(PlatformLocation location) {
		BukkitWorldAdapter adapter = (BukkitWorldAdapter) location.getWorld();
		return new Location(adapter.getBukkitWorld(), location.getX(), location.getY(), location.getZ());
	}

	@Override
	public Location toNative(PlatformLocation location) {
		BukkitWorldAdapter adapter = (BukkitWorldAdapter) location.getWorld();
		World bukkitWorld = adapter.getBukkitWorld();
		return new Location(bukkitWorld, location.getX(), location.getY(), location.getZ());
	}

	@Override
	public PlatformLocation fromNative(Location nativeLocation) {
		if (!(nativeLocation instanceof Location loc))
			throw new IllegalArgumentException("Expected Bukkit Location");
		return new PlatformLocation(new BukkitWorldAdapter(loc.getWorld()), loc.getX(), loc.getY(), loc.getZ());
	}
}
