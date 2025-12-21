package org.vicky.forge.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import static org.vicky.VickyUtilitiesForge.MODID;
import static org.vicky.forge.forgeplatform.useables.ForgeHacks.fromVicky;

/**
 * Client-only renderer for PlatformBasedLivingEntity.
 * Uses the modelId from the entity descriptor to locate geo/texture/animation files.
 */
public class PlatformBasedLivingEntityRenderer extends GeoEntityRenderer<PlatformBasedLivingEntity> {
    public PlatformBasedLivingEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ResourceLocation.fromNamespaceAndPath(MODID, "")) {
            @Override
            public ResourceLocation getModelResource(PlatformBasedLivingEntity object) {
                org.vicky.platform.utils.ResourceLocation model =
                        object.getDescriptor().getMobDetails().getModelId();
                return fromVicky(model);
            }

            @Override
            public ResourceLocation getTextureResource(PlatformBasedLivingEntity object) {
                org.vicky.platform.utils.ResourceLocation explicit =
                        object.getDescriptor().getMobDetails().getTexture();
                return fromVicky(explicit);
            }

            @Override
            public ResourceLocation getAnimationResource(PlatformBasedLivingEntity object) {
                org.vicky.platform.utils.ResourceLocation explicitAnim =
                        object.getDescriptor().getMobDetails().getAnimationsFile();
                return fromVicky(explicitAnim);
            }
        });

        this.shadowRadius = 0.5f;
    }
}

