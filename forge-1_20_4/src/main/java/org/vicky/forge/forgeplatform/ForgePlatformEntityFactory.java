/* Licensed under Apache-2.0 2025. */
package org.vicky.forge.forgeplatform;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.entity.ForgePlatformEntity;
import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.forge.entity.PlatformBasedLivingEntity;
import org.vicky.forge.forgeplatform.useables.ForgePlatformWorldAdapter;
import org.vicky.forge.forgeplatform.useables.ForgeVec3;
import org.vicky.platform.entity.*;
import org.vicky.platform.entity.distpacher.EntityTaskManager;
import org.vicky.platform.utils.ResourceLocation;
import org.vicky.platform.world.PlatformLocation;
import org.vicky.platform.world.PlatformWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.vicky.VickyUtilitiesForge.MODID;

public class ForgePlatformEntityFactory implements PlatformEntityFactory {
    public static final ForgePlatformEntityFactory INSTANCE = new ForgePlatformEntityFactory();

    private ForgePlatformEntityFactory() {}

    private final Map<ResourceLocation, RegisteredMobEntityEventHandler> HANDLERS = new HashMap<>();
    private final Map<ResourceLocation, RegistryObject<EntityType<? extends LivingEntity>>> REGISTERED_ENTITIES = new HashMap<>();
    private final Map<RegistryObject<EntityType<? extends LivingEntity>>, Supplier<AttributeSupplier>> ATTR_SUPPLIERS = new HashMap<>();

    // DeferredRegister created on mod init
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);

    @Override
    public @NotNull RegisteredMobEntityEventHandler registerHandler(@NotNull ResourceLocation resourceLocation, @NotNull MobEntityEventHandler mobEntityEventHandler) {
        var registeredHandler = new RegisteredMobEntityEventHandler(mobEntityEventHandler);
        HANDLERS.put(resourceLocation, registeredHandler);
        return registeredHandler;
    }

    @Override
    public @Nullable RegisteredMobEntityEventHandler getHandler(@NotNull ResourceLocation resourceLocation) {
        return HANDLERS.get(resourceLocation);
    }

    // make void and remove loc
    @Override
    public void register(@NotNull MobEntityDescriptor descriptor) throws ErrorOnMobProductionException {
        String key = descriptor.getMobDetails().getMobKey().getPath();

        RegistryObject<EntityType<? extends LivingEntity>> registeredObject = ENTITIES.register(key, () ->
                EntityType.Builder.<PathfinderMob>of(
                                (type, level) -> new PlatformBasedLivingEntity(descriptor, type, level),
                                MobCategory.valueOf(descriptor.getMobDetails().getCategory().name())
                        )
                        .sized((float) descriptor.getMobDetails().getBoundingBox().maxX, (float) descriptor.getMobDetails().getBoundingBox().maxZ)
                        .build(key)
        );

        REGISTERED_ENTITIES.put(ResourceLocation.from(MODID, key), registeredObject);
        ATTR_SUPPLIERS.put(registeredObject, () -> AttributeSupplier.builder()
                .add(Attributes.MAX_HEALTH, descriptor.getMobDetails().getMaxHealth())
                .add(Attributes.ATTACK_DAMAGE, descriptor.getMobDetails().getAttackDamage())
                .add(Attributes.MOVEMENT_SPEED, descriptor.getMobDetails().getMovementSpeed())
                .add(Attributes.FOLLOW_RANGE, descriptor.getMobDetails().getFollowRange())
                .add(Attributes.KNOCKBACK_RESISTANCE, descriptor.getMobDetails().getKnockbackResistance())
                .add(Attributes.ARMOR, descriptor.getMobDetails().getBaseArmor())
                .add(Attributes.FLYING_SPEED, descriptor.getMobDetails().getFlySpeed())
                .add(Attributes.ATTACK_KNOCKBACK, descriptor.getMobDetails().getAttackKnockback()) //
                .add(Attributes.ATTACK_SPEED, descriptor.getMobDetails().getAttackSpeed()) //
                .add(Attributes.JUMP_STRENGTH, descriptor.getMobDetails().getJumpStrength()) //
                .add(Attributes.ARMOR_TOUGHNESS, descriptor.getMobDetails().getBaseArmorToughness()) //
                .add(Attributes.MAX_ABSORPTION, descriptor.getMobDetails().getMaxAbsorption()) //
                .add(Attributes.LUCK, descriptor.getMobDetails().getLuck()) //
                .build());
    }

    // Call during FMLCommonSetup or mod constructor to add listener for entity attributes
    public void attachListeners(IEventBus modBus) {
        modBus.addListener(this::onEntityAttributeCreation);
    }

    private void onEntityAttributeCreation(EntityAttributeCreationEvent evt) {
        for (var entry : ATTR_SUPPLIERS.entrySet()) {
            RegistryObject<EntityType<? extends LivingEntity>> type = entry.getKey();
            AttributeSupplier supplier = entry.getValue().get();
            evt.put(type.get(), supplier); // map the entity type to its attributes
        }
    }

    @Override
    public @Nullable PlatformEntity spawnArrowAt(@Nullable PlatformLocation platformLocation) {
        if (!(platformLocation instanceof ForgeVec3 loc)) return null;
        return ForgePlatformEntity.from(new Arrow(loc.getForgeWorld(), loc.x,
                loc.y, loc.z, Items.ARROW.getDefaultInstance()));
    }

    // make nullable
    @Override
    public @Nullable PlatformLivingEntity spawn(@NotNull PlatformWorld<?, ?> world, @NotNull ResourceLocation id, double x, double y, double z) {
        if (!(world instanceof ForgePlatformWorldAdapter level)) return null;
        RegistryObject<EntityType<? extends LivingEntity>> ro = REGISTERED_ENTITIES.get(id);
        if (ro == null) return null;
        EntityType<?> type = ro.get();
        if (level.getNative().isClientSide()) return null;

        Entity entity = type.create(level.getNative());
        if (!(entity instanceof PathfinderMob mob)) return null;

        mob.moveTo(x, y, z, mob.getYRot(), mob.getXRot());
        level.getNative().addFreshEntity(mob);

        // find handler and call onSpawn
        RegisteredMobEntityEventHandler handler = HANDLERS.get(id);
        var platformed = ForgePlatformLivingEntity.from(mob);
        if (handler != null) {
            handler.getHandler().onSpawn(platformed);
        }
        return platformed;
    }
}