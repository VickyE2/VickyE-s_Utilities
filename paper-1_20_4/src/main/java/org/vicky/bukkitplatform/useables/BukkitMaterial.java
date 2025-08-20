/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.bukkit.Material;
import org.vicky.platform.utils.ResourceLocation;
import org.vicky.platform.world.PlatformMaterial;

public record BukkitMaterial(Material material) implements PlatformMaterial {

    @Override
    public boolean isSolid() {
        return material.isBlock();
    }

    @Override
    public boolean isAir() {
        return material.isAir();
    }

    @Override
    public ResourceLocation getResourceLocation() {
        return ResourceLocation.from(material.createBlockData().getAsString());
    }
}
