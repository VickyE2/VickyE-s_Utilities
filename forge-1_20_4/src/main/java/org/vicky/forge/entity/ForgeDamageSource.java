package org.vicky.forge.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.vicky.platform.entity.AntagonisticDamageSource;

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
}
