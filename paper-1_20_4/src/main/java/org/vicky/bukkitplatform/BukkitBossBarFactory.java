/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.vicky.bukkitplatform.useables.BukkitBossBarDescriptor;
import org.vicky.bukkitplatform.useables.BukkitPlatformBossBar;
import org.vicky.bukkitplatform.useables.ColorAdapters;
import org.vicky.platform.IColor;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformBossBarFactory;
import org.vicky.platform.defaults.BossBarOverlay;

public class BukkitBossBarFactory implements PlatformBossBarFactory<BukkitBossBarDescriptor> {
    public static BarColor adaptColor(IColor color) {
        return ColorAdapters.adaptColor(color);
	}

    public static BarStyle adaptOverlay(BossBarOverlay overlay) {
		return switch (overlay) {
            case PROGRESS -> BarStyle.SOLID;
            case NOTCHED_6 -> BarStyle.SEGMENTED_6;
            case NOTCHED_10 -> BarStyle.SEGMENTED_10;
            case NOTCHED_12 -> BarStyle.SEGMENTED_12;
            case NOTCHED_20 -> BarStyle.SEGMENTED_20;
		};
	}

	@Override
    public PlatformBossBar createBossBar(BukkitBossBarDescriptor descriptor) {
        org.bukkit.boss.BossBar bar = descriptor.toBukkitBossBar();
        return new BukkitPlatformBossBar(bar, descriptor);
	}
}
