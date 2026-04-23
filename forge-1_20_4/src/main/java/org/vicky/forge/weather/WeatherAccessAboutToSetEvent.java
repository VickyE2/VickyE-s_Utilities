package org.vicky.forge.weather;

import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WeatherAccessAboutToSetEvent extends Event {
    private final LevelWeatherAccess requested;
    @Nullable
    private LevelWeatherAccess replacement;

    public WeatherAccessAboutToSetEvent(LevelWeatherAccess requested) {
        this.requested = requested;
    }

    public LevelWeatherAccess getRequested() {
        return requested;
    }

    @Nullable
    public LevelWeatherAccess getReplacement() {
        return replacement;
    }

    public void setReplacement(@NotNull LevelWeatherAccess replacement) {
        this.replacement = replacement;
    }
}