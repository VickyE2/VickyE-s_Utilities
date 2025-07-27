package org.vicky.platform;

import net.kyori.adventure.text.Component;
import org.vicky.platform.defaults.BossBarColor;
import org.vicky.platform.defaults.BossBarOverlay;

public interface PlatformBossBarFactory {
    PlatformBossBar createBossBar(
            Component title,
            Float progress,
            BossBarColor color,
            BossBarOverlay overlay,
            String context
    );
}