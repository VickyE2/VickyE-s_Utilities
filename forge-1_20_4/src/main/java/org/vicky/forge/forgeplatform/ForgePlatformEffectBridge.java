package org.vicky.forge.forgeplatform;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.platform.entity.PlatformEffectBridge;
import org.vicky.platform.entity.PlatformEffectInstance;
import org.vicky.platform.entity.RegisteredUniversalEffect;
import org.vicky.platform.entity.UniversalEffect;
import org.vicky.platform.utils.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

// make universal effect an interface
public class ForgePlatformEffectBridge implements PlatformEffectBridge<ForgePlatformLivingEntity> {
    public static final ForgePlatformEffectBridge INSTANCE = new ForgePlatformEffectBridge();

    private ForgePlatformEffectBridge() {
        for (var effect : ForgeRegistries.MOB_EFFECTS.getValues()) {
            registerEffect(
                    new UniversalEffect(

                    )
            )
        }
    }

    private final Map<ResourceLocation, RegisteredUniversalEffect> registeredEffects =
            new HashMap<>();

    @Override
    public @NotNull RegisteredUniversalEffect registerEffect(@NotNull UniversalEffect universalEffect) {
        return null;
    }

    @Override
    public @Nullable RegisteredUniversalEffect getEffect(@NotNull ResourceLocation resourceLocation) {
        return registeredEffects.get(resourceLocation);
    }

    @Override
    public void applyEffect(@NotNull ForgePlatformLivingEntity mob, @NotNull UniversalEffect universalEffect, int i, int i1) {
        MobEffect
    }

    @Override
    public void removeEffect(@NotNull ForgePlatformLivingEntity mob, @NotNull UniversalEffect universalEffect) {

    }

    @Override
    public boolean hasEffect(@NotNull ForgePlatformLivingEntity mob, @NotNull UniversalEffect universalEffect) {
        return false;
    }

    @Override
    public @Nullable PlatformEffectInstance getEffectData(@NotNull ForgePlatformLivingEntity mob, @NotNull UniversalEffect universalEffect) {
        return null;
    }
}
