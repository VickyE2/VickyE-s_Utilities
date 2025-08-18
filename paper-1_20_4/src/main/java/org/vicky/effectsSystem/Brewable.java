/* Licensed under Apache-2.0 2024. */
package org.vicky.effectsSystem;

import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.vicky.guiparent.GuiCreator;

public interface Brewable {
  String getPotionName();

  String getEffectKey();

  ItemStack mainBrewItem();

  ItemStack bottomPotion();

  boolean canSplash();

  boolean canLinger();

  Color potionColor();

  GuiCreator.ItemConfig generateConfig();

  boolean hasGlint();

  default ItemStack brewResult(Player player) {
    return GuiCreator.createItem(generateConfig(), player);
  }
}
