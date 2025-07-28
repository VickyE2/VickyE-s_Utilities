/* Licensed under Apache-2.0 2025. */
package org.vicky.platform;

import org.vicky.platform.world.PlatformLocation;

public interface PlatformLocationAdapter {
	Object toNative(PlatformLocation location); // Could return Bukkit Location or Fabric Vec3d, etc.

	PlatformLocation fromNative(Object nativeLocation);
}
