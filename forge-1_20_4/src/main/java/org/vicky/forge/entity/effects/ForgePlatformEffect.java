/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.effects;

import static org.vicky.forge.forgeplatform.useables.ForgeHacks.toVicky;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.platform.entity.*;
import org.vicky.platform.utils.ResourceLocation;

import net.minecraft.world.effect.MobEffect;

public class ForgePlatformEffect implements PlatformEffect {

	public final MobEffect ordinal;

	private ForgePlatformEffect(MobEffect e) {
		this.ordinal = e;
	}
	public static ForgePlatformEffect from(MobEffect e) {
		return new ForgePlatformEffect(e);
	}

	@Override
	public @NotNull ResourceLocation getKey() {
		return toVicky(ordinal.builtInRegistryHolder().key().location());
	}

	@Override
	public @NotNull String getDisplayName() {
		return ordinal.getDisplayName().getString();
	}

	@Override
	public int getColor() {
		return ordinal.getColor();
	}

	@Override
	public int getMaxAmplifier() {
		if (ordinal instanceof PlatformInstanceMobEffect eff) {
			return eff.getDesc().getMaxAmplifier();
		} else {
			return 4;
		}
	}

	@Override
	public boolean isInstant() {
		return ordinal.isInstantenous();
	}

	@Override
	public int getDefaultDuration() {
		if (ordinal instanceof PlatformInstanceMobEffect eff) {
			return eff.getDesc().getDefaultDuration();
		} else {
			return 255;
		}
	}

	public @NotNull Consumer<EffectTickContext> onTick() {
		return (effectTickContext -> {
			if (effectTickContext.getEntity() instanceof ForgePlatformLivingEntity e) {
				ordinal.applyEffectTick(e.ordinal, effectTickContext.getAmplifier());
			}
		});
	}

	@Override
	public @NotNull Consumer<EffectApplyContext> onEffectStarted() {
		return (effectTickContext -> {
			if (effectTickContext.getEntity() instanceof ForgePlatformLivingEntity e) {
				ordinal.onEffectStarted(e.ordinal, effectTickContext.getAmplifier());
			}
		});
	}

	@Override
	public @NotNull Consumer<EffectRemoveContext> onRemove() {
		return (effectTickContext -> {
			if (effectTickContext.getEntity() instanceof ForgePlatformLivingEntity e) {
				if (ordinal instanceof PlatformInstanceMobEffect eff) {
					eff.onEffectStopped(e.ordinal, effectTickContext.getAmplifier());
				} else {
					ordinal.applyEffectTick(e.ordinal, effectTickContext.getAmplifier());
				}
			}
		});
	}

	@Override
	public @NotNull MobEffectCategory getCategory() {
		return MobEffectCategory.valueOf(ordinal.getCategory().name());
	}
}
