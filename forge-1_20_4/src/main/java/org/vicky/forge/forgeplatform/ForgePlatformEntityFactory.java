/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.entity.PlatformBasedLivingEntity;
import org.vicky.forge.forgeplatform.useables.ForgeVec3;
import org.vicky.platform.entity.*;
import org.vicky.platform.utils.ResourceLocation;
import org.vicky.platform.utils.Vec3;
import org.vicky.platform.world.PlatformLocation;

import java.util.HashMap;
import java.util.Map;

import static org.vicky.forge.entity.EntityTypes.ENTITIES;
import static org.vicky.forge.forgeplatform.useables.ForgeHacks.toVicky;

public class ForgePlatformEntityFactory implements PlatformEntityFactory {
    public static final ForgePlatformEntityFactory INSTANCE = new ForgePlatformEntityFactory();

    private ForgePlatformEntityFactory() {}

    private final Map<ResourceLocation, RegisteredMobEntityEventHandler> HANDLERS = new HashMap<>();
    private final Map<ResourceLocation, RegistryObject<EntityType<?>>> REISTERED_ENTITIES = new HashMap<>();

    // Make RMEEH internal instead of protected
    @Override
    public @NotNull RegisteredMobEntityEventHandler registerHandler(@NotNull ResourceLocation resourceLocation, @NotNull MobEntityEventHandler mobEntityEventHandler) {
        var registeredHandler = new RegisteredMobEntityEventHandler(
                mobEntityEventHandler
        );
        HANDLERS.put(resourceLocation, registeredHandler);
        return registeredHandler;
    }

    @Override
    public @Nullable RegisteredMobEntityEventHandler getHandler(@NotNull ResourceLocation resourceLocation) {
        return HANDLERS.get(resourceLocation);
    }

    // Make void
    @Override
    public void register(@NotNull MobEntityDescriptor mobEntityDescriptor, @NotNull PlatformLocation platformLocation) throws ErrorOnMobProductionException {
        RegistryObject<EntityType<?>> registeredObject = ENTITIES.register(
                mobEntityDescriptor.getMobDetails().getMobKey(),
                () -> EntityType.Builder.<Mob>of(
                        (type, level) ->
                                new PlatformBasedLivingEntity(mobEntityDescriptor, type, level),
                        MobCategory.valueOf(mobEntityDescriptor.getMobDetails().getCategory())
                )
                        .sized(mobEntityDescriptor.getPhysicalProps().size)
                        .
                        .build(mobEntityDescriptor.getMobDetails().getMobKey())
        );
        REISTERED_ENTITIES.put(toVicky(registeredObject.getId()), registeredObject);
    }

    @Override
    public @Nullable PlatformEntity spawnArrowAt(@Nullable PlatformLocation platformLocation) {
        return null;
    }
}
