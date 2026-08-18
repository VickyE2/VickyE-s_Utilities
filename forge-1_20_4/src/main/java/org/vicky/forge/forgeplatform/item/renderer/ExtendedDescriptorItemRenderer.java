package org.vicky.forge.forgeplatform.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
import org.joml.Matrix4f;
import org.vicky.forge.forgeplatform.item.ExtendedDescriptorItem;
import org.vicky.platform.items.ItemModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.util.RenderUtils;

public class ExtendedDescriptorItemRenderer extends GeoItemRenderer<ExtendedDescriptorItem> {
    public ExtendedDescriptorItemRenderer() {
        super(new GeoModel<>() {
            @Override
            public ResourceLocation getModelResource(ExtendedDescriptorItem object) {
                if (object.getDescriptor().getModel() instanceof ItemModel.CustomItemModel customModel) {
                    return ResourceLocation.fromNamespaceAndPath(customModel.getModelId().getNamespace(), "geo/item/" + customModel.getModelId().getPath() + ".geo.json");
                }
                throw new RuntimeException("Attempted to use the ExtendedDescriptorItemRenderer for an Item without the use of ItemModel.CustomItemModel: " + PlainTextComponentSerializer.plainText().serialize(object.getDescriptor().getDisplayName()));
            }

            @Override
            public ResourceLocation getTextureResource(ExtendedDescriptorItem object) {
                if (object.getDescriptor().getModel() instanceof ItemModel.CustomItemModel customModel) {
                    return ResourceLocation.fromNamespaceAndPath(customModel.getModelId().getNamespace(), "textures/item/" + customModel.getModelId().getPath() + ".png");
                }
                throw new RuntimeException("Attempted to use the ExtendedDescriptorItemRenderer for an Item without the use of ItemModel.CustomItemModel: " + PlainTextComponentSerializer.plainText().serialize(object.getDescriptor().getDisplayName()));
            }

            @Override
            public ResourceLocation getAnimationResource(ExtendedDescriptorItem object) {
                if (object.getDescriptor().getModel() instanceof ItemModel.CustomItemModel customModel) {
                    return ResourceLocation.fromNamespaceAndPath(customModel.getModelId().getNamespace(), "animations/" + customModel.getModelId().getPath() + ".animation.json");
                }
                throw new RuntimeException("Attempted to use the ExtendedDescriptorItemRenderer for an Item without the use of ItemModel.CustomItemModel: " + PlainTextComponentSerializer.plainText().serialize(object.getDescriptor().getDisplayName()));
            }
        });

        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
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


        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(stack.last().pose());
            bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.modelRenderTranslations));
            bone.setLocalSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, this.itemRenderTranslations));
        }

        PlayerRenderer renderer =
                (PlayerRenderer) mc.getEntityRenderDispatcher()
                        .getRenderer(mc.player);

        PlayerModel<AbstractClientPlayer> model =
                renderer.getModel();

        ResourceLocation skin = mc.player.getSkin().texture();

        VertexConsumer armBuffer =
                currentBuffer.getBuffer(RenderType.entitySolid(skin));

        VertexConsumer sleeveBuffer =
                currentBuffer.getBuffer(RenderType.entityTranslucent(skin));

        // Only use the GeoBone transform.
        // RenderUtils.transformToBone(stack, bone);

        stack.pushPose();
        RenderUtils.prepMatrixForBone(stack, bone);

        // DO NOT call setupModelFromBone() for this test.
        ModelPart arm = left ? model.leftArm : model.rightArm;
        ModelPart sleeve = left ? model.leftSleeve : model.rightSleeve;

        renderModelPartAtCurrentPose(
                arm,
                stack,
                armBuffer,
                packedLight,
                packedOverlay
        );

        renderModelPartAtCurrentPose(
                sleeve,
                stack,
                sleeveBuffer,
                packedLight,
                packedOverlay
        );

        stack.popPose();
    }

    private void renderModelPartAtCurrentPose(
            ModelPart part,
            PoseStack stack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay
    ) {
        float x = part.x;
        float y = part.y;
        float z = part.z;

        float xRot = part.xRot;
        float yRot = part.yRot;
        float zRot = part.zRot;

        float xScale = part.xScale;
        float yScale = part.yScale;
        float zScale = part.zScale;

        try {
            // Prevent ModelPart.render() from modifying our GeckoLib transform.
            part.x = 0.0F;
            part.y = 0.0F;
            part.z = 0.0F;

            part.xRot = 0.0F;
            part.yRot = 0.0F;
            part.zRot = 0.0F;

            part.xScale = 1.0F;
            part.yScale = 1.0F;
            part.zScale = 1.0F;

            part.render(
                    stack,
                    buffer,
                    packedLight,
                    packedOverlay,
                    1, 1, 1, 1
            );
        }
        finally {
            part.x = x;
            part.y = y;
            part.z = z;

            part.xRot = xRot;
            part.yRot = yRot;
            part.zRot = zRot;

            part.xScale = xScale;
            part.yScale = yScale;
            part.zScale = zScale;
        }
    }
}
