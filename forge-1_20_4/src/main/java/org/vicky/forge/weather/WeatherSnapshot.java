package org.vicky.forge.weather;

import net.minecraft.resources.ResourceLocation;

/**
 * A snapshot of the current weather state for a level.
 * weatherId is for vanilla or custom weather identifiers.
 */
public record WeatherSnapshot(
        boolean raining,
        boolean thundering,
        ResourceLocation weatherId,
        float strength
) {
    public static WeatherSnapshot vanillaClear() {
        return new WeatherSnapshot(
                false,
                false,
                ResourceLocation.fromNamespaceAndPath("minecraft", "clear"),
                0.0f
        );
    }
}

