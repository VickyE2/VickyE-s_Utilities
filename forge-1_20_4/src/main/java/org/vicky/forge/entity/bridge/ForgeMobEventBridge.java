/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.bridge;

import static org.vicky.forge.forgeplatform.useables.ForgeHacks.toVicky;

import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.forge.entity.PlatformBasedLivingEntity;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.entity.*;
import org.vicky.platform.entity.distpacher.EntityTaskManager;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge → Platform event bridge.
 *
 * This class must be registered on the Forge EVENT BUS. It translates Forge
 * events into calls on MobEntityEventHandler.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeMobEventBridge {

	private ForgeMobEventBridge() {
	}

	@SubscribeEvent
	public static void onLivingAttacked(LivingAttackEvent event) {
		if (!(event.getEntity() instanceof PlatformBasedLivingEntity victim))
			return;
		if (!(event.getSource().getEntity() instanceof LivingEntity attacker))
			return;

		var handler = victim.getDescriptor().getEventHandler();

		EventResult result = handler.getHandler().onAttacked(victim.asPlatform(),
				ForgePlatformLivingEntity.from(attacker));

		if (result == EventResult.CANCEL) {
			event.setCanceled(true);
		}
	}

	/*
	 * ------------------------------------------------------------ ATTACK (outgoing
	 * damage) ------------------------------------------------------------
	 */

	@SubscribeEvent
	public static void onLivingDamage(LivingDamageEvent event) {
		if (!(event.getSource().getEntity() instanceof PlatformBasedLivingEntity attacker))
			return;

		var handler = attacker.getDescriptor().getEventHandler();

		EventResult result = handler.getHandler().onAttack(attacker.asPlatform(),
				ForgePlatformLivingEntity.from(attacker));

		if (result == EventResult.CANCEL) {
			event.setCanceled(true);
		}
	}

	/*
	 * ------------------------------------------------------------ POTION APPLIED
	 * ------------------------------------------------------------
	 */

	@SubscribeEvent
	public static void onPotionApplied(MobEffectEvent.Added event) {
		if (!(event.getEntity() instanceof PlatformBasedLivingEntity entity))
			return;

		var handler = entity.getDescriptor().getEventHandler();

		EventResult result = handler.getHandler().onApplyPotion(entity.asPlatform(),
				PlatformPlugin.effectBridge()
				.getEffect(toVicky(event.getEffectInstance().getEffect().builtInRegistryHolder().key().location())));

		if (result == EventResult.CANCEL) {
			event.setCanceled(true);
		}
	}
}
