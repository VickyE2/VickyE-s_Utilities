package org.vicky.forge.forgeplatform.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.forge.forgeplatform.item.renderer.ExtendedDescriptorItemRenderer;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.platform.entity.PlatformLivingEntity;
import org.vicky.platform.item.PlatformItemStack;
import org.vicky.platform.items.AnimationContext;
import org.vicky.platform.items.ItemDescriptor;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

import static org.vicky.forge.forgeplatform.item.InspectManager.isInspecting;

public class ExtendedDescriptorItem extends DescriptorItem implements GeoItem {
    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public ExtendedDescriptorItem(ItemDescriptor descriptor, Properties props) {
        super(descriptor, props);
    }

    @Override
    public boolean isPerspectiveAware() {
        return true;
    }

    @Override
    public void registerControllers(
            AnimatableManager.ControllerRegistrar controllers
    ) {
        if (getDescriptor().getAnimations() == null)
            return;

        getDescriptor()
                .getAnimations()
                .getLayers()
                .forEach(layer -> controllers.add(
                        new ExtendedItemAnimationController<>(
                                this,
                                layer.getName(),
                                5,
                                state -> {

                                    AnimationContext context =
                                            createContext(state.getDelegate());

                                    return state.setAndContinue(
                                            layer.getResolver()
                                                    .invoke(context)
                                    );
                                }
                        )
                ));
    }

    public AnimationContext createContext(
            AnimationState<?> state
    ) {

        Entity holder = state.getData(DataTickets.ENTITY);

        PlatformLivingEntity livingEntity =
                holder instanceof LivingEntity le
                        ? ForgePlatformLivingEntity.from(le)
                        : null;

        PlatformItemStack stack =
                new ForgeItemStack(state.getData(DataTickets.ITEMSTACK));

        return new AnimationContext(
                stack,
                livingEntity,
                ForgeHacks.toVicky(state.getData(DataTickets.ITEM_RENDER_PERSPECTIVE)),
                holder instanceof LivingEntity e && e.isUsingItem() &&
                        e.getUseItem().is(state.getData(DataTickets.ITEMSTACK).getItem()),
                state.isMoving(),
                holder instanceof LivingEntity e && e.isAutoSpinAttack(),
                holder instanceof Player p && isInspecting(p)
        );
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private ExtendedDescriptorItemRenderer renderer = null;
            // Don't instantiate until ready. This prevents race conditions breaking things
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new ExtendedDescriptorItemRenderer();

                return renderer;
            }

            @Override
            public HumanoidModel.@Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                return IClientItemExtensions.super.getArmPose(entityLiving, hand, itemStack);
            }

            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
                return IClientItemExtensions.super.getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
            }
        });
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
