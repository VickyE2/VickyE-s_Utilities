/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import org.vicky.bukkitplatform.useables.BukkitPlatformBossBar;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformBossBarFactory;
import org.vicky.platform.defaults.BossBarColor;
import org.vicky.platform.defaults.BossBarOverlay;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

public class BukkitBossBarFactory implements PlatformBossBarFactory {
	@Override
	public PlatformBossBar createBossBar(Component title, Float progress, BossBarColor color, BossBarOverlay overlay,
			String context) {
		BossBar bar = BossBar.bossBar(title, progress != null ? progress : 1.0f, adaptColor(color),
				adaptOverlay(overlay));
		return new BukkitPlatformBossBar(bar);
	}

	public static BossBar.Color adaptColor(BossBarColor color) {
		return switch (color) {
			case PINK -> BossBar.Color.PINK;
			case BLUE -> BossBar.Color.BLUE;
			case RED -> BossBar.Color.RED;
			case GREEN -> BossBar.Color.GREEN;
			case YELLOW -> BossBar.Color.YELLOW;
			case PURPLE -> BossBar.Color.PURPLE;
			case WHITE -> BossBar.Color.WHITE;
		};
	}

	public static BossBar.Overlay adaptOverlay(BossBarOverlay overlay) {
		return switch (overlay) {
			case PROGRESS -> BossBar.Overlay.PROGRESS;
			case NOTCHED_6 -> BossBar.Overlay.NOTCHED_6;
			case NOTCHED_10 -> BossBar.Overlay.NOTCHED_10;
			case NOTCHED_12 -> BossBar.Overlay.NOTCHED_12;
			case NOTCHED_20 -> BossBar.Overlay.NOTCHED_20;
		};
	}
}
