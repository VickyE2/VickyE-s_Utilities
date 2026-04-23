package org.vicky.forge.weather;

public interface LevelWeatherState {
    WeatherSnapshot snapshot();

    /**
     * Restore/apply the given snapshot.
     * Implement this with your custom weather controller.
     */
    void apply(WeatherSnapshot snapshot);
}
