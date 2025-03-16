/* Licensed under Apache-2.0 2024. */
package org.vicky.guiparent;

import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.listeners.BaseGuiListener;

/**
 * This is a gui Listener that cancels all forms of adding or removing to the inventory.
 * It should basically be used when it's a "button-only" inventory.
 */
public class CancelAllGuiListener extends BaseGuiListener {
  public CancelAllGuiListener(JavaPlugin plugin) {
    super(plugin);
    addInventoryClickHandler(event -> event.setCancelled(true));
    addInventoryDragHandler(event -> event.setCancelled(true));
  }
}
