package org.vicky.forge.forgeplatform.useables;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.forge.forgeplatform.adventure.AdventureComponentConverter;
import org.vicky.platform.items.ItemDescriptor;

import java.util.List;

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
        if (player instanceof ServerPlayer sp)
            return ForgeHacks.fromVicky(descriptor.getHandler().onInteractLiving(
                    new ForgePlatformItem(stack),
                    ForgeHacks.toVicky(hand),
                    ForgePlatformPlayer.adapt(sp),
                    ForgePlatformLivingEntity.from(livingEntity)
            ));

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
            else if (inHotbar && player instanceof ServerPlayer sp) {
                descriptor.getHandler().whenInHotBar(
                        platformStack,
                        ForgePlatformPlayer.adapt(sp)
                );
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer sp) {
            var result = descriptor.getHandler().onUse(
                    new ForgePlatformItem(stack),
                    ForgeHacks.toVicky(hand),
                    ForgePlatformPlayer.adapt(sp)
            );
            return switch (result) {
                case SUCCESS -> InteractionResultHolder.success(stack);
                case CONSUME -> InteractionResultHolder.consume(stack);
                case PASS -> InteractionResultHolder.pass(stack);
                case FAIL -> InteractionResultHolder.fail(stack);
                case CONSUME_PARTIAL -> InteractionResultHolder.sidedSuccess(stack, true);
            };
        }
        return super.use(level, player, hand);
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext ctx) {
        if (ctx.getPlayer() instanceof ServerPlayer sp)
            return ForgeHacks.fromVicky(descriptor.getHandler().onUseOn(
                    new ForgePlatformItem(ctx.getItemInHand()),
                    ForgeHacks.toVicky(ctx.getHand()),
                    ForgePlatformLivingEntity.from(sp),
                    new ForgePlatformBlockAdapter(
                            ctx.getClickedPos(),
                            ctx.getLevel().getBlockState(ctx.getClickedPos()),
                            ctx.getLevel()
                    )
            ));
        return super.useOn(ctx);
    }

    @Override
    public void appendHoverText(
            @NotNull ItemStack stack,
            Level level,
            @NotNull List<Component> tooltip,
            @NotNull TooltipFlag flag
    ) {
        for (var line : descriptor.getLore()) {
            tooltip.add(AdventureComponentConverter.toNative(line));
        }
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        return AdventureComponentConverter.toNative(descriptor.getDisplayName());
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();

        if (!descriptor.getBaseNbt().isEmpty()) {
            CompoundTag tag = stack.getOrCreateTag();

            descriptor.getBaseNbt().forEach((key, value) -> {
                tag.put(key, ForgeHacks.toNBT(value));
            });
        }

        return stack;
    }
}
