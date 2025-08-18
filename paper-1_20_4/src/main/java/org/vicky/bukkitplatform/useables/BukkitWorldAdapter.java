/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import de.pauleff.api.ICompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.vicky.platform.utils.Vec3;
import org.vicky.platform.world.PlatformBlock;
import org.vicky.platform.world.PlatformBlockState;
import org.vicky.platform.world.PlatformWorld;

public class BukkitWorldAdapter implements PlatformWorld<BlockData, World> {
	private final World world;

	public BukkitWorldAdapter(World world) {
		this.world = world;
	}

	@Override
	public String getName() {
		return world.getName();
	}

	@Override
	public World getNative() {
		return world;
	}

	@Override
	public int getHighestBlockYAt(double v, double v1) {
		return world.getHighestBlockYAt((int) v, (int) v1);
	}

	@Override
	public int getMaxWorldHeight() {
		return world.getMaxHeight();
	}

	@Override
	public PlatformBlock<BlockData> getBlockAt(double v, double v1, double v2) {
		return null;
	}

	@Override
	public PlatformBlock<BlockData> getBlockAt(Vec3 vec3) {
		return new BukkitPlatformBlock(world.getBlockAt(vec3.getX(), vec3.getY(), vec3.getZ()));
	}

	@Override
	public PlatformBlockState<BlockData> AIR() {
		return BukkitBlockState.from(Material.AIR);
	}

	@Override
	public PlatformBlockState<BlockData> TOP_LAYER_BLOCK() {
		return BukkitBlockState.from(Material.GRASS_BLOCK);
	}

	@Override
	public PlatformBlockState<BlockData> STONE_LAYER_BLOCK() {
		return BukkitBlockState.from(Material.STONE);
	}

	@Override
	public void setPlatformBlockState(Vec3 vec3, PlatformBlockState<BlockData> platformBlockState) {
		world.getBlockAt(vec3.getX(), vec3.getY(), vec3.getZ()).setBlockData(platformBlockState.getNative(), false);
	}

	@Override
	public void setPlatformBlockState(Vec3 vec3, PlatformBlockState<BlockData> platformBlockState, ICompoundTag iCompoundTag) {
		world.getBlockAt(vec3.getX(), vec3.getY(), vec3.getZ()).setBlockData(platformBlockState.getNative(), false);
		// Help needed
	}

	@Override
	public PlatformBlockState<BlockData> createPlatformBlockState(String s, String s1) {
		return new BukkitBlockState(Bukkit.createBlockData(s + "[" + s1 + "]"));
	}

	@Override
	public void loadChunkIfNeeded(int i, int i1) {
		world.loadChunk(i, i1);
	}

	@Override
	public boolean isChunkLoaded(int i, int i1) {
		return world.isChunkLoaded(i, i1);
	}

	public World getBukkitWorld() {
		return world;
	}
}
