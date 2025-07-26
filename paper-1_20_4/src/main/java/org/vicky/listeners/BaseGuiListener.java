/* Licensed under Apache-2.0 2024. */
package org.vicky.listeners;

import static org.vicky.global.Global.globalConfigManager;

import java.util.*;
import java.util.function.Consumer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.guiparent.ButtonAction;
import org.vicky.guiparent.GuiCreator;
import org.vicky.utilities.ANSIColor;
import org.vicky.utilities.ContextLogger.ContextLogger;

/**
 * BaseGuiListener is the core listener for GUI events.
 * <p>
 * This class provides final implementations of {@link #onInventoryClick(InventoryClickEvent)} and
 * {@link #onInventoryClose(InventoryCloseEvent)} to handle base GUI functionality and then delegate to
 * additional handlers registered via {@link #addInventoryClickHandler(Consumer)} and
 * {@link #addInventoryCloseHandler(Consumer)}.
 * </p>
 */
public abstract class BaseGuiListener implements Listener {
  private static final Map<Class<? extends BaseGuiListener>, BaseGuiListener> INSTANCES =
      new HashMap<>();

  protected final JavaPlugin plugin;

  protected static long CLICK_DELAY = 200;
  protected final Map<Inventory, Map<Integer, ButtonAction<?>>> buttonActions = new HashMap<>();
  protected final List<Consumer<InventoryClickEvent>> additionalClickHandlers = new ArrayList<>();
  protected final List<Consumer<InventoryOpenEvent>> additionalOpenHandlers = new ArrayList<>();
  protected final List<Consumer<InventoryCloseEvent>> additionalCloseHandlers = new ArrayList<>();
  protected final List<Consumer<InventoryDragEvent>> additionalDragHandlers = new ArrayList<>();
  protected final ContextLogger logger =
      new ContextLogger(ContextLogger.ContextType.SYSTEM, "GUI-LISTENER");
  protected final List<Inventory> guiInventories = new ArrayList<>();
  protected long lastClickTime = 0;

  /**
   * Constructs a BaseGuiListener with the specified JavaPlugin instance.
   *
   * @param plugin the plugin instance
   */
  protected BaseGuiListener(JavaPlugin plugin) {
    this.plugin = plugin;
    registerSingletonInstance();
  }

  @SuppressWarnings("unchecked")
  private void registerSingletonInstance() {
    Class<? extends BaseGuiListener> clazz = this.getClass();
    if (INSTANCES.containsKey(clazz)) {
      throw new IllegalStateException(clazz.getSimpleName() + " is already instantiated!");
    }
    INSTANCES.put(clazz, this);
  }

  @SuppressWarnings("unchecked")
  public static <T extends BaseGuiListener> T getListenerInstance(Class<T> type) {
    return (T) INSTANCES.get(type);
  }

  /**
   * Sets the delay timer for when the next click should be considered.
   *
   * @param clickDelay the time in milliseconds of delay
   */
  public static void setClickDelay(long clickDelay) {
    CLICK_DELAY = clickDelay;
  }

  /**
   * Parses a slot range string (e.g., "1-10,12,15-18") into a set of zero-based slot indices.
   *
   * @param slotRange the slot range string to parse
   * @return a set of integer slot indices
   */
  protected Set<Integer> parseSlots(String slotRange) {
    Set<Integer> slots = new HashSet<>();
    String[] parts = slotRange.split(",");
    for (String part : parts) {
      if (part.contains("-")) {
        String[] range = part.split("-");
        int start = Math.max(0, Integer.parseInt(range[0]) - 1);
        int end = Math.min(9 * 9, Integer.parseInt(range[1]) - 1);
        for (int i = start; i <= end; i++) {
          slots.add(i);
        }
      } else {
        int slot = Integer.parseInt(part) - 1;
        slots.add(slot);
      }
    }
    return slots;
  }

  /**
   * Registers a {@link ButtonAction} for each slot specified in the provided ItemConfig(s).
   *
   * @param itemConfigs one or more ItemConfig objects specifying the slot ranges
   */
  public final void registerButton(Inventory inventory, GuiCreator.ItemConfig... itemConfigs) {
    for (GuiCreator.ItemConfig itemConfig : itemConfigs) {
      ButtonAction<?> action = itemConfig.getButtonAction();
      if (globalConfigManager == null || globalConfigManager.getBooleanValue("Debug"))
        logger.printBukkit(
            ANSIColor.colorize(
                "Button registered with Action " + action.getActionType(), ANSIColor.CYAN),
            ContextLogger.LogType.AMBIENCE,
            false);
      Set<Integer> slotSet = parseSlots(itemConfig.getSlotRange());
      for (int slot : slotSet) {
        buttonActions.computeIfAbsent(inventory, k -> new HashMap<>()).put(slot, action);
      }
    }
  }

  /**
   * Adds a handler for inventory click events.
   *
   * @param handler a Consumer that processes {@link InventoryClickEvent} instances
   */
  public void addInventoryClickHandler(Consumer<InventoryClickEvent> handler) {
    additionalClickHandlers.add(handler);
  }

  /**
   * Adds a handler for inventory click events.
   *
   * @param handler a Consumer that processes {@link InventoryClickEvent} instances
   */
  public void addInventoryOpenedHandler(Consumer<InventoryOpenEvent> handler) {
    additionalOpenHandlers.add(handler);
  }

  /**
   * Adds a handler for inventory drag events.
   *
   * @param handler a Consumer that processes {@link InventoryDragEvent} instances
   */
  public void addInventoryDragHandler(Consumer<InventoryDragEvent> handler) {
    additionalDragHandlers.add(handler);
  }

  /**
   * Adds a handler for inventory close events.
   *
   * @param handler a Consumer that processes {@link InventoryCloseEvent} instances
   */
  public void addInventoryCloseHandler(Consumer<InventoryCloseEvent> handler) {
    additionalCloseHandlers.add(handler);
  }

  /**
   * Final implementation of the inventory drag event.
   * <p>
   * This method cancels drag events if no additional drag handlers are specified.
   * </p>
   *
   * @param event the {@link InventoryDragEvent} being handled
   */
  @EventHandler
  public void onInventoryDrag(InventoryDragEvent event) {
    if (guiInventories.isEmpty() || !guiInventories.contains(event.getInventory())) return;
    if (additionalDragHandlers.isEmpty()) event.setCancelled(true);
    for (Consumer<InventoryDragEvent> handler : additionalDragHandlers) {
      handler.accept(event);
    }
  }

  /**
   * Final implementation of the inventory click event.
   * <p>
   * This method processes button actions and then delegates to additional click handlers.
   * </p>
   *
   * @param event the {@link InventoryClickEvent} being handled
   */
  @EventHandler
  public final void onInventoryClick(InventoryClickEvent event) {
    if (event.getClickedInventory() == null
        || guiInventories.isEmpty()
        || !guiInventories.contains(event.getClickedInventory())) {
      return;
    }
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastClickTime < CLICK_DELAY) {
      event.setCancelled(true);
      return;
    }
    lastClickTime = currentTime;
    for (Consumer<InventoryClickEvent> handler : additionalClickHandlers) {
      handler.accept(event);
    }

    int slot = event.getSlot();
    if (buttonActions.containsKey(event.getClickedInventory()))
      if (buttonActions.get(event.getClickedInventory()).containsKey(slot)) {
        ButtonAction<?> action = buttonActions.get(event.getClickedInventory()).get(slot);
        if (action.getActionType() != ButtonAction.ActionType.CHAIN)
          action.execute((Player) event.getWhoClicked(), plugin);
        else action.chainExecute((Player) event.getWhoClicked());
      }
  }

  /**
   * Final implementation of the inventory close event.
   * <p>
   * This method clears button actions and delegates to any additional close handlers.
   * </p>
   *
   * @param event the {@link InventoryCloseEvent} being handled
   */
  @EventHandler
  public final void onInventoryClose(InventoryCloseEvent event) {
    if (guiInventories.isEmpty() || !guiInventories.contains(event.getInventory())) {
      return;
    }
    for (Consumer<InventoryCloseEvent> handler : additionalCloseHandlers) {
      handler.accept(event);
    }
    if (!guiInventories.contains(event.getInventory())) {
      buttonActions.remove(event.getInventory());
      removeGuiInventory(event.getInventory());
    }
  }

  /**
   * Final implementation of the inventory open event.
   *
   * @param event the {@link InventoryOpenEvent} being handled
   */
  @EventHandler
  public final void onInventoryOpen(InventoryOpenEvent event) {
    if (guiInventories.isEmpty() || !guiInventories.contains(event.getInventory())) {
      return;
    }
    for (Consumer<InventoryOpenEvent> handler : additionalOpenHandlers) {
      handler.accept(event);
    }
  }

  /**
   * Adds the GUI inventory to those managed by this listener.
   * <p>
   * This should be called whenever the GUI inventory is being added
   * </p>
   *
   * @param inventory the {@link Inventory} to set;
   */
  public void addGuiInventory(@NotNull Inventory inventory) {
    this.guiInventories.add(inventory);
    logger.printBukkit(
        ANSIColor.colorize("Inventory has added: " + inventory, ANSIColor.CYAN),
        ContextLogger.LogType.AMBIENCE);
  }

  /**
   * Removes the GUI inventory from those managed by this listener.
   * <p>
   * This should be called whenever the GUI inventory is being removed
   * </p>
   *
   * @param inventory the {@link Inventory} to set; may be null to clear the inventory
   */
  public void removeGuiInventory(@Nullable Inventory inventory) {
    if (inventory == null) {
      return;
    }
    if (guiInventories.contains(inventory)) {
      this.guiInventories.remove(inventory);
      logger.printBukkit(
          ANSIColor.colorize("Inventory has removed: " + inventory, ANSIColor.CYAN),
          ContextLogger.LogType.AMBIENCE);
    }
  }

  public ButtonAction<?> removeButton(Inventory inventory, int i) {
    if (guiInventories.contains(inventory)) {
      return buttonActions.get(inventory).remove(i);
    }
    return null;
  }

  public ButtonAction[] removeButtons(Inventory inventory, int... iz) {
    if (guiInventories.contains(inventory)) {
      Set<ButtonAction> actions = new HashSet<>();
      for (int i : iz) {
        var action = buttonActions.get(inventory).remove(i);
        actions.add(action);
      }
      return actions.toArray(ButtonAction[]::new);
    }
    return null;
  }
}
