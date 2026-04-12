package org.vicky.platform.world;

import org.jetbrains.annotations.NotNull;
import org.vicky.platform.utils.ResourceLocation;

public interface PlatformMaterial {
    /**
     * This doesn't mean for a literal solid, It just means if it's an item or an actual placeable block
     */
    boolean isSolid();
    boolean isAir();
    boolean isLiquid();

    @NotNull
    ResourceLocation getResourceLocation();
}
