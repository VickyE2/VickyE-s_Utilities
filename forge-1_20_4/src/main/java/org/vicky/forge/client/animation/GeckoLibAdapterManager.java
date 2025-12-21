/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.client.animation;

import org.vicky.forge.entity.PlatformBasedLivingEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;

/**
 * Manages a lightweight per-entity animatable wrapper used purely for playing
 * animations via GeckoLib controllers. This class is client-only.
 */
public final class GeckoLibAdapterManager {

	private GeckoLibAdapterManager() {
	}

	public static void playAnimationClient(Integer entityId, String animationKey, Boolean loop) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;

		Entity e = mc.level.getEntity(entityId);
		if (!(e instanceof PlatformBasedLivingEntity pb))
			return;

		// call the entity's forcePlay method you already implemented
		pb.forcePlay(animationKey == null ? "" : animationKey, loop != null && loop);
	}
}