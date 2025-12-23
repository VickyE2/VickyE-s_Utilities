/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform;

import org.vicky.forge.forgeplatform.useables.ForgePlatformParticle;
import org.vicky.forge.forgeplatform.useables.ForgeVec3;
import org.vicky.platform.IColor;
import org.vicky.platform.PlatformParticleProvider;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.entity.PlatformParticle;
import org.vicky.platform.world.PlatformLocation;

import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class ForgeParticleProvider implements PlatformParticleProvider {
	@Override
	public void spawnBasic(PlatformParticle type, PlatformLocation loc, int count, double spreadX, double spreadY,
			double spreadZ, float speed, float size) {
		if (!(type instanceof ForgePlatformParticle))
			throw new IllegalArgumentException("Expected ForgePlatformParticle type");
		if (((ForgeVec3) PlatformPlugin.locationAdapter().toNative(loc)).getForgeWorld() instanceof ServerLevel serverLevel) {
			ParticleOptions particle = ((ForgePlatformParticle) type).particleOptions(); // Your PlatformParticle maps
			serverLevel.sendParticles(particle, loc.x, loc.y, loc.z, count, spreadX, spreadY, spreadZ, speed);
		}
	}

	@Override
	public void spawnColored(PlatformParticle type, PlatformLocation loc, int count, double spreadX, double spreadY,
			double spreadZ, float speed, IColor color, float size) {
		if (!(type instanceof ForgePlatformParticle))
			throw new IllegalArgumentException("Expected ForgePlatformParticle type");
		if (((ForgeVec3) PlatformPlugin.locationAdapter().toNative(loc)).getForgeWorld() instanceof ServerLevel serverLevel) {
			DustParticleOptions dust = (DustParticleOptions) ((ForgePlatformParticle) type).particleOptions();
			serverLevel.sendParticles(dust, loc.x, loc.y, loc.z, count, spreadX, spreadY, spreadZ, speed);
		}
	}

	@Override
	public void spawnTransition(PlatformParticle type, PlatformLocation loc, int count, double spreadX, double spreadY,
			double spreadZ, float speed, IColor from, IColor to, float size) {
		if (!(type instanceof ForgePlatformParticle))
			throw new IllegalArgumentException("Expected ForgePlatformParticle type");
		if (((ForgeVec3) PlatformPlugin.locationAdapter().toNative(loc)).getForgeWorld() instanceof ServerLevel serverLevel) {
			DustColorTransitionOptions transition = (DustColorTransitionOptions) ((ForgePlatformParticle) type)
					.particleOptions();
			serverLevel.sendParticles(transition, loc.x, loc.y, loc.z, count, spreadX, spreadY, spreadZ, speed);
		}
	}
}
