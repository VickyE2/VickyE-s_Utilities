/* Licensed under Apache-2.0 2025. */
package org.vicky.platform;

import java.util.UUID;

import org.vicky.platform.entity.PlatformEntity;
import org.vicky.platform.entity.PlatformLivingEntity;
import org.vicky.platform.world.PlatformLocation;

import net.kyori.adventure.text.Component;

public interface PlatformPlayer extends PlatformLivingEntity {
	UUID uniqueId();
	Component name();
	void sendMessage(Component msg);
	void sendMessage(String msg);
	void sendMessage(PlatformEntity sender, String message);
	void sendMessage(PlatformEntity sender, Component message);
	void showBossBar(PlatformBossBar bar);
	void hideBossBar(PlatformBossBar bar);
	double flightSpeed();
	void giveItem(PlatformItemStack item);
	void playSound(PlatformLocation location, String soundName, Object soundCategory, Float volume, Float pitch);
	// void openGui(GuiSpec spec);

	@Override
	default boolean isPlayer() {
		return true;
	}
}