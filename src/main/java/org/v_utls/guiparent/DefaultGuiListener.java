/* Licensed under Apache-2.0 2024. */
package org.v_utls.guiparent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.v_utls.listeners.BaseGuiListener;

public class DefaultGuiListener extends BaseGuiListener {

  @Override
  @EventHandler
  public void onInventoryClick(InventoryClickEvent event) {
    // Default behavior can be left empty or log a message if needed
  }

  @Override
  @EventHandler
  public void onInventoryClose(InventoryCloseEvent event) {
    // Default behavior can be left empty or log a message if needed
  }

  @Override
  public void setGuiInventory(Inventory inventory) {}
}
