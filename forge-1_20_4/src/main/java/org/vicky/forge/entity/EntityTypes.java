package org.vicky.forge.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.DeferredRegister;
import org.vicky.VickyUtilitiesForge;

public class EntityTypes {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, VickyUtilitiesForge.MODID);
}
