package org.vicky.forge.forgeplatform.useables;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.vicky.platform.PlatformItem;

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
