/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.world;

import org.vicky.platform.utils.ResourceLocation;

import net.kyori.adventure.text.Component;

public interface PlatformBiome {
	ResourceLocation id();

	/** Returns the formatted translatable display name of the biome. */
	Component displayName();

	// ==================== Climate & Environment ====================

	/**
	 * Returns the temperature value of the biome (affects snow/rain/mob spawns).
	 */
	float temperature();

	/** Returns the downfall value (rainfall/snowfall intensity). */
	float downfall();

	/** Returns true if the biome has precipitation (rain or snow). */
	boolean hasPrecipitation();

	// ==================== Visuals & Colors ====================

	/** Returns the RGB integer value for the fog color. */
	int fogColor();

	/** Returns the RGB integer value for water color. */
	int waterColor();

	/** Returns the RGB integer value for underwater fog color. */
	int waterFogColor();

	/** Returns the RGB integer value for sky color. */
	int skyColor();
}
