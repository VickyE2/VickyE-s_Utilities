/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.effects;

import org.jetbrains.annotations.NotNull;
import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.platform.entity.EffectApplyContext;
import org.vicky.platform.entity.EffectDescriptor;
import org.vicky.platform.entity.EffectRemoveContext;
import org.vicky.platform.entity.EffectTickContext;

import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class PlatformInstanceMobEffect extends MobEffect {

	private final EffectDescriptor desc;
	private PlatformInstanceMobEffect(EffectDescriptor d) {
		super(MobEffectCategory.valueOf(d.getCategory().name()), d.getColor());
		this.desc = d;
	}

	public static PlatformInstanceMobEffect from(EffectDescriptor d) {
		return new PlatformInstanceMobEffect(d);
	}

	public ForgePlatformEffect toPlatform() {
		return ForgePlatformEffect.from(this);
	}

	@Override
	public Component getDisplayName() {
		return Component.literal(desc.getDisplayName());
	}

	@Override
	public boolean isInstantenous() {
		return desc.isInstant();
	}

	@Override
	public void applyEffectTick(@NotNull LivingEntity e, int amplifier) {
		desc.getOnTick().accept(new EffectTickContext(ForgePlatformLivingEntity.from(e), amplifier, 0));
	}

	@Override
	public void onEffectStarted(@NotNull LivingEntity e, int amplifier) {
		desc.getOnEffectStarted().accept(new EffectApplyContext(ForgePlatformLivingEntity.from(e), amplifier, 0));
	}

	public void onEffectStopped(@NotNull LivingEntity e, int amplifier) {
		desc.getOnRemove().accept(new EffectRemoveContext(ForgePlatformLivingEntity.from(e), amplifier));
	}

	public EffectDescriptor getDesc() {
		return desc;
	}
}
