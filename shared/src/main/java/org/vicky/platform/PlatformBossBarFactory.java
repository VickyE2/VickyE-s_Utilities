/* Licensed under Apache-2.0 2025. */
package org.vicky.platform;

import org.vicky.platform.defaults.BossBarColor;
import org.vicky.platform.defaults.BossBarOverlay;

import net.kyori.adventure.text.Component;

public interface PlatformBossBarFactory {
	PlatformBossBar createBossBar(Component title, Float progress, BossBarColor color, BossBarOverlay overlay,
			String context);
}
