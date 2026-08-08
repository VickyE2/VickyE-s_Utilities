/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.item;

import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.platform.entity.PlatformAnimationController;
import org.vicky.platform.item.ItemData;
import org.vicky.platform.item.PlatformItemEditor;
import org.vicky.platform.item.PlatformItemStack;
import org.vicky.platform.items.PlatformItemInspection;
import org.vicky.platform.utils.ComponentUtil;
import org.vicky.platform.utils.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record ForgeItemStack(ItemStack delegate) implements PlatformItemStack {

	public static final ForgeItemStack EMPTY = new ForgeItemStack(ItemStack.EMPTY);

	@Override
	public int count() {
		return delegate.getCount();
	}

	@Override
	public void count(int i) {
		delegate.setCount(i);
	}

	@Override
	public void setDamageValue(int i) {
		delegate.setDamageValue(i);
	}

	@Override
	public void shrink(int i) {
		delegate.shrink(i);
	}

	@Override
	public ResourceLocation key() {
		return ResourceLocation.from(ForgeRegistries.ITEMS.getKey(delegate.getItem()).toString());
	}


	@Override
	public List<Component> lore() {
		CompoundTag tag = delegate.getOrCreateTag();
		CompoundTag display = tag.getCompound("display");

		if (display.getList("Lore", Tag.TAG_STRING).isEmpty()) return new ArrayList<>();

		return new ArrayList<>(display.getList("Lore", Tag.TAG_STRING).stream()
				.map(it -> (StringTag) it)
				.map(StringTag::getAsString)
				.map(ComponentUtil::createStr).toList());
	}

	@Override
	public @NotNull ItemData data() {
		return new ForgeItemData(delegate);
	}

	@Override
	public void setTooltipName(Component name) {
		delegate.setHoverName(ForgeHacks.fromVicky(name));
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public PlatformItemEditor edit() {
		return new ForgeItemEditor(delegate);
	}

	@Override
	public @Nullable PlatformAnimationController controller() {
		return null;
	}

	@Override
	public @Nullable PlatformItemInspection inspection() {
		return null;
	}

	@Override
	public boolean isInspectable() {
		return false;
	}
}
