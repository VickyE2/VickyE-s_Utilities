package org.vicky.forge.forgeplatform.item;

import net.minecraft.world.item.ItemStack;
import org.vicky.platform.item.ItemData;
import org.vicky.platform.tags.ItemDataFormat;
import org.vicky.platform.tags.SerializedItemData;

public record ForgeItemData(ItemStack delegate) implements ItemData {
    @Override
    public ItemDataFormat format() {
        return ItemDataFormat.LEGACY_NBT;
    }

    @Override
    public boolean isEmpty() {
        return delegate.getOrCreateTag().isEmpty();
    }

    @Override
    public ItemData copy() {
        return new ForgeItemData(delegate.copy());
    }

    @Override
    public SerializedItemData serialize() {
        return SerializedItemData.legacy(delegate.getOrCreateTag().getAsString());
    }
}
