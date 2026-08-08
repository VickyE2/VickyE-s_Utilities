/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.client.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.vicky.forge.entity.PlatformBasedLivingEntity;
import org.vicky.platform.items.Animation;

/**
 * Manages a lightweight per-entity animatable wrapper used purely for playing
 * animations via GeckoLib controllers. This class is client-only.
 */
public final class GeckoLibAdapterManager {

	private GeckoLibAdapterManager() {
	}

	public static void playAnimationClient(Integer entityId, Animation animation) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;

		Entity e = mc.level.getEntity(entityId);
		if (!(e instanceof PlatformBasedLivingEntity pb))
			return;

		// call the entity's forcePlay method you already implemented
		pb.forcePlay(animation);
	}

	public static void stopAnimationClient(Integer entityId, String animation) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;

		Entity e = mc.level.getEntity(entityId);
		if (!(e instanceof PlatformBasedLivingEntity pb))
			return;

		// call the entity's forcePlay method you already implemented
		pb.forceStop(animation);
	}

	public static void pauseAnimationClient(Integer entityId) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;

		Entity e = mc.level.getEntity(entityId);
		if (!(e instanceof PlatformBasedLivingEntity pb))
			return;

		// call the entity's forcePlay method you already implemented
		pb.forcePause();
	}

	public static void resumeAnimationClient(Integer entityId) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return;

		Entity e = mc.level.getEntity(entityId);
		if (!(e instanceof PlatformBasedLivingEntity pb))
			return;

		// call the entity's forcePlay method you already implemented
		pb.forceResume();
	}
}