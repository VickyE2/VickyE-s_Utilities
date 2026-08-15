/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import java.util.Objects;

/**
 * Client-only renderer for PlatformBasedLivingEntity. Uses the modelId from the
 * entity descriptor to locate geo/texture/animation files.
 */
public class PlatformBasedLivingEntityRenderer extends GeoEntityRenderer<PlatformBasedLivingEntity> {
	public PlatformBasedLivingEntityRenderer(EntityRendererProvider.Context context) {
		super(context, new GeoModel<>() {
			@Override
			public ResourceLocation getModelResource(PlatformBasedLivingEntity object) {
				org.vicky.platform.utils.ResourceLocation model = object.getDescriptor().getMobDetails().getModelId();
				return ResourceLocation.fromNamespaceAndPath(model.getNamespace(), "geo/models/" + model.getPath() + ".geo.json");
			}

			@Override
			public ResourceLocation getTextureResource(PlatformBasedLivingEntity object) {
				org.vicky.platform.utils.ResourceLocation explicit = object.getDescriptor().getMobDetails()
						.getTexture();
				return ResourceLocation.fromNamespaceAndPath(explicit.getNamespace(), "textures/entity/" + explicit.getPath() + ".png");
			}

			@Override
			public ResourceLocation getAnimationResource(PlatformBasedLivingEntity object) {
				org.vicky.platform.utils.ResourceLocation explicitAnim = object.getDescriptor().getMobDetails()
						.getAnimationsFile();
				return ResourceLocation.fromNamespaceAndPath(explicitAnim.getNamespace(), "animations/" + explicitAnim.getPath() + ".animation.json");
			}
		});

		if (getGeoModel().getBone("shadow").isPresent()) {
			this.shadowRadius = (float) getGeoModel().getBone("shadow").get().getCubes().get(0).size().x;
		}
		this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
	}


	public void renderRecursively(PoseStack stack, PlatformBasedLivingEntity animatable, GeoBone bone, RenderType type, MultiBufferSource buffer, VertexConsumer bufferIn, boolean isReRender, float partialTick, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {

		var headTracker = animatable
				.getDescriptor()
				.getMobDetails()
				.getAnimations()
				.getHeadTracking();

		if (headTracker != null
				&& Objects.equals(bone.getName(), headTracker.getBone())) {

			float yaw = Mth.lerp(
					partialTick,
					animatable.yHeadRotO,
					animatable.yHeadRot
			);

			float pitch = Mth.lerp(
					partialTick,
					animatable.xRotO,
					animatable.getXRot()
			);

			bone.setRotY(yaw * Mth.DEG_TO_RAD);
			bone.setRotX(-pitch * Mth.DEG_TO_RAD);
		}

		super.renderRecursively(
				stack,
				animatable,
				bone,
				type,
				buffer,
				bufferIn,
				isReRender,
				partialTick,
				packedLightIn,
				packedOverlayIn,
				red,
				green,
				blue,
				alpha
		);
	}
}
