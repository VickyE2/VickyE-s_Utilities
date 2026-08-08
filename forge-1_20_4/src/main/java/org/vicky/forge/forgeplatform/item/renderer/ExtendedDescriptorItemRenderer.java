package org.vicky.forge.forgeplatform.item.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.vicky.forge.forgeplatform.item.ExtendedDescriptorItem;
import org.vicky.platform.items.ItemModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
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
                    return ResourceLocation.fromNamespaceAndPath(customModel.getModelId().getNamespace(), "geo/item/" + customModel.getModelId() + ".geo.json");
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

        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    private static final float SCALE_RECIPROCAL = 1.0f / 16.0f;
    protected MultiBufferSource currentBuffer;
    protected RenderType renderType;
    public ItemDisplayContext transformType;


    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int p_239207_6_) {
        this.transformType = transformType;
        super.renderByItem(stack, transformType, matrixStack, bufferIn, combinedLightIn, p_239207_6_);
    }

    @Override
    public void actuallyRender(PoseStack matrixStackIn, ExtendedDescriptorItem animatable, BakedGeoModel model, RenderType type, MultiBufferSource renderTypeBuffer, VertexConsumer vertexBuilder, boolean isRenderer, float partialTicks, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        this.currentBuffer = renderTypeBuffer;
        this.renderType = type;
        this.animatable = animatable;
        super.actuallyRender(matrixStackIn, animatable, model, type, renderTypeBuffer, vertexBuilder, isRenderer, partialTicks, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    @Override
    public void renderRecursively(PoseStack stack, ExtendedDescriptorItem animatable, GeoBone bone, RenderType type, MultiBufferSource buffer, VertexConsumer bufferIn, boolean isReRender, float partialTick, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        Minecraft mc = Minecraft.getInstance();
        String name = bone.getName();
        boolean renderingArms = false;
        String leftHandName = null;
        String rightHandName = null;

        if (animatable.getDescriptor().getModel() instanceof ItemModel.CustomItemModel customModel) {
            if (customModel.getHandGroups() != null) {
                leftHandName = customModel.getHandGroups().getFirst();
                rightHandName = customModel.getHandGroups().getSecond();
            }
        }

        if ((leftHandName != null && rightHandName != null) && (name.equals(leftHandName) || name.equals(rightHandName))) {
            bone.setHidden(true);
            renderingArms = true;
        }
        if (this.transformType.firstPerson() && renderingArms) {
            AbstractClientPlayer player = mc.player;
            float armsAlpha = player.isInvisible() ? 0.15f : 1.0f;
            PlayerRenderer playerRenderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
            PlayerModel<AbstractClientPlayer> model = playerRenderer.getModel();
            stack.pushPose();
            RenderUtils.translateMatrixToBone(stack, bone);
            RenderUtils.translateToPivotPoint(stack, bone);
            RenderUtils.rotateMatrixAroundBone(stack, bone);
            RenderUtils.scaleMatrixForBone(stack, bone);
            RenderUtils.translateAwayFromPivotPoint(stack, bone);
            ResourceLocation loc = player.getSkin().texture();
            VertexConsumer armBuilder = this.currentBuffer.getBuffer(RenderType.entitySolid(loc));
            VertexConsumer sleeveBuilder = this.currentBuffer.getBuffer(RenderType.entityTranslucent(loc));
            if (name.equals(leftHandName)) {
                stack.translate(-1.0f * SCALE_RECIPROCAL, 2.0f * SCALE_RECIPROCAL, 0.0f);
                AnimUtils.renderPartOverBone(model.leftArm, bone, stack, armBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
                AnimUtils.renderPartOverBone(model.leftSleeve, bone, stack, sleeveBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
            } else {
                stack.translate(SCALE_RECIPROCAL, 2.0f * SCALE_RECIPROCAL, 0.0f);
                AnimUtils.renderPartOverBone(model.rightArm, bone, stack, armBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
                AnimUtils.renderPartOverBone(model.rightSleeve, bone, stack, sleeveBuilder, packedLightIn, OverlayTexture.NO_OVERLAY, armsAlpha);
            }
            this.currentBuffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(this.animatable)));
            stack.popPose();
        }
        super.renderRecursively(stack, animatable, bone, type, buffer, bufferIn, isReRender, partialTick, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }
}
