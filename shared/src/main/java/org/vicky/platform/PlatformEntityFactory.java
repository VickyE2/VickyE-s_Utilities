package org.vicky.platform;

import org.vicky.platform.entity.PlatformArrow;

public interface PlatformEntityFactory {
    PlatformArrow spawnArrowAt(double x, double y, double z);
}
