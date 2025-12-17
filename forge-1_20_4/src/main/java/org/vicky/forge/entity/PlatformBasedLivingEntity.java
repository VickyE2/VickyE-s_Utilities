package org.vicky.forge.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.vicky.platform.entity.MobEntityDescriptor;

public class PlatformBasedLivingEntity extends Mob {
    public PlatformBasedLivingEntity(MobEntityDescriptor descriptor, EntityType<? extends Entity> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
    }


}
