package org.vicky.platform;

import org.vicky.platform.utils.BossBarDescriptor;

public interface PlatformBossBarFactory<T extends BossBarDescriptor> {
    PlatformBossBar createBossBar(T descriptor);
}