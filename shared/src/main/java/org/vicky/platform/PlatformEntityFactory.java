package org.vicky.platform;

import org.vicky.platform.entity.PlatformArrow;
import org.vicky.platform.world.PlatformLocation;

public interface PlatformEntityFactory {
    PlatformArrow spawnArrowAt(PlatformLocation loc);
}
