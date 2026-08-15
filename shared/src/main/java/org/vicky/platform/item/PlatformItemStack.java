/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.item;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.platform.entity.PlatformAnimationController;
import org.vicky.platform.items.PlatformItemInspection;
import org.vicky.platform.tags.SerializedItemData;
import org.vicky.platform.utils.ResourceLocation;

import net.kyori.adventure.text.Component;

public interface PlatformItemStack {
	int count();

	void count(int value);

	void setDamageValue(int i);

	void shrink(int i);

	@NotNull
	ResourceLocation key();

	@NotNull
	List<Component> lore();

	/**
	 * Returns the stack's native storage representation.
	 * <p>
	 * Older platforms return LegacyItemData. Newer platforms return
	 * ComponentItemData.
	 */
	@NotNull
	ItemData data();

	@NotNull
	PlatformItemEditor edit();

	@Nullable
	PlatformAnimationController controller();

	@Nullable
	PlatformItemInspection inspection();
	boolean isInspectable();

	/**
	 * Serializes the complete native item data.
	 */
	default SerializedItemData serializeData() {
		return data().serialize();
	}

	void setTooltipName(Component name);

	boolean isEmpty();

	static PlatformItemStack cast(Object item) {
		if (item instanceof PlatformItemStack platformItem) {
			return platformItem;
		}
		// If it's a Minecraft native ItemStack, wrap or convert it here:
		// return new MyPlatformItemStackWrapper((net.minecraft.world.item.ItemStack)
		// item);
		throw new IllegalArgumentException("Cannot cast " + item + " to PlatformItemStack");
	}
}