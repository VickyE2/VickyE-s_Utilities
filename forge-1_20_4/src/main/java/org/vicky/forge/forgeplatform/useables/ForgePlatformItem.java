/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.useables;

import net.kyori.adventure.text.Component;
import net.minecraft.nbt.*;
import org.vicky.forge.forgeplatform.adventure.AdventureComponentConverter;
import org.vicky.platform.PlatformItemStack;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.vicky.platform.utils.ResourceLocation;
import org.vicky.utilities.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ForgePlatformItem(ItemStack item) implements PlatformItemStack {

	@Override
	public Component getName() {
		return AdventureComponentConverter.fromNative(item.getHoverName());
	}

	@Override
	public void setName(Component name) {
		item.setHoverName(AdventureComponentConverter.toNative(name));
	}

	@Override
	public ResourceLocation getDescriptorId() {
		return ResourceLocation.from(item.getDescriptionId());
	}

	@Override
	public void setCount(int count) {
		item.setCount(count);
	}

	@Override
	public int getCount() {
		return item.getCount();
	}

	@Override
	public void applyNbt(Map<String, ?> nbt) {
		CompoundTag tag = item.getOrCreateTag();
		for (Map.Entry<String, ?> entry : nbt.entrySet()) {
			Tag converted = ForgeHacks.toNBT(entry.getValue());
			if (converted != null) {
				tag.put(entry.getKey(), converted);
			}
		}
	}

	@Override
	public void applyNbt(Pair<String, ?> nbt) {
		CompoundTag tag = item.getOrCreateTag();
		Tag converted = ForgeHacks.toNBT(nbt.getValue());
		if (converted != null) {
			tag.put(nbt.getKey(), converted);
		}
	}

	@Override
	public boolean hasNbt(String key) {
		CompoundTag tag = item.getTag();
		return tag != null && tag.contains(key);
	}

	@Override
	public Object getNbt(String key) {
		CompoundTag tag = item.getTag();
		if (tag == null || !tag.contains(key)) return null;
		return ForgeHacks.fromNBT(tag.get(key));
	}

	@Override
	public PlatformItemStack copy() {
		return new ForgePlatformItem(item.copy());
	}

	@Override
	public String getIdentifier() {
		return ForgeRegistries.ITEMS.getKey(item.getItem()).toString();
	}
}
