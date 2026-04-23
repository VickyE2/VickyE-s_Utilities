package org.vicky.forge.weather;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.Objects;

@Cancelable
public class ForgeWeatherChangeEvent extends Event {
    private final ServerLevel level;
    private final WeatherSnapshot oldWeather;
    private final WeatherSnapshot newWeather;

    public ForgeWeatherChangeEvent(ServerLevel level, WeatherSnapshot oldWeather, WeatherSnapshot newWeather) {
        this.level = level;
        this.oldWeather = oldWeather;
        this.newWeather = newWeather;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public WeatherSnapshot getOldWeather() {
        return oldWeather;
    }

    public WeatherSnapshot getNewWeather() {
        return newWeather;
    }

    public boolean changedRaining() {
        return oldWeather.raining() != newWeather.raining();
    }

    public boolean changedThundering() {
        return oldWeather.thundering() != newWeather.thundering();
    }

    public boolean changedWeatherId() {
        return !Objects.equals(oldWeather.weatherId(), newWeather.weatherId());
    }

    public boolean changedStrength() {
        return Float.compare(oldWeather.strength(), newWeather.strength()) != 0;
    }
}
