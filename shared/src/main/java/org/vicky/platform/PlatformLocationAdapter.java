package org.vicky.platform;

import org.vicky.platform.world.PlatformLocation;

public interface PlatformLocationAdapter<T> {
    T toNative(PlatformLocation location); // Could return Bukkit Location or Fabric Vec3d, etc.

    PlatformLocation fromNative(T nativeLocation);
}