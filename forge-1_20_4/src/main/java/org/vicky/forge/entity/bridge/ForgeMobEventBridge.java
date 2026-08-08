/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.entity.bridge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.vicky.forge.entity.ForgePlatformLivingEntity;
import org.vicky.forge.entity.PlatformBasedLivingEntity;
import org.vicky.forge.forgeplatform.item.DescriptorItem;
import org.vicky.forge.forgeplatform.item.ForgeItemStack;
import org.vicky.forge.forgeplatform.player.ForgePlatformPlayer;
import org.vicky.forge.forgeplatform.useables.ForgeHacks;
import org.vicky.platform.PlatformPlugin;
import org.vicky.platform.entity.EventResult;

import static org.vicky.forge.forgeplatform.useables.ForgeHacks.toVicky;

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
	public static void onItemPickup(EntityItemPickupEvent event) {
		if (event.getItem().getItem().getItem() instanceof DescriptorItem descriptorItem) {
			if (event.getEntity() instanceof ServerPlayer player) {
				event.setResult(ForgeHacks.fromVicky(descriptorItem.getDescriptor().getHandler()
						.onPickedUpByPlayer(
								new ForgeItemStack(event.getItem().getItem()),
								ForgePlatformPlayer.adapt(player)
						))); // should return allow or deny event.setResult(Resul);
				return;
			}
			event.setResult(ForgeHacks.fromVicky(descriptorItem.getDescriptor().getHandler()
					.onPickedUp(
							new ForgeItemStack(event.getItem().getItem()),
							ForgePlatformLivingEntity.from(event.getEntity())
					)
			));
		}
	}

	@SubscribeEvent
	public static void onItemDropped(ItemTossEvent event) {
		if (event.getEntity().getItem().getItem() instanceof DescriptorItem descriptorItem) {
			var result = descriptorItem.getDescriptor().getHandler()
					.onDropped(
							new ForgeItemStack(event.getEntity().getItem()),
							ForgePlatformLivingEntity.from(event.getPlayer())
					);
			if (event.hasResult())
				event.setResult(ForgeHacks.fromVicky(result));
			if (event.isCancelable())
				switch (result) {
                    case ALLOW -> event.setCanceled(false);
                    case DENY -> event.setCanceled(true);
                };
		}
	}

	@SubscribeEvent
	public static void onLivingAttacked(LivingAttackEvent event) {
		if (!(event.getEntity() instanceof PlatformBasedLivingEntity victim))
			return;
		if (!(event.getSource().getEntity() instanceof LivingEntity attacker))
			return;

		var handler = victim.getDescriptor().getEventHandler();

		EventResult result = handler.getHandler().onAttacked(victim.getPlatform(),
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

		EventResult result = handler.getHandler().onAttack(attacker.getPlatform(),
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

		EventResult result = handler.getHandler().onApplyPotion(entity.getPlatform(),
				PlatformPlugin.effectBridge()
				.getEffect(toVicky(event.getEffectInstance().getEffect().builtInRegistryHolder().key().location())));

		if (result == EventResult.CANCEL) {
			event.setCanceled(true);
		}
	}
}
