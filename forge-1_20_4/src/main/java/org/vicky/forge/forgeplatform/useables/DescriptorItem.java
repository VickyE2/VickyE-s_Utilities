package org.vicky.forge.forgeplatform.useables;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.vicky.platform.items.ItemDescriptor;

public class DescriptorItem extends Item {

    private final ItemDescriptor descriptor;

    public DescriptorItem(ItemDescriptor descriptor, Properties props) {
        super(props);
        this.descriptor = descriptor;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        if (descriptor.getPhysicalProps().getGlint())
            return true;

        return super.isFoil(stack);
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();

        if (!descriptor.getBaseNbt().isEmpty()) {
            CompoundTag tag = stack.getOrCreateTag();

            descriptor.getBaseNbt().forEach((key, value) -> {
                if (value instanceof String s)
                    tag.putString(key, s);
                else if (value instanceof Integer i)
                    tag.putInt(key, i);
                else if (value instanceof Double i)
                    tag.putDouble(key, i);
                else if (value instanceof Byte i)
                    tag.putByte(key, i);
                else if (value instanceof Long i)
                    tag.putLong(key, i);
                else if (value instanceof Float i)
                    tag.putFloat(key, i);
                else if (value instanceof Boolean b)
                    tag.putBoolean(key, b);
            });
        }

        return stack;
    }
}
