package org.vicky.forge.weather;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleLevelWeatherAccess implements LevelWeatherAccess {

    private final Map<ResourceKey<Level>, SimpleLevelWeatherState> states = new ConcurrentHashMap<>();

    @Override
    public Optional<LevelWeatherState> get(ServerLevel level) {
        return Optional.ofNullable(states.get(level.dimension()));
    }

    @Override
    public LevelWeatherState getOrCreate(ServerLevel level) {
        return states.computeIfAbsent(level.dimension(), key -> new SimpleLevelWeatherState());
    }
}