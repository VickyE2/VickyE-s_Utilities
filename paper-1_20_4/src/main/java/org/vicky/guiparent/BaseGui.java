/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.guiparent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.listeners.BaseGuiListener;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.vicky.guiparent.GuiCreator.createItem;

/**
 * BaseGui is an abstract class providing the core functionality for creating
 * and displaying GUIs.
 * <p>
 * It initializes the GUI manager and registers a GUI listener for handling
 * inventory events. Subclasses must implement the {@link #showGui(Player)}
 * method to display the specific GUI.
 * </p>
 */
public abstract class BaseGui {

    protected final JavaPlugin plugin;
    protected final GuiCreator guiManager;
    private final Map<UUID, Inventory> current = new HashMap<>();
    private final List<GuiCreator.ItemConfig> registered = new ArrayList<>();
    protected BaseGuiListener listener; // Make the listener flexible
    private GuiType guiType;

    /**
     * Constructs a BaseGui with a specified plugin and a custom GUI listener.
     * <p>
     * If the provided listener is null, a {@link DefaultGuiListener} is used. The
     * listener is automatically registered for GUI events.
     * </p>
     *
     * @param plugin   the JavaPlugin instance for the plugin
     * @param listener the custom GUI listener; may be null to use a default listener
     * @param type     the type of gui this class tends to open
     */
    public BaseGui(JavaPlugin plugin, BaseGuiListener listener, GuiType type) {
        this.plugin = plugin;
        this.listener = listener != null ? listener : new DefaultGuiListener(plugin); // Use provided listener or
        // default one
        this.guiManager = new GuiCreator(plugin, listener);
        this.guiManager.setOwner(this);
        guiType = type;

        // Register the listener for GUI actions
        plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);
    }

    public BaseGui(JavaPlugin plugin, BaseGuiListener listener) {
        this(plugin, listener, GuiType.NORMAL);
    }

    /**
     * Constructs a BaseGui with a specified plugin using the default GUI listener.
     * <p>
     * The default listener is registered for GUI events.
     * </p>
     *
     * @param plugin the JavaPlugin instance for the plugin
     */
    public BaseGui(JavaPlugin plugin) {
        this.plugin = plugin;
        this.listener = new DefaultGuiListener(plugin); // Use default listener
        this.guiManager = new GuiCreator(plugin, listener);

        // Register the listener for GUI actions
        plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);
    }

    void addInventory(UUID key, Inventory inv) {
        this.current.put(key, inv);
    }

    protected Inventory getInventory(UUID key) {
        return current.get(key);
    }

    /**
     * Displays the GUI to the specified player.
     * <p>
     * Each subclass must implement this method to open its particular GUI.
     * </p>
     *
     * @param player the player to whom the GUI is shown
     */
    public abstract void showGui(Player player);

    /**
     * Dynamically sets a new GUI listener for this GUI.
     * <p>
     * Optionally, the current listener can be unregistered before replacing it. The
     * new listener is then registered for GUI events.
     * </p>
     *
     * @param listener the new {@link BaseGuiListener} to set
     */
    public void setListener(BaseGuiListener listener) {
        if (this.listener != null)
            HandlerList.unregisterAll(this.listener);
        this.listener = listener;
        plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);
    }

    protected void registerItem(GuiCreator.ItemConfig config) {
        this.registered.add(config);
    }

    protected void registerItems(GuiCreator.ItemConfig... config) {
        this.registered.addAll(Arrays.asList(config));
    }

    protected void registerItems(List<GuiCreator.ItemConfig> config) {
        this.registered.addAll(config);
    }

    protected CompletableFuture<Inventory> buildInventory(Player player) {
        CompletableFuture<Inventory> futureInventory = new CompletableFuture<>();
        Inventory inventory = Bukkit.createInventory(new GuiCreator.GUIHolder(), 54, "");

        // Run the inventory population in a separate thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            for (var itemConfig : registered) {
                Set<Integer> slotSet = GuiCreator.parseSlots(itemConfig.getSlotRange());
                for (int slot : slotSet) {
                    if (slot < 54) {
                        ItemStack item = createItem(itemConfig, player);
                        inventory.setItem(slot, item);
                    }
                }
            }
            // Complete the future with the populated inventory
            futureInventory.complete(inventory);
        });

        return futureInventory; // Return the CompletableFuture
    }

    public List<GuiCreator.ItemConfig> getRegistered() {
        return registered;
    }

    public enum GuiType {
        PAGED, NORMAL, ANVIL
    }
}
