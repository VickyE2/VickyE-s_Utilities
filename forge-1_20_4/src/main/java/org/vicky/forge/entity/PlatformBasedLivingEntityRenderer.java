/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity;

import static org.vicky.VickyUtilitiesForge.MODID;
import static org.vicky.forge.forgeplatform.useables.ForgeHacks.fromVicky;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.vicky.platform.entity.DefaultEntities;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
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
				return ResourceLocation.fromNamespaceAndPath(model.getNamespace(), "geo/" + model.getPath());
			}

			@Override
			public ResourceLocation getTextureResource(PlatformBasedLivingEntity object) {
				org.vicky.platform.utils.ResourceLocation explicit = object.getDescriptor().getMobDetails()
						.getTexture();
				return fromVicky(explicit);
			}

			@Override
			public ResourceLocation getAnimationResource(PlatformBasedLivingEntity object) {
				org.vicky.platform.utils.ResourceLocation explicitAnim = object.getDescriptor().getMobDetails()
						.getAnimationsFile();
				return fromVicky(explicitAnim);
			}
		});

		this.shadowRadius = 0.5f;
	}
}
