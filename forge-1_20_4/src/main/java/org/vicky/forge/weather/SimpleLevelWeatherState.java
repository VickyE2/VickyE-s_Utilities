package org.vicky.forge.weather;

import net.minecraft.resources.ResourceLocation;

public class SimpleLevelWeatherState implements LevelWeatherState {

    private WeatherSnapshot snapshot = WeatherSnapshot.vanillaClear();

    @Override
    public WeatherSnapshot snapshot() {
        return snapshot;
    }

    @Override
    public void apply(WeatherSnapshot snapshot) {
        this.snapshot = snapshot;
    }
}