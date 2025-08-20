/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.vicky.bukkitplatform.useables.BukkitItem;
import org.vicky.bukkitplatform.useables.BukkitMaterial;
import org.vicky.platform.PlatformItem;
import org.vicky.platform.PlatformItemFactory;
import org.vicky.platform.world.PlatformMaterial;

public class BukkitItemFactory implements PlatformItemFactory {
    @Override
    public PlatformItem fromMaterial(PlatformMaterial platformMaterial) {
        if (platformMaterial instanceof BukkitMaterial material) {
            return new BukkitItem(new ItemStack(material.material()));
        }
        return new BukkitItem(
                Bukkit.getItemFactory().createItemStack(platformMaterial.getResourceLocation().asString()));
    }
}
