/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.useables;

import org.vicky.platform.PlatformItem;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public record ForgePlatformItem(ItemStack item) implements PlatformItem {
	@Override
	public String getName() {
		return item.getHoverName().getString();
	}

	@Override
	public String getIdentifier() {
		return ForgeRegistries.ITEMS.getKey(item.getItem()).toString();
	}
}
