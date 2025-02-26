/* Licensed under Apache-2.0 2024. */
package org.vicky.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.guiparent.ButtonAction;
import org.vicky.guiparent.GuiCreator;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * BaseGuiListener is the core listener for GUI events.
 * Instead of requiring overriding of onInventoryClick/onInventoryClose,
 * this class provides final implementations that call a list of additional handlers.
 * Developers can add behavior via addInventoryClickHandler() and addInventoryCloseHandler().
 */
public abstract class BaseGuiListener implements Listener {

  private final Map<Integer, ButtonAction> buttonActions = new HashMap<>();
  private final List<Consumer<InventoryClickEvent>> additionalClickHandlers = new ArrayList<>();
  private final List<Consumer<InventoryCloseEvent>> additionalCloseHandlers = new ArrayList<>();
  private long lastClickTime = 0;
  private final ContextLogger logger = new ContextLogger(ContextLogger.ContextType.SYSTEM, "GUI-LISTENER");
  private static final long CLICK_DELAY = 200;
  private Inventory guiInventory = null;
  protected final JavaPlugin plugin;

  public BaseGuiListener(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  protected Set<Integer> parseSlots(String slotRange) {
    Set<Integer> slots = new HashSet<>();
    String[] parts = slotRange.split(",");
    for (String part : parts) {
      if (part.contains("-")) {
        String[] range = part.split("-");
        int start = Math.max(0, Integer.parseInt(range[0]) - 1); // 0-based index
        int end = Math.min(9 * 9 - 1, Integer.parseInt(range[1]) - 1);
        for (int i = start; i <= end; i++) {
          slots.add(i);
        }
      } else {
        int slot = Integer.parseInt(part) - 1; // Convert to 0-based index
        slots.add(slot);
      }
    }
    return slots;
  }

  /**
   * Registers a ButtonAction for the provided slot range via ItemConfig.
   */
  public void registerButton(ButtonAction action, GuiCreator.ItemConfig... itemConfigs) {
    logger.printBukkit(ANSIColor.colorize("Button registered with Action " + action.getActionType() +
            " and data: " + action.getActionData(), ANSIColor.CYAN), ContextLogger.LogType.AMBIENCE, false);
    for (GuiCreator.ItemConfig itemConfig : itemConfigs) {
      Set<Integer> slotSet = parseSlots(itemConfig.getSlotRange());
      for (int slot : slotSet) {
        buttonActions.put(slot, action);
      }
    }
  }

  /**
   * Adds a handler for inventory click events.
   */
  public void addInventoryClickHandler(Consumer<InventoryClickEvent> handler) {
    additionalClickHandlers.add(handler);
  }

  /**
   * Adds a handler for inventory close events.
   */
  public void addInventoryCloseHandler(Consumer<InventoryCloseEvent> handler) {
    additionalCloseHandlers.add(handler);
  }

  /**
   * Final implementation of the inventory click event.
   * This method handles the base logic (e.g., processing button actions) and then
   * delegates to any additional click handlers registered.
   */
  @EventHandler
  public final void onInventoryClick(InventoryClickEvent event) {
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastClickTime < CLICK_DELAY) {
      event.setCancelled(true);
      return;
    }
    lastClickTime = currentTime;

    if (event.getClickedInventory() == null) {
      event.setCancelled(false);
      return;
    }

    if (guiInventory == null || event.getClickedInventory() != guiInventory) {
      event.setCancelled(false);
      return;
    }

    event.setCancelled(true);
    int slot = event.getSlot();
    if (buttonActions.containsKey(slot)) {
      ButtonAction action = buttonActions.get(slot);
      action.execute((Player) event.getWhoClicked(), plugin);
    }

    // Call additional registered handlers.
    for (Consumer<InventoryClickEvent> handler : additionalClickHandlers) {
      handler.accept(event);
    }
  }

  /**
   * Final implementation of the inventory close event.
   * Delegates to any additional close handlers.
   */
  @EventHandler
  public final void onInventoryClose(InventoryCloseEvent event) {
    if (event.getInventory() == guiInventory) {
      buttonActions.clear();
      setGuiInventory(null);
    }
    for (Consumer<InventoryCloseEvent> handler : additionalCloseHandlers) {
      handler.accept(event);
    }
  }

  /**
   * Sets the GUI inventory that this listener manages.
   * Must be implemented by subclasses.
   *
   * @param inventory the Inventory to set.
   */
  public void setGuiInventory(Inventory inventory) {
    if (inventory == null) {
      logger.printBukkit("GUI inventory is being set to null! Make sure to initialize it before use.", ContextLogger.LogType.WARNING);
    }
    logger.printBukkit(ANSIColor.colorize("Inventory has been set to: " + guiInventory, ANSIColor.CYAN), ContextLogger.LogType.AMBIENCE);
    this.guiInventory = inventory;
  }
}
