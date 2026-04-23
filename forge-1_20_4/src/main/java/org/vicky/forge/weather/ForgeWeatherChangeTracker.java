package org.vicky.forge.weather;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap; /**
 * Checks each server level every level tick and fires ForgeWeatherChangeEvent
 * whenever the snapshot changes.
 */
public final class ForgeWeatherChangeTracker {
    private static final Map<Level, WeatherSnapshot> LAST =
            Collections.synchronizedMap(new WeakHashMap<>());

    private static volatile LevelWeatherAccess weatherAccess;

    private ForgeWeatherChangeTracker() {
    }

    /**
     * Fires an event before the access object is stored.
     * Another listener may replace the access implementation.
     */
    public static void setWeatherAccess(LevelWeatherAccess requestedAccess) {
        WeatherAccessAboutToSetEvent event = new WeatherAccessAboutToSetEvent(requestedAccess);
        MinecraftForge.EVENT_BUS.post(event);

        weatherAccess = event.getReplacement() != null
                ? event.getReplacement()
                : requestedAccess;
    }

    public static void register(IEventBus bus) {
        bus.addListener(EventPriority.NORMAL, ForgeWeatherChangeTracker::onLevelTick);
    }

    private static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.level instanceof ServerLevel level)) return;

        WeatherSnapshot current = readSnapshot(level);
        WeatherSnapshot previous = LAST.get(level);

        if (previous == null) {
            LAST.put(level, current);
            return;
        }

        if (previous.equals(current)) return;

        ForgeWeatherChangeEvent changeEvent = new ForgeWeatherChangeEvent(level, previous, current);
        MinecraftForge.EVENT_BUS.post(changeEvent);

        if (changeEvent.isCanceled()) {
            restoreSnapshot(level, previous);
            LAST.put(level, previous);
            return;
        }

        LAST.put(level, current);
    }

    private static WeatherSnapshot readSnapshot(ServerLevel level) {
        LevelWeatherAccess access = weatherAccess;
        if (access != null) {
            Optional<LevelWeatherState> state = access.get(level);
            if (state.isPresent()) {
                return state.get().snapshot();
            }
        }

        // Vanilla fallback.
        // If you expose extra custom state elsewhere, put it into weatherAccess.
        ResourceLocation vanillaId;
        if (level.isThundering()) {
            vanillaId = ResourceLocation.fromNamespaceAndPath("minecraft", "thunder");
        } else if (level.isRaining()) {
            vanillaId = ResourceLocation.fromNamespaceAndPath("minecraft", "rain");
        } else {
            vanillaId = ResourceLocation.fromNamespaceAndPath("minecraft", "clear");
        }

        return new WeatherSnapshot(
                level.isRaining(),
                level.isThundering(),
                vanillaId,
                1.0f
        );
    }

    private static void restoreSnapshot(ServerLevel level, WeatherSnapshot snapshot) {
        LevelWeatherAccess access = weatherAccess;
        if (access != null) {
            access.get(level).ifPresent(state -> state.apply(snapshot));
            return;
        }

        // No custom state backend installed.
        // Restore logic belongs in your weather controller if you need cancellation to fully revert.
    }
}
