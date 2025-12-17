package org.vicky.forge.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.vicky.platform.entity.AntagonisticDamageSource;
import org.vicky.platform.entity.DamageType;

import static org.vicky.forge.forgeplatform.useables.ForgeHacks.toVicky;

public class ForgeDamageSource extends DamageSource {
    private ForgeDamageSource(LivingEntity entity, AntagonisticDamageSource src) {
        super(
                entity.level()
                        .registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolder(ResourceKey.create(Registries.DAMAGE_TYPE,
                                ResourceLocation.parse(src.getDamageType().name().toLowerCase()))).get(),
                ((ForgePlatformLivingEntity) src.getDirectEntity()).ordinal,
                ((ForgePlatformLivingEntity) src.getCausingEntity()).ordinal,
                new Vec3(src.getSourceLocation().x, src.getSourceLocation().y, src.getSourceLocation().z)
        );
    }

    public static ForgeDamageSource from(LivingEntity e, AntagonisticDamageSource src) {
        return new ForgeDamageSource(e, src);
    }

    public static AntagonisticDamageSource from(DamageSource src) {
        return new AntagonisticDamageSource(
                ForgePlatformEntity.from(src.getEntity()),
                ForgePlatformEntity.from(src.getDirectEntity()),
                toVicky(src.getSourcePosition() != null ? src.getSourcePosition() : Vec3.ZERO, src.getEntity() != null ? src.getEntity().level() : null),
                toVicky(src.sourcePositionRaw() != null ? src.sourcePositionRaw() : Vec3.ZERO, src.getEntity() != null ? src.getEntity().level() : null),
                src.isIndirect(),
                src.scalesWithDifficulty(),
                DamageType.valueOf(src.typeHolder().unwrap().orThrow().location().getPath().toUpperCase())
        );
    }
}
