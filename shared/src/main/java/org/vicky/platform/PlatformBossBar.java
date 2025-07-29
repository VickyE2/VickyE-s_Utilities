package org.vicky.platform;

import net.kyori.adventure.text.Component;
import org.vicky.platform.defaults.BossBarColor;
import org.vicky.platform.defaults.BossBarOverlay;

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
