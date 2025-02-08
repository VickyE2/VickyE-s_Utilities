/* Licensed under Apache-2.0 2024. */
package org.vicky.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * BaseGuiListener is an abstract class for GUI-related event handling.
 * <p>
 * Subclasses must implement methods for handling inventory click events,
 * inventory close events, and setting the GUI inventory.
 * </p>
 */
public abstract class BaseGuiListener implements Listener {

  /**
   * Handles inventory click events.
   * <p>
   * This method is called when a player clicks within the GUI inventory.
   * Subclasses must provide their specific implementation.
   * </p>
   *
   * @param event the InventoryClickEvent representing the click action
   */
  public abstract void onInventoryClick(InventoryClickEvent event);

  /**
   * Handles inventory close events.
   * <p>
   * This method is called when a player closes the GUI inventory.
   * Subclasses must provide their specific implementation.
   * </p>
   *
   * @param event the InventoryCloseEvent representing the close action
   */
  public abstract void onInventoryClose(InventoryCloseEvent event);

  /**
   * Sets the GUI inventory that this listener will manage.
   * <p>
   * Subclasses should implement this method to define and assign the specific Inventory instance
   * that is used for the GUI.
   * </p>
   *
   * @param inventory the Inventory to set as the GUI
   */
  public abstract void setGuiInventory(Inventory inventory);
}
