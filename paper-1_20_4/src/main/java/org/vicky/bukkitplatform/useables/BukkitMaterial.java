package org.vicky.bukkitplatform.useables;

import org.bukkit.Material;
import org.vicky.platform.utils.ResourceLocation;
import org.vicky.platform.world.PlatformMaterial;

public class BukkitMaterial implements PlatformMaterial {

    private final Material material;

    public BukkitMaterial(Material material) {
        this.material = material;
    }

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
