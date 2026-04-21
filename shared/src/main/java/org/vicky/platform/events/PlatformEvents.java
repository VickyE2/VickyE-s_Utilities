package org.vicky.platform.events;

import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.entity.PlatformLivingEntity;
import org.vicky.platform.world.PlatformWorld;

public class PlatformEvents {
    public static class LivingEntityEvent implements PlatformEvent {
        public final PlatformLivingEntity player;

        public LivingEntityEvent(PlatformLivingEntity player) {
            this.player = player;
        }
    }
    public static class LivingEntityTickEvent extends LivingEntityEvent {
        public LivingEntityTickEvent(PlatformLivingEntity player) {
            super(player);
        }
    }

    public static class PlayerEvent extends LivingEntityEvent {
        public PlayerEvent(PlatformPlayer player) {
            super(player);
        }
    }
    public static class PlayerTickEvent extends PlayerEvent {
        public PlayerTickEvent(PlatformPlayer player) {
            super(player);
        }
    }

    public static class WorldEvent<T> implements PlatformEvent {
        public final PlatformWorld<?, T> world;

        public WorldEvent(PlatformWorld<?, T> world) {
            this.world = world;
        }
    }
    public static class WeatherChangeEvent<T> extends WorldEvent<T> implements PlatformCancellableEvent {
        public boolean cancelled;

        public WeatherChangeEvent(PlatformWorld<?, T> world) {
            super(world);
        }

        @Override
        public boolean isCancelable() {
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
