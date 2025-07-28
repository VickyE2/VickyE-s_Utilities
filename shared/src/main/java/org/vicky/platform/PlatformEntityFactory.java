/* Licensed under Apache-2.0 2025. */
package org.vicky.platform;

import org.vicky.platform.entity.PlatformArrow;
import org.vicky.platform.world.PlatformLocation;

public interface PlatformEntityFactory {
	PlatformArrow spawnArrowAt(PlatformLocation location);
}
