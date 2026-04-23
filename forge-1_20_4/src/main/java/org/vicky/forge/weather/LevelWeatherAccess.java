package org.vicky.forge.weather;

import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

/**
 * Plug your custom weather storage here.
 * Implement this with a capability/attachment/persistent state.
 */
public interface LevelWeatherAccess {
    Optional<LevelWeatherState> get(ServerLevel level);
    LevelWeatherState getOrCreate(ServerLevel level);
}
