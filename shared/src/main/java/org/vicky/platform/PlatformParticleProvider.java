/* Licensed under Apache-2.0 2025. */
package org.vicky.platform;

import org.vicky.platform.entity.PlatformParticle;
import org.vicky.platform.world.PlatformLocation;

public interface PlatformParticleProvider {
	void spawnBasic(PlatformParticle type, PlatformLocation loc, int count, double spreadX, double spreadY,
			double spreadZ, float speed, float size);

	void spawnColored(PlatformParticle type, PlatformLocation loc, int count, double spreadX, double spreadY,
			double spreadZ, float speed, IColor color, float size);

	void spawnTransition(PlatformParticle type, PlatformLocation loc, int count, double spreadX, double spreadY,
			double spreadZ, float speed, IColor from, IColor to, float size);
}
