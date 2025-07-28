/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.vicky.bukkitplatform.useables.BukkitColorAdapter;
import org.vicky.bukkitplatform.useables.BukkitLocationAdapter;
import org.vicky.bukkitplatform.useables.BukkitParticleMapper;
import org.vicky.platform.IColor;
import org.vicky.platform.PlatformParticleProvider;
import org.vicky.platform.entity.PlatformParticle;
import org.vicky.platform.world.PlatformLocation;

public class BukkitParticleProvider implements PlatformParticleProvider {
	@Override
	public void spawnBasic(PlatformParticle type, PlatformLocation loc, int count, double sx, double sy, double sz,
			float speed, float size) {
		Location location = BukkitLocationAdapter.to(loc);
		location.getWorld().spawnParticle(BukkitParticleMapper.map(type.getId()), location, count, sx, sy, sz, speed);
	}

	@Override
	public void spawnColored(PlatformParticle type, PlatformLocation loc, int count, double sx, double sy, double sz,
			float speed, IColor color, float size) {
		Location location = BukkitLocationAdapter.to(loc);
		location.getWorld().spawnParticle(Particle.REDSTONE, location, count, sx, sy, sz, speed,
				new Particle.DustOptions(BukkitColorAdapter.adapt(color), size));
	}

	@Override
	public void spawnTransition(PlatformParticle type, PlatformLocation loc, int count, double spreadX, double spreadY,
			double spreadZ, float speed, IColor from, IColor to, float size) {
		Location location = BukkitLocationAdapter.to(loc);
		location.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, location, 1, spreadX, spreadY, spreadZ, speed,
				new Particle.DustTransition(BukkitColorAdapter.adapt(from), BukkitColorAdapter.adapt(to), size));
	}
}
