package org.vicky.platform;

import org.vicky.platform.utils.Location3D;

public interface PlatformLocationAdapter {
    Object toNative(Location3D location); // Could return Bukkit Location or Fabric Vec3d, etc.
    Location3D fromNative(Object nativeLocation);
}