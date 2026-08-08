/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.item;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.vicky.platform.tags.SerializedItemData;
import org.vicky.platform.utils.ResourceLocation;

import java.util.List;

public interface PlatformItemStack {
	int count();

	void count(int value);

	void setDamageValue(int i);

	void shrink(int i);

	ResourceLocation key();

	List<Component> lore();

	/**
	 * Returns the stack's native storage representation.
	 * <p>
	 * Older platforms return LegacyItemData. Newer platforms return
	 * ComponentItemData.
	 */
	@NotNull
	ItemData data();

	PlatformItemEditor edit();

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