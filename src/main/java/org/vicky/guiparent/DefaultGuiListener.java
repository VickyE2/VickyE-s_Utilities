/* Licensed under Apache-2.0 2024. */
package org.vicky.guiparent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.listeners.BaseGuiListener;

public class DefaultGuiListener extends BaseGuiListener {
  public DefaultGuiListener(JavaPlugin plugin) {
    super(plugin);
  }
}
