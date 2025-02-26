/* Licensed under Apache-2.0 2024. */
package org.vicky.guiparent;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import java.util.*;
import java.util.function.BiFunction;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.listeners.BaseGuiListener;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.apis.DatabasePlayerAPI;
import org.vicky.utilities.DatabaseManager.dao_s.DatabasePlayerDAO;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;

/**
 * GuiCreator is a utility class responsible for creating and opening both GUI inventories
 * and anvil GUIs for players.
 * <p>
 * It leverages various APIs including ItemsAdder and AnvilGUI to build custom GUIs.
 * </p>
 */
public class GuiCreator {

  private final JavaPlugin plugin;
  private final BaseGuiListener listener;
  private final ContextLogger logger = new ContextLogger(ContextLogger.ContextType.SYSTEM, "GUI");

  // Flag indicating if a textured GUI can be created.
  boolean canBeMade = false;

  /**
   * Constructs a new GuiCreator with the specified plugin and GUI listener.
   *
   * @param plugin   The JavaPlugin instance for the plugin.
   * @param listener The BaseGuiListener responsible for handling GUI events.
   */
  public GuiCreator(JavaPlugin plugin, BaseGuiListener listener) {
    this.plugin = plugin;
    this.listener = listener;
  }

  /**
   * Opens a custom GUI inventory for the given player.
   * <p>
   * The GUI is created based on the specified dimensions, title, texture settings, and item configurations.
   * </p>
   *
   * @param player      The player who will see the GUI.
   * @param height      The number of rows in the GUI inventory (must be >= 1).
   * @param width       The number of columns in the GUI inventory (must be between 1 and 9).
   * @param title       The title of the GUI inventory.
   * @param textured    Whether to use a texture for the inventory background.
   * @param textureKey  The key for the texture if textured is true.
   * @param offset      The vertical offset for the texture application.
   * @param itemConfigs Varargs of ItemConfig objects that define items in the GUI (e.g., positions, names, lore, etc.).
   */
  public void openGUI(
          Player player,
          int height,
          int width,
          String title,
          boolean textured,
          String textureKey,
          int offset,
          ItemConfig... itemConfigs) {

    if (height < 1 || width < 1 || width > 9) {
      player.sendMessage("Invalid dimensions for the GUI.");
      return;
    }

    int slots = height * width;
    Plugin itemsAdder = Bukkit.getPluginManager().getPlugin("ItemsAdder");
    Inventory inventory;

    // Create an inventory based on texture availability.
    if (textured && textureKey != null && !textureKey.isEmpty() && itemsAdder != null) {
      FontImageWrapper texture = new FontImageWrapper(textureKey);
      if (!texture.exists()) {
        logger.printBukkit("Gui Texture: " + textureKey + " Does not exist", ContextLogger.LogType.WARNING, true);
        inventory = Bukkit.createInventory(new GUIHolder(), slots, title);
      } else {
        inventory = Bukkit.createInventory(new GUIHolder(), slots, title);
        canBeMade = true;
      }
    } else {
      inventory = Bukkit.createInventory(new GUIHolder(), slots, title);
    }

    // Populate the inventory with items based on the provided ItemConfigs.
    for (ItemConfig itemConfig : itemConfigs) {
      Set<Integer> slotSet = parseSlots(itemConfig.getSlotRange(), width);
      for (int slot : slotSet) {
        if (slot < slots) {
          ItemStack item = createItem(itemConfig, player);
          inventory.setItem(slot, item);
        }
      }
    }

    // Open the inventory and apply texture if available.
    listener.setGuiInventory(inventory);
    player.openInventory(inventory);
    if (canBeMade) {
      FontImageWrapper texture = new FontImageWrapper(textureKey);
      TexturedInventoryWrapper.setPlayerInventoryTexture(player, texture, title, 0, offset);
    }
  }

  public enum ArrowGap {
    SMALL(1),
    MEDIUM(2),
    WIDE(3);

    public final int gap;
    ArrowGap(int gap) {
      this.gap = gap;
    }
  }

  /**
   * Opens a paginated GUI for the given player with custom item configurations, a starting slot, and texture support.
   *
   * @param player        The player who will see the GUI.
   * @param itemConfigs   The list of ItemConfig objects to display.
   * @param page          The current page number.
   * @param itemsPerPage  The number of items per page.
   * @param title         The title of the GUI.
   * @param startingSlot  The slot index (0-based) where the items should start appearing.
   * @param textured      Whether the GUI should be textured.
   * @param textureKey    The key for the texture if textured is true.
   * @param offset        The vertical offset for the texture.
   */
  public void openPaginatedGUI(
          Player player,
          int height,
          ArrowGap spacing,
          List<GuiCreator.ItemConfig> itemConfigs,
          int page,
          int itemsPerPage,
          String title,
          int startingSlot,
          boolean textured,
          String textureKey,
          int offset) {

    DatabasePlayer player1 = new DatabasePlayerDAO().findById(player.getUniqueId());
    String themeId = player1.getUserTheme();

    int totalItems = itemConfigs.size();
    int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
    page = Math.max(1, Math.min(page, totalPages));

    int startIndex = (page - 1) * itemsPerPage;
    int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

    // Total slots based on height (e.g., height * 9)
    int slots = 9 * height;
    Inventory inventory = Bukkit.createInventory(new GUIHolder(), slots, title);

    // Populate page items starting at the specified startingSlot.
    int displaySlot = startingSlot;
    for (int i = startIndex; i < endIndex; i++) {
      GuiCreator.ItemConfig config = itemConfigs.get(i);
      ItemStack item = createItem(config, player);
      if (displaySlot < slots) {
        inventory.setItem(displaySlot++, item);
      }
    }

    // Calculate center slot of the bottom row.
    int centerSlot = ((height - 1) * 9) + 4;
    // Determine positions for navigation buttons based on the spacing gap.
    int prevSlot = Math.max(0, centerSlot - spacing.gap);
    int nextSlot = Math.min(slots - 1, centerSlot + spacing.gap);

    // Create navigation buttons using ItemConfig and ButtonActions:
    if (page > 1) {
      GuiCreator.ItemConfig prevConfig = new GuiCreator.ItemConfig(
              Material.ARROW,
              "ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ (" + (page - 1) + ")",
              Integer.toString(prevSlot),
              true,
              null,
              "vicky_themes:left_arrow_" + themeId,
              List.of(ChatColor.GREEN + "ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ᴛᴏ ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ")
      );
      // Register a ButtonAction that reopens the GUI at page-1.
      int finalPage = page;
      listener.registerButton(
              ButtonAction.ofRunCode(p -> openPaginatedGUI(
                      player, height, spacing, itemConfigs, finalPage - 1, itemsPerPage, title, startingSlot, textured, textureKey, offset
              ), true),
              prevConfig
      );
      inventory.setItem(prevSlot, createItem(prevConfig, player));
    }
    else {
      GuiCreator.ItemConfig prevConfig = new GuiCreator.ItemConfig(
              Material.ARROW,
              "ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ (" + (page - 1) + ")",
              Integer.toString(prevSlot),
              true,
              null,
              "vicky_themes:left_arrow_" + themeId + "_disabled",
              List.of(ChatColor.GREEN + "ᴛʜɪs ɪs ᴛʜᴇ ғɪʀsᴛ ᴘᴀɢᴇ")
      );
      inventory.setItem(prevSlot, createItem(prevConfig, player));
    }
    if (page < totalPages) {
      GuiCreator.ItemConfig nextConfig = new GuiCreator.ItemConfig(
              Material.ARROW,
              "ɴᴇxᴛ ᴘᴀɢᴇ (" + (page + 1) + ")",
              Integer.toString(nextSlot),
              true,
              null,
              "vicky_themes:right_arrow_" + themeId,
              List.of(ChatColor.GREEN + "ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ᴛᴏ ɴᴇxᴛ ᴘᴀɢᴇ")
      );
      int finalPage1 = page;
      listener.registerButton(
              ButtonAction.ofRunCode(p -> openPaginatedGUI(
                      player, height, spacing, itemConfigs, finalPage1 + 1, itemsPerPage, title, startingSlot, textured, textureKey, offset
              ), true),
              nextConfig
      );
      inventory.setItem(nextSlot, createItem(nextConfig, player));
    }
    else {
      GuiCreator.ItemConfig prevConfig = new GuiCreator.ItemConfig(
              Material.ARROW,
              "ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ (" + (page - 1) + ")",
              Integer.toString(prevSlot),
              true,
              null,
              "vicky_themes:right_arrow_" + themeId + "_disabled",
              List.of(ChatColor.GREEN + "ᴛʜɪs ɪs ᴛʜᴇ ʟᴀsᴛ ᴘᴀɢᴇ")
      );
      inventory.setItem(prevSlot, createItem(prevConfig, player));
    }

    // Set the inventory in the listener and open it.
    listener.setGuiInventory(inventory);
    player.openInventory(inventory);

    // Apply texture if available.
    if (textured && textureKey != null && !textureKey.isEmpty()) {
      FontImageWrapper texture = new FontImageWrapper(textureKey);
      if (texture.exists()) {
        TexturedInventoryWrapper.setPlayerInventoryTexture(player, texture, title, 0, offset);
      } else {
        logger.printBukkit("Gui Texture: " + textureKey + " does not exist", ContextLogger.LogType.WARNING);
      }
    }
  }

  /**
   * Opens an anvil GUI for the given player with custom item configurations and a completion action.
   *
   * @param player           The player who will interact with the anvil GUI.
   * @param initialText      The initial text to display in the anvil GUI's input field.
   * @param leftItemConfig   The ItemConfig for the left input slot (can be null).
   * @param rightItemConfig  The ItemConfig for the right input slot (can be null).
   * @param outputItemConfig The ItemConfig for the output slot (can be null).
   * @param title            The title of the anvil GUI window.
   * @param canClickLeft     If true, the left slot is intractable.
   * @param canClickRight    If true, the right slot is intractable.
   * @param completionAction A BiFunction that takes a Player and a String (the input text) and returns a list of AnvilGUI.ResponseAction to determine the anvil GUI's behavior upon a click.
   */
  public void openAnvilGUI(
          Player player,
          String initialText,
          ItemConfig leftItemConfig,
          ItemConfig rightItemConfig,
          ItemConfig outputItemConfig,
          String title,
          boolean canClickLeft,
          boolean canClickRight,
          BiFunction<Player, String, List<AnvilGUI.ResponseAction>> completionAction) {
    ItemStack leftItem = null;
    ItemStack rightItem = null;
    ItemStack outputItem = null;

    if (leftItemConfig != null) {
      leftItem = createItem(leftItemConfig, player);
    }
    if (rightItemConfig != null) {
      rightItem = createItem(rightItemConfig, player);
    }
    if (outputItemConfig != null) {
      outputItem = createItem(outputItemConfig, player); // Generate ItemStack for output slot
    }

    AnvilGUI.Builder builder = new AnvilGUI.Builder()
            .plugin(plugin)
            .text(initialText)
            .title(title)
            .onClick((slot, stateSnapshot) -> {
              String clickedText = stateSnapshot.getText();
              if (slot == AnvilGUI.Slot.OUTPUT) {
                return completionAction.apply(player, clickedText);
              } else if (slot == AnvilGUI.Slot.INPUT_LEFT) {
                return completionAction.apply(player, leftItemConfig != null ? leftItemConfig.getName() : clickedText);
              }
              return Arrays.asList(AnvilGUI.ResponseAction.close());
            });

    if (canClickLeft) {
      builder.interactableSlots(AnvilGUI.Slot.INPUT_LEFT);
    } else if (canClickRight) {
      builder.interactableSlots(AnvilGUI.Slot.INPUT_RIGHT);
    }

    if (rightItemConfig != null) {
      builder.itemRight(rightItem);
    }
    if (leftItemConfig != null) {
      builder.itemLeft(leftItem);
    }
    if (outputItemConfig != null) {
      builder.itemOutput(outputItem);
    }

    builder.open(player);
  }

  /**
   * Parses a slot range string into a set of zero-based slot indices.
   * <p>
   * The slotRange string can be a comma-separated list of numbers or ranges (e.g., "1-10,12,15-18").
   * This method converts the values to zero-based indices and ensures they are within valid bounds.
   * </p>
   *
   * @param slotRange The slot range string to parse.
   * @param width     The width (number of columns) of the GUI for boundary checking.
   * @return A set of integer slot indices.
   */
  private Set<Integer> parseSlots(String slotRange, int width) {
    Set<Integer> slots = new HashSet<>();
    String[] parts = slotRange.split(",");
    for (String part : parts) {
      if (part.contains("-")) {
        String[] range = part.split("-");
        int start = Math.max(0, Integer.parseInt(range[0]) - 1);
        int end = Math.min(width * 9, Integer.parseInt(range[1]) - 1);
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
   * Creates an ItemStack based on the provided ItemConfig and player context.
   * <p>
   * If the item is configured as an ItemsAdder item, this method attempts to create it via the ItemsAdder API.
   * For player head items, the owner is set accordingly. Otherwise, a regular item is created.
   * </p>
   *
   * @param itemConfig The configuration for the item.
   * @param player     The player context (used for setting head owner, etc.).
   * @return An ItemStack created according to the configuration.
   */
  private ItemStack createItem(ItemConfig itemConfig, Player player) {
    ItemStack item;
    // If it's an ItemsAdder item, create it using the ItemsAdder API
    if (itemConfig.isItemsAdderItem()) {
      CustomStack customItem = CustomStack.getInstance(itemConfig.getItemsAdderName());
      if (customItem != null) {
        ItemMeta meta = customItem.getItemStack().getItemMeta();
        // Check if the item is a player head
        if (customItem.getItemStack().getType() == Material.PLAYER_HEAD) {
          if (meta instanceof org.bukkit.inventory.meta.SkullMeta) {
            org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) meta;
            if (itemConfig.getHeadOwner() == null) {
              skullMeta.setOwningPlayer(player);
            } else {
              skullMeta.setOwningPlayer(itemConfig.getHeadOwner()); // Set the head to the specified owner
            }
          }
        }
        // Set display name and lore as usual
        if (meta != null) {
          meta.setDisplayName(itemConfig.getName());
          meta.setLore(itemConfig.getLore());
        }
        customItem.getItemStack().setItemMeta(meta);
        return customItem.getItemStack();
      } else {
        logger.printBukkit("ItemsAdder item '" + itemConfig.getItemsAdderName() + "' could not be found!", ContextLogger.LogType.ERROR, true);
      }
    }
    // Fallback: create a regular item
    item = new ItemStack(itemConfig.getMaterial());
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(itemConfig.getName());
      meta.setLore(itemConfig.getLore());
      // Special case: set player head meta if it's a regular player head item
      if (item.getType() == Material.PLAYER_HEAD) {
        if (meta instanceof org.bukkit.inventory.meta.SkullMeta) {
          org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) meta;
          if (itemConfig.getHeadOwner() == null) {
            skullMeta.setOwningPlayer(player);
          } else {
            skullMeta.setOwningPlayer(itemConfig.getHeadOwner()); // Set the head to the specified owner
          }
        }
      }
      if (itemConfig.getCustomModelData() != null) {
        meta.setCustomModelData(itemConfig.getCustomModelData());
      }
      item.setItemMeta(meta);
    }
    return item;
  }

  /**
   * GUIHolder is a simple InventoryHolder implementation used for creating GUI inventories.
   * <p>
   * In this implementation, getInventory() returns null as it is not used.
   * </p>
   */
  private static class GUIHolder implements InventoryHolder {
    /**
     * Returns the inventory associated with this holder.
     * <p>
     * Not implemented for this holder; returns null.
     * </p>
     *
     * @return null
     */
    @Override
    public Inventory getInventory() {
      return null; // Not implemented
    }
  }

  /**
   * ItemConfig encapsulates configuration details for an item in a GUI, including material, name, slot range,
   * custom model data, lore, and an optional ItemsAdder item name or head owner.
   * <p>
   * Two constructors are provided: one for regular items and one for player head items.
   * </p>
   */
  public static class ItemConfig {
    private final Material material;
    private final String name;
    private final String slotRange; // e.g., "1-10" or "1,8"
    private final boolean clickable;
    private final Integer customModelData; // Custom model data (optional)
    private final String itemsAdderName; // ItemsAdder item name (optional)
    private final List<String> lore; // Lore for the item
    private final OfflinePlayer headOwner; // Head owner for player head items (optional)

    /**
     * Constructor for regular items.
     *
     * @param material        The material for the item
     * @param name            The display name for the item
     * @param slotRange       The slot range as a string (e.g., "1-10" or "1,8")
     * @param clickable       Whether the item is clickable
     * @param customModelData Custom model data for the item (can be null)
     * @param itemsAdderName  The ItemsAdder item name (optional, may be null)
     * @param lore            A list of strings representing the lore for the item
     */
    public ItemConfig(
            Material material,
            String name,
            String slotRange,
            boolean clickable,
            Integer customModelData,
            String itemsAdderName,
            List<String> lore) {
      this.material = material;
      this.name = name;
      this.slotRange = slotRange;
      this.clickable = clickable;
      this.customModelData = customModelData;
      this.itemsAdderName = itemsAdderName;
      this.lore = lore; // Assign lore to the item config
      this.headOwner = null; // Default to null for non-head items
    }

    /**
     * Constructor for player head items.
     *
     * @param material        The material for the item (typically PLAYER_HEAD)
     * @param name            The display name for the item
     * @param slotRange       The slot range as a string
     * @param clickable       Whether the item is clickable
     * @param customModelData Custom model data for the item (optional)
     * @param itemsAdderName  The ItemsAdder item name (optional)
     * @param lore            A list of strings representing the lore for the item
     * @param headOwner       The OfflinePlayer representing the head owner
     */
    public ItemConfig(
            Material material,
            String name,
            String slotRange,
            boolean clickable,
            Integer customModelData,
            String itemsAdderName,
            List<String> lore,
            OfflinePlayer headOwner) {
      this.material = material;
      this.name = name;
      this.slotRange = slotRange;
      this.clickable = clickable;
      this.customModelData = customModelData;
      this.itemsAdderName = itemsAdderName;
      this.lore = lore;
      this.headOwner = headOwner; // Set the head owner's player
    }

    /**
     * Gets the material for this item.
     *
     * @return the Material of the item
     */
    public Material getMaterial() {
      return material;
    }

    /**
     * Gets the display name for this item.
     *
     * @return the name of the item
     */
    public String getName() {
      return name;
    }

    /**
     * Gets the slot range string for this item.
     *
     * @return the slot range (e.g., "1-10" or "1,8")
     */
    public String getSlotRange() {
      return slotRange;
    }

    /**
     * Gets the custom model data for this item.
     *
     * @return the custom model data, or null if not set
     */
    public Integer getCustomModelData() {
      return customModelData;
    }

    /**
     * Checks if this item is clickable.
     *
     * @return true if the item is clickable, false otherwise
     */
    public boolean isClickable() {
      return clickable;
    }

    /**
     * Checks if this item is configured as an ItemsAdder item.
     *
     * @return true if an ItemsAdder item name is provided, false otherwise
     */
    public boolean isItemsAdderItem() {
      return itemsAdderName != null;
    }

    /**
     * Gets the ItemsAdder item name for this item.
     *
     * @return the ItemsAdder item name
     */
    public String getItemsAdderName() {
      return itemsAdderName;
    }

    /**
     * Gets the lore for this item.
     *
     * @return a list of strings representing the item's lore
     */
    public List<String> getLore() {
      return lore;
    }

    /**
     * Gets the head owner for this item, if applicable.
     *
     * @return the OfflinePlayer representing the head owner, or null if not applicable
     */
    public OfflinePlayer getHeadOwner() {
      return headOwner;
    }
  }
}