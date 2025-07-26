/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.PermittedObjects;

import org.bukkit.inventory.ItemStack;
import org.vicky.utilities.PermittedObject;

public record AllowedItemStack(ItemStack value) implements PermittedObject<ItemStack> {
  @Override
  public ItemStack getValue() {
    return value;
  }
}
