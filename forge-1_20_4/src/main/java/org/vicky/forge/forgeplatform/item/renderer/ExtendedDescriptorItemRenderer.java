package org.vicky.forge.forgeplatform.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.vicky.forge.forgeplatform.item.ExtendedDescriptorItem;
import org.vicky.platform.items.ItemModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.RenderUtils;

public class ExtendedDescriptorItemRenderer extends GeoItemRenderer<ExtendedDescriptorItem> {
    public ExtendedDescriptorItemRenderer() {
        super(new GeoModel<>() {
            @Override
            public ResourceLocation getModelResource(ExtendedDescriptorItem object) {
                if (object.getDescriptor().getModel() instanceof ItemModel.CustomItemModel customModel) {
                    return ResourceLocation.fromNamespaceAndPath(customModel.getModelId().getNamespace(), "geo/item/" + customModel.getModelId().getPath() + ".geo.json");
                }
                return null;
            }

            @Override
            public ResourceLocation getTextureResource(ExtendedDescriptorItem object) {
                if (object.getDescriptor().getModel() instanceof ItemModel.CustomItemModel customModel) {
                    return ResourceLocation.fromNamespaceAndPath(customModel.getModelId().getNamespace(), "textures/item/" + customModel.getModelId().getPath() + ".png");
                }
                return null;
            }

            @Override
            public ResourceLocation getAnimationResource(ExtendedDescriptorItem object) {
                if (object.getDescriptor().getModel() instanceof ItemModel.CustomItemModel customModel) {
                    return ResourceLocation.fromNamespaceAndPath(customModel.getModelId().getNamespace(), "animations/" + customModel.getModelId().getPath() + ".animation.json");
                }
                return null;
            }
        });

        // this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    public ItemDisplayContext transformType;


    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int p_239207_6_) {
        this.transformType = transformType;
        super.renderByItem(stack, transformType, matrixStack, bufferIn, combinedLightIn, p_239207_6_);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, ExtendedDescriptorItem item, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (this.transformType != null &&
                !this.transformType.firstPerson() &&
                item.getDescriptor().getModel() instanceof ItemModel.CustomItemModel customModel &&
                customModel.getHandGroups() != null) {
            String left = customModel.getHandGroups().getFirst();
            String right = customModel.getHandGroups().getSecond();

            if (bone.getName().equals(left) || bone.getName().equals(right)) {
                bone.setHidden(true);
                return;
            }
        }

        if (this.transformType != null &&
                this.transformType.firstPerson() &&
                item.getDescriptor().getModel() instanceof ItemModel.CustomItemModel customModel &&
                customModel.getHandGroups() != null) {

            String left = customModel.getHandGroups().getFirst();
            String right = customModel.getHandGroups().getSecond();

            if (bone.getName().equals(left) || bone.getName().equals(right)) {

                // Don't let GeckoLib render the dummy arm geometry.
                bone.setHidden(true);

                renderPlayerArm(
                        poseStack,
                        bone,
                        bufferSource,
                        bone.getName().equals(left),
                        packedLight,
                        packedOverlay
                );

                bufferSource.getBuffer(renderType);

                return;
            }
        }
        super.renderRecursively(poseStack, item, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private void renderPlayerArm(
            PoseStack stack,
            GeoBone bone,
            MultiBufferSource currentBuffer,
            boolean left,
            int packedLight,
            int packedOverlay
    ) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null)
            return;

        PlayerRenderer renderer =
                (PlayerRenderer) mc.getEntityRenderDispatcher()
                        .getRenderer(mc.player);

        PlayerModel<AbstractClientPlayer> model =
                renderer.getModel();

        ResourceLocation skin = mc.player.getSkin().texture();

        stack.pushPose();

        VertexConsumer armBuffer =
                currentBuffer.getBuffer(RenderType.entitySolid(skin));

        VertexConsumer sleeveBuffer =
                currentBuffer.getBuffer(RenderType.entityTranslucent(skin));

        // Only use the GeoBone transform.
        RenderUtils.prepMatrixForBone(stack, bone);

        // DO NOT call setupModelFromBone() for this test.
        ModelPart arm = left ? model.leftArm : model.rightArm;
        ModelPart sleeve = left ? model.leftSleeve : model.rightSleeve;

        // arm.setPos(0, 0, 0);
        // sleeve.setPos(0, 0, 0);

        arm.render(
                stack,
                armBuffer,
                packedLight,
                packedOverlay,
                1, 1, 1, 1
        );

        sleeve.render(
                stack,
                sleeveBuffer,
                packedLight,
                packedOverlay,
                1, 1, 1, 1
        );

        stack.popPose();
    }
}
