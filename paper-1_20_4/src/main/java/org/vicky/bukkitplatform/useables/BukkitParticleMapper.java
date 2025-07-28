/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Particle;
import org.jetbrains.annotations.NotNull;

public class BukkitParticleMapper {
	private static final Map<String, Particle> PARTICLE_MAP = new HashMap<>();

	static {
		for (Particle particle : Particle.values()) {
			PARTICLE_MAP.put(particle.name().toLowerCase(Locale.ROOT), particle);
			PARTICLE_MAP.put("minecraft:" + particle.name().toLowerCase(Locale.ROOT), particle); // handle resource
																									// location style
		}
	}

	public static @NotNull Particle map(@NotNull String particleId) {
		Particle particle = PARTICLE_MAP.get(particleId.toLowerCase(Locale.ROOT));
		if (particle == null) {
			throw new IllegalArgumentException("Unknown particle id: " + particleId);
		}
		return particle;
	}
}
