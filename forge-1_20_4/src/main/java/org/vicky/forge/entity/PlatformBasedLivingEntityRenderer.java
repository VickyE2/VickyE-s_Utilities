/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity;

import static org.vicky.forge.forgeplatform.useables.ForgeHacks.fromVicky;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

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
	}
}
