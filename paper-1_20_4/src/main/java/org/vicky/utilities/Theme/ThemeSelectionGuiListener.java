/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.Theme;

import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.listeners.BaseGuiListener;

public class ThemeSelectionGuiListener extends BaseGuiListener {
  /**
   * Constructs a BaseGuiListener with the specified JavaPlugin instance.
   *
   * @param plugin the plugin instance
   */
  public ThemeSelectionGuiListener(JavaPlugin plugin) {
    super(plugin);
    addInventoryClickHandler(event -> event.setCancelled(true));
    addInventoryDragHandler(event -> event.setCancelled(true));
  }
}
