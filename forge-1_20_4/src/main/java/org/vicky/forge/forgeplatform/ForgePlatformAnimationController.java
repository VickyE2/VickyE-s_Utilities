/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform;

import org.jetbrains.annotations.Nullable;
import org.vicky.forge.network.PacketHandler;
import org.vicky.forge.network.registeredpackets.PlayAnimationPacket;
import org.vicky.platform.entity.PlatformAnimationController;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.PacketDistributor;

public class ForgePlatformAnimationController implements PlatformAnimationController {
	private final Entity ordinal;
	private ForgePlatformAnimationController(Entity e) {
		this.ordinal = e;
	}

	public static ForgePlatformAnimationController from(Entity e) {
		return new ForgePlatformAnimationController(e);
	}

	@Override
	public void play(@Nullable String animationKey, boolean loop) {
		var key = animationKey != null ? animationKey : "";
		var pkt = new PlayAnimationPacket(ordinal.getId(), key, loop);
		PacketHandler.MAIN_CHNNEL.send(pkt, PacketDistributor.TRACKING_ENTITY_AND_SELF.with(ordinal));
	}
}
