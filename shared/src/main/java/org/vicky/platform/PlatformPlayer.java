package org.vicky.platform;

import net.kyori.adventure.text.Component;
import org.vicky.platform.utils.Location3D;

import java.util.UUID;

public interface PlatformPlayer {
    UUID uniqueId();
    Component name();
    void sendMessage(Component msg);
    void sendMessage(String msg);
    void sendComponent(Component component);
    void showBossBar(PlatformBossBar bar);
    void hideBossBar(PlatformBossBar bar);
    void playSound(Location3D location, String soundName, Object soundCategory, Float volume, Float pitch);
    Location3D getLocation();
}