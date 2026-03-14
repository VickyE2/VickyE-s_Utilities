package org.vicky.forge.forgeplatform.useables;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.forge.forgeplatform.adventure.AdventureComponentConverter;
import org.vicky.platform.items.ItemDescriptor;

public class DescriptorItem extends Item {

    private final ItemDescriptor descriptor;

    public DescriptorItem(ItemDescriptor descriptor, Properties props) {
        super(props);
        this.descriptor = descriptor;
    }

    public ItemDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        if (descriptor.getPhysicalProps().getGlint())
            return true;

        return super.isFoil(stack);
    }



    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, @NotNull Player player,
                                                           @NotNull LivingEntity livingEntity, @NotNull InteractionHand hand) {
        descriptor.getHandler().onInteract(
                new ForgePlatformItem(stack),
                ForgeHacks.toVicky(hand),
                ForgePlatformLivingEntity.from(player)
                // need the entity interacting with ForgePlatformLivingEntity.from(livingEntity)
        ); // should return interaction result
        return super.interactLivingEntity(stack, player, livingEntity, hand);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slot, boolean selected) {
        var platformStack = new ForgePlatformItem(stack);
        if (entity instanceof LivingEntity livingEntity) {
            descriptor.getHandler().whenInInventory(
                    platformStack,
                    ForgePlatformLivingEntity.from(livingEntity)
            );
        }
        if (entity instanceof LivingEntity player) {
            boolean inHotbar = slot >= 0 && slot < 9;
            boolean isOffhand = player.getOffhandItem() == stack;
            if (isOffhand) {
                descriptor.getHandler().whileInHand(
                        platformStack,
                        ForgeHacks.toVicky(InteractionHand.OFF_HAND),
                        ForgePlatformLivingEntity.from(player)
                );
            }
            else if (selected) {
                descriptor.getHandler().whileInHand(
                        platformStack,
                        ForgeHacks.toVicky(InteractionHand.MAIN_HAND),
                        ForgePlatformLivingEntity.from(player)
                );
            }
            else if (inHotbar) {
                descriptor.getHandler().whenInHotBar(
                        platformStack,
                        ForgePlatformLivingEntity.from(player)
                );
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        // descriptor.getHandler().onUse()
        return super.use(level, player, hand);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext ctx) {
        // descriptor.getHandler().onUseOn() - should return interactionResult
        // public enum InteractionResult {
        //   SUCCESS,
        //   CONSUME,
        //   CONSUME_PARTIAL,
        //   PASS,
        //   FAIL;
        return super.useOn(ctx);
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();

        if (!descriptor.getLore().isEmpty()) {
            var tag = stack.getOrCreateTag();
            CompoundTag display = tag.getCompound("display");
            if (display.isEmpty()) {
                display = new CompoundTag();
                tag.put("display", display);
            }

            ListTag lore = new ListTag();
            for (var line : descriptor.getLore()) {
                lore.add(StringTag.valueOf(
                        Component.Serializer.toJson(AdventureComponentConverter.toNative(line))
                ));
            }
            display.put("Lore", lore);
        }

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
