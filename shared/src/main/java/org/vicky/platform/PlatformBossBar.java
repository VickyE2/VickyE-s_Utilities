/* Licensed under Apache-2.0 2025. */
package org.vicky.platform;

import org.vicky.platform.defaults.BossBarColor;
import org.vicky.platform.defaults.BossBarOverlay;

import net.kyori.adventure.text.Component;

public interface PlatformBossBar {
	void setTitle(Component title);

	void setProgress(Float progress);

	void setVisible(Boolean visible, PlatformPlayer player);

	void setColor(BossBarColor color);

	void setOverlay(BossBarOverlay overlay);

	void addViewer(PlatformPlayer viewer);

	void removeViewer(PlatformPlayer viewer);

	void hideAll();
}
