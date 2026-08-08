package org.vicky.forge.forgeplatform.item;

import net.kyori.adventure.text.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.vicky.forge.forgeplatform.useables.ForgeCompoundData;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.platform.item.PlatformItemEditor;
import org.vicky.platform.item.PlatformItemStack;
import org.vicky.platform.tags.CompoundData;

import java.util.List;
import java.util.function.Consumer;

public record ForgeItemEditor(ItemStack delegate) implements PlatformItemEditor {

    @Override
    public PlatformItemEditor customName(Component name) {
        delegate.setHoverName(ForgeHacks.fromVicky(name));
        return this;
    }

    @Override
    public PlatformItemEditor customData(Consumer<CompoundData> consume) {
        consume.accept(new ForgeCompoundData(delegate.getOrCreateTag()));
        return this;
    }

    @Override
    public PlatformItemEditor clearCustomName() {
        delegate.resetHoverName();
        return this;
    }

    @Override
    public PlatformItemEditor addLore(List<Component> lines) {
        CompoundTag tag = delegate.getOrCreateTag();
        CompoundTag display = tag.getCompound(ItemStack.TAG_DISPLAY);

        var lore = display.getList(ItemStack.TAG_LORE, Tag.TAG_STRING);
        lines.forEach(line -> lore.add(StringTag.valueOf(net.minecraft.network.chat.Component.Serializer.toJson(ForgeHacks.fromVicky(line)))));

        display.put(ItemStack.TAG_LORE, lore);
        tag.put(ItemStack.TAG_DISPLAY, display);

        return this;
    }

    @Override
    public PlatformItemEditor addLore(Component line) {
        CompoundTag tag = delegate.getOrCreateTag();
        CompoundTag display = tag.getCompound(ItemStack.TAG_DISPLAY);

        var lore = display.getList(ItemStack.TAG_LORE, Tag.TAG_STRING);
        lore.add(StringTag.valueOf(net.minecraft.network.chat.Component.Serializer.toJson(ForgeHacks.fromVicky(line))));

        display.put(ItemStack.TAG_LORE, lore);
        tag.put(ItemStack.TAG_DISPLAY, display);

        return this;
    }

    @Override
    public PlatformItemEditor clearLore() {
        CompoundTag tag = delegate.getOrCreateTag();
        CompoundTag display = tag.getCompound(ItemStack.TAG_DISPLAY);
        display.put(ItemStack.TAG_LORE, new ListTag());
        tag.put(ItemStack.TAG_DISPLAY, display);

        return this;
    }

    @Override
    public PlatformItemEditor damage(int damage) {
        delegate.setDamageValue(damage);
        return this;
    }

    @Override
    public PlatformItemEditor unbreakable(boolean unbreakable) {
        delegate.getOrCreateTag().putBoolean("Unbreakable", unbreakable);
        return this;
    }

    @Override
    public PlatformItemEditor customModelData(int value) {
        return this;
    }

    @Override
    public PlatformItemEditor enchantment(String enchantmentId, int level) {
        // 1.18.2 uses 'new ResourceLocation(string)', NOT 'ResourceLocation.parse(string)'
        var enchantment = ForgeRegistries.ENCHANTMENTS.getValue(ResourceLocation.parse(enchantmentId));
        if (enchantment != null) {
            delegate.enchant(enchantment, level);
        }
        return this;
    }

    @Override
    public PlatformItemEditor removeEnchantment(String enchantmentId) {
        // Enchantments on normal items are stored inside a ListTag under the "Enchantments" key
        if (delegate.hasTag() && delegate.getTag().contains("Enchantments", 9)) {
            ListTag enchantmentsList = delegate.getTag().getList("Enchantments", 10); // 10 = TAG_COMPOUND

            // Iterate backward through the list to safely remove elements by ID matching
            for (int i = enchantmentsList.size() - 1; i >= 0; i--) {
                CompoundTag enchantEntry = enchantmentsList.getCompound(i);
                if (enchantEntry.getString("id").equals(enchantmentId)) {
                    enchantmentsList.remove(i);
                }
            }

            // Clean up the tag if no enchantments are left
            if (enchantmentsList.isEmpty()) {
                delegate.getTag().remove("Enchantments");
            }
        }
        return this;
    }

    @Override
    public PlatformItemEditor customString(String key, String value) {
        delegate.getOrCreateTag().putString(key, value);
        return this;
    }

    @Override
    public PlatformItemEditor customInt(String key, int value) {
        delegate.getOrCreateTag().putInt(key, value);
        return this;
    }

    @Override
    public PlatformItemStack apply() {
        return new ForgeItemStack(delegate);
    }
}
