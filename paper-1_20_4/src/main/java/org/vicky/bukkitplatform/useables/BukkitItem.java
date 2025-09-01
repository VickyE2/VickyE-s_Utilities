/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.vicky.platform.PlatformItem;

public class BukkitItem implements PlatformItem {

    private final ItemStack stack;
    private final UUID id = UUID.randomUUID();

    public BukkitItem(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public String getName() {
        return stack.getType().name();
    }

    @Override
    public String getIdentifier() {
        return id.toString();
    }

    public ItemStack getStack() {
        return stack;
    }
}
