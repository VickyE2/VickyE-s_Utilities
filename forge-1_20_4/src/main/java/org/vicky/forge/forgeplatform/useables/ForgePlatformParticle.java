/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.useables;

import org.vicky.platform.entity.PlatformParticle;

import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public record ForgePlatformParticle(ParticleOptions particleOptions) implements PlatformParticle {

	@Override
	public String getId() {
		ResourceLocation key = BuiltInRegistries.PARTICLE_TYPE.getKey(particleOptions.getType());
		return key != null ? key.toString() : "unknown";
	}

	@Override
	public boolean supportsColor() {
		return particleOptions instanceof DustParticleOptions;
	}

	@Override
	public boolean supportsTransition() {
		return particleOptions instanceof DustColorTransitionOptions;
	}
}
