/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.useables;

import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.vicky.platform.utils.Property;
import org.vicky.platform.world.PlatformBlockState;
import org.vicky.platform.world.PlatformMaterial;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

public record ForgePlatformBlockStateAdapter(BlockState state) implements PlatformBlockState<BlockState> {

	@Override
	public @NotNull String getId() {
		return ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString();
	}

	@Override
	public @NotNull PlatformMaterial getMaterial() {
		return new ForgePlatformMaterial(state.getBlock());
	}

	@Override
	public @NotNull BlockState getNative() {
		return state;
	}

	@Override
	public String getAsString(boolean var1) {
		return state.toString();
	}

	@Override
	public boolean matches(PlatformBlockState<BlockState> other) {
		return state.equals(other.getNative());
	}

	@Override
	public <T extends Comparable<T>> boolean has(Property<T> property) {
		try {
			var type = ForgeHacks.fromVicky(property);
			if (type == null) return false;
			return state.hasProperty(type);
		}
		catch (Exception e) {
			return false;
		}
	}

	@Override
	public <T extends Comparable<T>> T get(Property<T> property) {
		try {
			var type = ForgeHacks.fromVicky(property);
			if (type == null) return null;
			return state.getValue(type);
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public <T extends Comparable<T>> PlatformBlockState<BlockState> set(Property<T> property, T value) {
		try {
			var type = ForgeHacks.fromVicky(property);
			if (type == null) return null;
			return new ForgePlatformBlockStateAdapter(state.setValue(type, value));
		}
		catch (Exception e) {
			return null;
		}
	}

}
