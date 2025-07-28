/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.vicky.bukkitplatform.BukkitBossBarFactory;
import org.vicky.platform.PlatformBossBar;
import org.vicky.platform.PlatformPlayer;
import org.vicky.platform.defaults.BossBarColor;
import org.vicky.platform.defaults.BossBarOverlay;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

public class BukkitPlatformBossBar implements PlatformBossBar {
	private final BossBar bar;

	public BukkitPlatformBossBar(BossBar bar) {
		this.bar = bar;
	}

	@Override
	public void setProgress(Float progress) {
		bar.progress(progress);
	}

	@Override
	public void setVisible(Boolean visible, PlatformPlayer player) {
		if (visible) {
			bar.addViewer(((BukkitPlatformPlayer) player).getBukkitPlayer());
		} else {
			bar.removeViewer(((BukkitPlatformPlayer) player).getBukkitPlayer());
		}
	}

	@Override
	public void setColor(BossBarColor color) {
		bar.color(BukkitBossBarFactory.adaptColor(color));
	}

	@Override
	public void setOverlay(BossBarOverlay overlay) {
		bar.overlay(BukkitBossBarFactory.adaptOverlay(overlay));
	}

	@Override
	public void setTitle(Component title) {
		bar.name(title);
	}

	@Override
	public void addViewer(PlatformPlayer player) {
		bar.addViewer(((BukkitPlatformPlayer) player).getBukkitPlayer());
	}

	@Override
	public void removeViewer(PlatformPlayer player) {
		bar.removeViewer(((BukkitPlatformPlayer) player).getBukkitPlayer());
	}

	@Override
	public void hideAll() {
		bar.viewers().forEach(v -> bar.removeViewer((Audience) v));
	}

	public BossBar getBossBar() {
		return bar;
	}
}
