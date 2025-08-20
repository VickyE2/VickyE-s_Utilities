package org.vicky.platform.world;

import de.pauleff.api.ICompoundTag;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.utils.Vec3;

import java.util.List;

public interface PlatformWorld<T, N> {
    String getName();

    N getNative();

    int getHighestBlockYAt(double x, double z);

    int getMaxWorldHeight();

    List<PlatformPlayer> getPlayers();

    PlatformBlock<T> getBlockAt(double x, double y, double z);

    PlatformBlock<T> getBlockAt(Vec3 pos);

    PlatformBlockState<T> getAirBlockState();

    PlatformBlockState<T> getWaterBlockState();

    /**
     * Sets the block state at the given position.
     *
     * @param position The location to modify.
     * @param state    The new block state to set.
     */
    void setPlatformBlockState(Vec3 position, PlatformBlockState<T> state);

    /**
     * Similar to {@link PlatformWorld#setPlatformBlockState(Vec3, PlatformBlockState)} but allows for setting nbt mainly for
     * block entities
     *
     * @param position The location to modify.
     * @param state    The new block state to set.
     * @param nbt      The compound nbt to parse. Each Impl would do this their own way
     */
    void setPlatformBlockState(Vec3 position, PlatformBlockState<T> state, ICompoundTag nbt);

    PlatformBlockState<T> createPlatformBlockState(String id, String properties);

    /**
     * Ensures the chunk containing this block position is loaded.
     * Platform-specific implementations handle the actual chunk logic.
     */
    void loadChunkIfNeeded(int chunkX, int chunkZ);

    boolean isChunkLoaded(int chunkX, int chunkZ);

    default void ensureChunkLoadedFor(Vec3 pos) {
        loadChunkIfNeeded((int) pos.x >> 4, (int) pos.z >> 4);
    }
}