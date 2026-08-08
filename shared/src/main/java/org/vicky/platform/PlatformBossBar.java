/* Licensed under Apache-2.0 2026. */
package org.vicky.platform;

import net.kyori.adventure.text.Component;
import org.vicky.platform.defaults.BossBarOverlay;
import org.vicky.platform.player.PlatformPlayer;
import org.vicky.platform.utils.BossBarDescriptor;

public interface PlatformBossBar {
	void setTitle(Component title);
	void setProgress(Float progress);
	void setVisible(Boolean visible, PlatformPlayer player);

	boolean isVisible(PlatformPlayer player);

	void setColor(IColor color);
	void setOverlay(BossBarOverlay overlay);

	void addViewer(PlatformPlayer viewer);
	void removeViewer(PlatformPlayer viewer);
	void hideAll();

	BossBarDescriptor getDescriptor();

	void setDescriptor(BossBarDescriptor descriptor);

	void updateFromDescriptor();
}
