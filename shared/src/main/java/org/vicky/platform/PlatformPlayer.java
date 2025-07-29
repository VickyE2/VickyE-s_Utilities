package org.vicky.platform;

import net.kyori.adventure.text.Component;
import org.vicky.platform.utils.Location3D;
import org.vicky.platform.world.PlatformLocation;

import java.util.UUID;

public interface PlatformPlayer {
    UUID uniqueId();
    Component name();
    void sendMessage(Component msg);
    void sendMessage(String msg);
    void sendComponent(Component component);
    void showBossBar(PlatformBossBar bar);
    void hideBossBar(PlatformBossBar bar);
    void playSound(PlatformLocation location, String soundName, Object soundCategory, Float volume, Float pitch);
    PlatformLocation getLocation();
}