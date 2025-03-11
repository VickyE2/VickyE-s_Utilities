/* Licensed under Apache-2.0 2024. */
package org.vicky.guiparent;

import dev.lone.LoneLibs.nbt.nbtapi.NBT;
import dev.lone.LoneLibs.nbt.nbtapi.NBTCompound;
import dev.lone.LoneLibs.nbt.nbtapi.NBTItem;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.listeners.BaseGuiListener;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.dao_s.DatabasePlayerDAO;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;

/**
 * GuiCreator is a utility class responsible for creating and opening both GUI inventories
 * and anvil GUIs for players.
 * <p>
 * It leverages various APIs including ItemsAdder and AnvilGUI to build custom GUIs.
 * </p>
 */
@SuppressWarnings("deprecation")
public class GuiCreator {

  private static final ContextLogger logger =
      new ContextLogger(ContextLogger.ContextType.SYSTEM, "GUI");
  private final BaseGuiListener listener;
  private final JavaPlugin plugin;
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
  public static Set<Integer> parseSlots(String slotRange, int width) {
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
  public static ItemStack createItem(ItemConfig itemConfig, Player player, JavaPlugin plugin) {
    if (itemConfig.isItemsAdderItem()) {
      CustomStack customItem = CustomStack.getInstance(itemConfig.getItemsAdderName());
      if (customItem != null) {
        ItemStack customStack = customItem.getItemStack();
        // Apply NBT changes first.
        if (!itemConfig.getNBTData().isEmpty()) {
          NBT.modify(
              customStack,
              nbt -> {
                for (Map.Entry<String, Object> entry : itemConfig.getNBTData().entrySet()) {
                  setNBTValue((NBTCompound) nbt, entry.getKey(), entry.getValue());
                }
              });
        }
        // Now take the ItemMeta snapshot AFTER the NBT modifications.
        ItemMeta meta = customStack.getItemMeta();
        if (meta != null) {
          meta.setDisplayName(itemConfig.getName());
          meta.setLore(itemConfig.getLore());
          // Special handling for player heads:
          if (customStack.getType() == Material.PLAYER_HEAD
              && meta instanceof SkullMeta skullMeta) {
            if (itemConfig.getHeadOwner() == null) {
              skullMeta.setOwningPlayer(player);
            } else {
              skullMeta.setOwningPlayer(itemConfig.getHeadOwner());
            }
          }
          customStack.setItemMeta(meta);
        }
        return customStack;
      } else {
        logger.printBukkit(
            "ItemsAdder item '" + itemConfig.getItemsAdderName() + "' could not be found!",
            ContextLogger.LogType.ERROR,
            true);
        throw new RuntimeException("ItemsAdder item not found");
      }
    }

    // Fallback: Create a regular item.
    ItemStack item = new ItemStack(itemConfig.getMaterial());

    // First, modify the item's NBT data.
    if (!itemConfig.getNBTData().isEmpty()) {
      NBT.modify(
          item,
          nbt -> {
            for (Map.Entry<String, Object> entry : itemConfig.getNBTData().entrySet()) {
              setNBTValue((NBTCompound) nbt, entry.getKey(), entry.getValue());
            }
          });
    }

    // Then retrieve the ItemMeta after NBT modifications.
    ItemMeta meta = item.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(itemConfig.getName());
      meta.setLore(itemConfig.getLore());
      // If it's a player head, set the head owner.
      if (item.getType() == Material.PLAYER_HEAD && meta instanceof SkullMeta skullMeta) {
        if (itemConfig.getHeadOwner() == null) {
          skullMeta.setOwningPlayer(player);
        } else {
          skullMeta.setOwningPlayer(itemConfig.getHeadOwner());
        }
      }
      // Set custom model data if provided.
      if (itemConfig.getCustomModelData() != null) {
        meta.setCustomModelData(itemConfig.getCustomModelData());
      }
      item.setItemMeta(meta);
    }
    return item;
  }

  /**
   * Helper method to set a value on the NBT compound based on the string value.
   * It tries boolean, then integer, then double, and falls back to string.
   *
   * @param nbt   the NBT compound to modify
   * @param key   the key to set
   * @param value the string value from the configuration
   */
  private static void setNBTValue(NBTCompound nbt, String key, Object value) {
    if (value instanceof Boolean bool) nbt.setBoolean(key, bool);
    else if (value instanceof UUID uuid) nbt.setUUID(key, uuid);
    else if (value instanceof Integer integer) nbt.setInteger(key, integer);
    else if (value instanceof Enum<?> enumerated) nbt.setEnum(key, enumerated);
    else if (value instanceof Double doublee) nbt.setDouble(key, doublee);
    else if (value instanceof String str) nbt.setString(key, str);
    else if (value instanceof ItemStack stack) nbt.setItemStack(key, stack);
    else if (value instanceof Float floater) nbt.setFloat(key, floater);
  }

  /**
   * Extracts an ItemConfig from the given ItemStack.
   *
   * @param item   the ItemStack to convert; if null, null is returned
   * @param plugin the JavaPlugin instance, used for creating NamespacedKeys and accessing ItemsAdder API
   * @return an ItemConfig populated with the extracted data from the ItemStack, or null if the item is null
   */
  public static ItemConfig fromItemStack(ItemStack item, JavaPlugin plugin) {
    if (item == null) {
      return null;
    }

    // Material is obtained directly from the ItemStack.
    Material material = item.getType();

    // Retrieve ItemMeta, if present.
    ItemMeta meta = item.getItemMeta();
    String name = "";
    List<String> lore = new ArrayList<>();
    Integer customModelData = null;
    OfflinePlayer headOwner = null;
    Map<String, Object> nbtData = new HashMap<>();

    if (meta != null) {
      // Display name
      if (meta.hasDisplayName()) {
        name = meta.getDisplayName();
      }
      // Lore
      if (meta.hasLore()) {
        lore = meta.getLore();
      }
      // Custom model data
      customModelData = meta.getCustomModelData();

      // If the item is a player head, extract the owning player.
      if (material == Material.PLAYER_HEAD && meta instanceof SkullMeta) {
        headOwner = ((SkullMeta) meta).getOwningPlayer();
      }

      // Extract NBT data from the PersistentDataContainer.
      for (NamespacedKey key : meta.getPersistentDataContainer().getKeys()) {
        String value = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (value != null) {
          nbtData.put(key.getKey(), value);
        }
      }
    }

    // Attempt to get ItemsAdder data if available.
    String itemsAdderName = null;
    CustomStack customStack = CustomStack.byItemStack(item);
    if (customStack != null) {
      itemsAdderName = customStack.getNamespacedID();
    }

    // The slotRange and clickable properties are typically provided by the GUI configuration,
    // and are not stored in the ItemStack. We set them to default values.
    String slotRange = ""; // Default: empty string (or you could choose a specific default)
    boolean clickable = false; // Default: false

    // Create and return the ItemConfig. This constructor must match your ItemConfig class.
    return new ItemConfig(
        material,
        name,
        slotRange,
        clickable,
        customModelData,
        itemsAdderName,
        lore,
        headOwner,
        nbtData,
        null);
  }

  /**
   * Retrieves NBT data from the given ItemStack using the NBT API.
   * Supported types: UUID, Integer, Double, String, ItemStack, and Enum.
   *
   * @param item   the ItemStack from which to retrieve NBT data
   * @param nbtKey the key for the NBT tag
   * @param clazz  the Class representing the expected type
   * @param <T>    the expected type of the NBT data
   * @return the NBT data cast to type T, or null if the tag is not present
   * @throws IllegalArgumentException if the type is unsupported
   */
  public static <T> T getNBTData(ItemStack item, String nbtKey, Class<T> clazz) {
    if (item == null) return null;
    NBTItem nbtItem = new NBTItem(item);
    if (!nbtItem.hasKey(nbtKey)) return null;

    Object result;
    if (clazz.equals(UUID.class)) {
      result = nbtItem.getUUID(nbtKey);
    } else if (clazz.equals(Integer.class)) {
      result = nbtItem.getInteger(nbtKey);
    } else if (clazz.equals(Double.class)) {
      result = nbtItem.getDouble(nbtKey);
    } else if (clazz.equals(String.class)) {
      result = nbtItem.getString(nbtKey);
    } else if (clazz.equals(ItemStack.class)) {
      result = nbtItem.getItemStack(nbtKey);
    } else if (clazz.isEnum()) {
      @SuppressWarnings("unchecked")
      Class<? extends Enum> enumClass = (Class<? extends Enum>) clazz;
      result = nbtItem.getEnum(nbtKey, enumClass);
    } else {
      throw new IllegalArgumentException("Unsupported NBT type: " + clazz.getName());
    }
    return clazz.cast(result);
  }

  /**
   * Checks if the given ItemStack contains custom NBT data for the specified key using a key derived from the ItemConfig's plugin.
   * <p>
   * This method creates a {@link NamespacedKey} using the plugin obtained from
   * {@link JavaPlugin#getProvidingPlugin(Class)} for the ItemConfig class and checks the item's
   * {@link org.bukkit.persistence.PersistentDataContainer} for a STRING value.
   * </p>
   *
   * @param item   the ItemStack to check; if null or lacking ItemMeta, false is returned
   * @param nbtKey the custom NBT key to check for
   * @return true if the item contains a STRING value for the given key, false otherwise
   */
  public static boolean hasNBTData(ItemStack item, String nbtKey) {
    if (item == null) return false;
    NBTItem nbtItem = new NBTItem(item);
    return nbtItem.hasKey(nbtKey);
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
        logger.printBukkit(
            "Gui Texture: " + textureKey + " Does not exist", ContextLogger.LogType.WARNING, true);
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
          ItemStack item = createItem(itemConfig, player, plugin);
          inventory.setItem(slot, item);
          if (itemConfig.buttonAction != null) listener.registerButton(inventory, itemConfig);
        }
      }
    }

    // Open the inventory and apply texture if available.
    listener.addGuiInventory(inventory);
    player.openInventory(inventory);
    if (canBeMade) {
      FontImageWrapper texture = new FontImageWrapper(textureKey);
      TexturedInventoryWrapper.setPlayerInventoryTexture(player, texture, title, 0, offset);
    }
  }

  /**
   * Opens a paginated GUI for the given player with custom item configurations, a starting slot, and texture support.
   *
   * @param player       The player who will see the GUI.
   * @param itemConfigs  The list of ItemConfig objects to display.
   * @param page         The current page number.
   * @param itemsPerPage The number of items per page.
   * @param title        The title of the GUI.
   * @param startingSlot The slot index (0-based) where the items should start appearing.
   * @param textured     Whether the GUI should be textured.
   * @param textureKey   The key for the texture if textured is true.
   * @param offset       The vertical offset for the texture.
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

    Optional<DatabasePlayer> opt = new DatabasePlayerDAO().findById(player.getUniqueId());
    if (opt.isEmpty()) {
      throw new RuntimeException("Player that made request cannot be found on the database");
    }
    DatabasePlayer player1 = opt.get();
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
      ItemStack item = createItem(config, player, plugin);
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
      int finalPage = page;
      GuiCreator.ItemConfig prevConfig =
          new GuiCreator.ItemConfig(
              Material.ARROW,
              "ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ (" + (page - 1) + ")",
              Integer.toString(prevSlot),
              true,
              null,
              "vicky_themes:left_arrow_" + themeId,
              List.of(ChatColor.GREEN + "ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ᴛᴏ ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ"),
              ButtonAction.ofRunCode(
                  p ->
                      openPaginatedGUI(
                          player,
                          height,
                          spacing,
                          itemConfigs,
                          finalPage - 1,
                          itemsPerPage,
                          title,
                          startingSlot,
                          textured,
                          textureKey,
                          offset),
                  true));
      // Register a ButtonAction that reopens the GUI at page-1.
      inventory.setItem(prevSlot, createItem(prevConfig, player, plugin));
    } else {
      GuiCreator.ItemConfig prevConfig =
          new GuiCreator.ItemConfig(
              Material.ARROW,
              "ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ (" + (0) + ")",
              Integer.toString(prevSlot),
              true,
              null,
              "vicky_themes:left_arrow_" + themeId + "_disabled",
              List.of(ChatColor.GREEN + "ᴛʜɪs ɪs ᴛʜᴇ ғɪʀsᴛ ᴘᴀɢᴇ"),
              null);
      inventory.setItem(prevSlot, createItem(prevConfig, player, plugin));
    }
    if (page < totalPages) {
      int finalPage1 = page;
      GuiCreator.ItemConfig nextConfig =
          new GuiCreator.ItemConfig(
              Material.ARROW,
              "ɴᴇxᴛ ᴘᴀɢᴇ (" + (page + 1) + ")",
              Integer.toString(nextSlot),
              true,
              null,
              "vicky_themes:right_arrow_" + themeId,
              List.of(ChatColor.GREEN + "ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ᴛᴏ ɴᴇxᴛ ᴘᴀɢᴇ"),
              ButtonAction.ofRunCode(
                  p ->
                      openPaginatedGUI(
                          player,
                          height,
                          spacing,
                          itemConfigs,
                          finalPage1 + 1,
                          itemsPerPage,
                          title,
                          startingSlot,
                          textured,
                          textureKey,
                          offset),
                  true));
      inventory.setItem(nextSlot, createItem(nextConfig, player, plugin));
    } else {
      GuiCreator.ItemConfig prevConfig =
          new GuiCreator.ItemConfig(
              Material.ARROW,
              "ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ (" + (page - 1) + ")",
              Integer.toString(prevSlot),
              true,
              null,
              "vicky_themes:right_arrow_" + themeId + "_disabled",
              List.of(ChatColor.GREEN + "ᴛʜɪs ɪs ᴛʜᴇ ʟᴀsᴛ ᴘᴀɢᴇ"),
              null);
      inventory.setItem(prevSlot, createItem(prevConfig, player, plugin));
    }

    // Set the inventory in the listener and open it.
    listener.addGuiInventory(inventory);
    player.openInventory(inventory);

    // Apply texture if available.
    if (textured && textureKey != null && !textureKey.isEmpty()) {
      FontImageWrapper texture = new FontImageWrapper(textureKey);
      if (texture.exists()) {
        TexturedInventoryWrapper.setPlayerInventoryTexture(player, texture, title, 0, offset);
      } else {
        logger.printBukkit(
            "Gui Texture: " + textureKey + " does not exist", ContextLogger.LogType.WARNING);
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
      leftItem = createItem(leftItemConfig, player, plugin);
    }
    if (rightItemConfig != null) {
      rightItem = createItem(rightItemConfig, player, plugin);
    }
    if (outputItemConfig != null) {
      outputItem =
          createItem(outputItemConfig, player, plugin); // Generate ItemStack for output slot
    }

    AnvilGUI.Builder builder =
        new AnvilGUI.Builder()
            .plugin(plugin)
            .text(initialText)
            .title(title)
            .onClick(
                (slot, stateSnapshot) -> {
                  String clickedText = stateSnapshot.getText();
                  if (slot == AnvilGUI.Slot.OUTPUT) {
                    return completionAction.apply(player, clickedText);
                  } else if (slot == AnvilGUI.Slot.INPUT_LEFT) {
                    return completionAction.apply(
                        player, leftItemConfig != null ? leftItemConfig.getName() : clickedText);
                  }
                  return List.of(AnvilGUI.ResponseAction.close());
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

  public enum Rarity {
    COMMON(ChatColor.WHITE, ChatColor.GRAY),
    UNCOMMON(ChatColor.GREEN, ChatColor.DARK_GREEN),
    RARE(ChatColor.BLUE, ChatColor.DARK_BLUE),
    EPIC(ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE),
    LEGENDARY(ChatColor.GOLD, ChatColor.YELLOW),
    MYTHICAL(ChatColor.DARK_RED, ChatColor.RED),
    GOD_LIKE(ChatColor.AQUA, ChatColor.LIGHT_PURPLE);

    private final List<ChatColor> colors;

    Rarity(ChatColor... color) {
      this.colors = new ArrayList<>(List.of(color));
    }

    public String getColor() {
      return colors.stream()
          .map(item -> item.name().toLowerCase())
          .collect(Collectors.joining("-"));
    }

    public String getSimpleColor() {
      return colors.get(0).name().toLowerCase();
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
    private final Integer customModelData; // Custom model data (optional)
    private final String itemsAdderName; // ItemsAdder item name (optional)
    private final List<String> lore; // Lore for the item
    private final OfflinePlayer headOwner; // Head owner for player head items (optional)
    private final Map<String, Object> nbtData; // Custom NBT data
    private final ButtonAction buttonAction; // Custom NBT data
    @Nullable private final Rarity rarity; // Custom NBT data
    private String name;
    private String slotRange; // e.g., "1-10" or "1,8"
    private boolean clickable;

    /**
     * Constructor for regular items using a list of lore strings.
     *
     * @param material        The material for the item.
     * @param name            The display name for the item.
     * @param slotRange       The slot range as a string (e.g., "1-10" or "1,8").
     * @param clickable       Whether the item is clickable.
     * @param customModelData Custom model data for the item (can be null).
     * @param itemsAdderName  The ItemsAdder item name (optional, may be null).
     * @param lore            A list of strings representing the lore for the item.
     * @param nbtData         A map of custom NBT keys and values (can be empty or null).
     */
    public ItemConfig(
        @Nullable Material material,
        @NotNull String name,
        @Nullable String slotRange,
        boolean clickable,
        @Nullable Integer customModelData,
        @Nullable String itemsAdderName,
        @NotNull List<String> lore,
        @Nullable Map<String, Object> nbtData,
        @Nullable ButtonAction buttonAction) {
      this.material = material;
      this.name = name;
      this.slotRange = slotRange;
      this.clickable = clickable;
      this.customModelData = customModelData;
      this.itemsAdderName = itemsAdderName;
      this.lore = lore;
      this.rarity = null;
      this.headOwner = null; // Default for non-head items
      this.nbtData = nbtData != null ? nbtData : new HashMap<>();
      this.buttonAction = buttonAction;
    }

    /**
     * Constructor for regular items using a list of lore strings.
     *
     * @param material        The material for the item.
     * @param name            The display name for the item.
     * @param rarity          The item color rarity.
     * @param slotRange       The slot range as a string (e.g., "1-10" or "1,8").
     * @param clickable       Whether the item is clickable.
     * @param customModelData Custom model data for the item (can be null).
     * @param itemsAdderName  The ItemsAdder item name (optional, may be null).
     * @param lore            A list of strings representing the lore for the item.
     * @param nbtData         A map of custom NBT keys and values (can be empty or null).
     */
    public ItemConfig(
        @Nullable Material material,
        @NotNull String name,
        @Nullable Rarity rarity,
        @Nullable String slotRange,
        boolean clickable,
        @Nullable Integer customModelData,
        @Nullable String itemsAdderName,
        @NotNull List<String> lore,
        @Nullable Map<String, Object> nbtData,
        @Nullable ButtonAction buttonAction) {
      this.material = material;
      this.name = name;
      this.rarity = rarity;
      this.slotRange = slotRange;
      this.clickable = clickable;
      this.customModelData = customModelData;
      this.itemsAdderName = itemsAdderName;
      this.lore = lore;
      this.headOwner = null; // Default for non-head items
      this.nbtData = nbtData != null ? nbtData : new HashMap<>();
      this.buttonAction = buttonAction;
    }

    /**
     * Constructor for regular items using a list of lore strings.
     *
     * @param material        The material for the item.
     * @param name            The display name for the item.
     * @param slotRange       The slot range as a string (e.g., "1-10" or "1,8").
     * @param clickable       Whether the item is clickable.
     * @param customModelData Custom model data for the item (can be null).
     * @param itemsAdderName  The ItemsAdder item name (optional, may be null).
     * @param lore            A list of strings representing the lore for the item.
     */
    public ItemConfig(
        @Nullable Material material,
        @NotNull String name,
        @Nullable String slotRange,
        boolean clickable,
        @Nullable Integer customModelData,
        @Nullable String itemsAdderName,
        @NotNull List<String> lore,
        @Nullable ButtonAction buttonAction) {
      this.material = material;
      this.name = name;
      this.slotRange = slotRange;
      this.clickable = clickable;
      this.customModelData = customModelData;
      this.itemsAdderName = itemsAdderName;
      this.lore = lore;
      this.rarity = null;
      this.headOwner = null; // Default for non-head items
      this.nbtData = new HashMap<>();
      this.buttonAction = buttonAction;
    }

    /**
     * Constructor for player head items using a list of lore strings.
     *
     * @param material        The material for the item (typically PLAYER_HEAD).
     * @param name            The display name for the item.
     * @param slotRange       The slot range as a string.
     * @param clickable       Whether the item is clickable.
     * @param customModelData Custom model data for the item (optional).
     * @param itemsAdderName  The ItemsAdder item name (optional).
     * @param lore            A list of strings representing the lore for the item.
     * @param headOwner       The OfflinePlayer representing the head owner.
     * @param nbtData         A map of custom NBT keys and values (can be empty or null).
     */
    public ItemConfig(
        @Nullable Material material,
        @NotNull String name,
        @Nullable String slotRange,
        boolean clickable,
        @Nullable Integer customModelData,
        @Nullable String itemsAdderName,
        @NotNull List<String> lore,
        @NotNull OfflinePlayer headOwner,
        @Nullable Map<String, Object> nbtData,
        @Nullable ButtonAction buttonAction) {
      this.material = material;
      this.name = name;
      this.rarity = null;
      this.slotRange = slotRange;
      this.clickable = clickable;
      this.customModelData = customModelData;
      this.itemsAdderName = itemsAdderName;
      this.lore = lore;
      this.headOwner = headOwner;
      this.nbtData = nbtData != null ? nbtData : new HashMap<>();
      this.buttonAction = buttonAction;
    }

    /**
     * Constructor for player head items using a list of lore strings.
     *
     * @param material        The material for the item (typically PLAYER_HEAD).
     * @param name            The display name for the item.
     * @param slotRange       The slot range as a string.
     * @param clickable       Whether the item is clickable.
     * @param customModelData Custom model data for the item (optional).
     * @param itemsAdderName  The ItemsAdder item name (optional).
     * @param lore            A list of strings representing the lore for the item.
     * @param headOwner       The OfflinePlayer representing the head owner.
     */
    public ItemConfig(
        @Nullable Material material,
        @NotNull String name,
        @Nullable String slotRange,
        boolean clickable,
        @Nullable Integer customModelData,
        @Nullable String itemsAdderName,
        @NotNull List<String> lore,
        @NotNull OfflinePlayer headOwner,
        @Nullable ButtonAction buttonAction) {
      this.material = material;
      this.name = name;
      this.rarity = null;
      this.slotRange = slotRange;
      this.clickable = clickable;
      this.customModelData = customModelData;
      this.itemsAdderName = itemsAdderName;
      this.lore = lore;
      this.headOwner = headOwner;
      this.nbtData = new HashMap<>();
      this.buttonAction = buttonAction;
    }

    private Map<String, Object> getNBTData() {
      return nbtData;
    }

    /**
     * Adds NBT data to an ItemStack with only allowed types.
     *
     * @param key The NBT key.
     * @param <T> The type of the value (restricted to UUID, Integer, Enum, Double, String, Float or ItemStack).
     * @return The modified ItemStack with the added NBT data.
     */
    public <T> ItemConfig addNbtData(String key, AllowedNBTType<T> valueWrapper) {
      T value = valueWrapper.getValue();

      switch (value) {
        case UUID uuid -> nbtData.put(key, uuid);
        case Integer integer -> nbtData.put(key, integer);
        case Enum<?> enumerated -> nbtData.put(key, enumerated);
        case Double doublee -> nbtData.put(key, doublee);
        case String str -> nbtData.put(key, str);
        case ItemStack stack -> nbtData.put(key, stack);
        case Float floater -> nbtData.put(key, floater);
        case null, default ->
            throw new IllegalArgumentException(
                "Unsupported NBT type: " + value.getClass().getSimpleName());
      }

      return this;
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

    @Nullable
    public Rarity getRarity() {
      return rarity;
    }

    public void setName(String name) {
      this.name = name;
    }

    public ButtonAction getButtonAction() {
      return buttonAction;
    }

    /**
     * Gets the slot range string for this item.
     *
     * @return the slot range (e.g., "1-10" or "1,8")
     */
    public String getSlotRange() {
      return slotRange;
    }

    public void setSlotRange(String slotRange) {
      this.slotRange = slotRange;
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

    public void setClickable(boolean clickable) {
      this.clickable = clickable;
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

    /**
     * Marker interface to enforce allowed types at compile-time.
     */
    public interface AllowedNBTType<T> {
      T getValue();
    }
  }

  public static class ItemConfigFactory {

    /**
     * Creates an ItemConfig for regular items using Adventure Components for lore.
     *
     * @param material        The material for the item.
     * @param name            The display name.
     * @param slotRange       The slot range as a string.
     * @param clickable       Whether the item is clickable.
     * @param customModelData The custom model data (optional).
     * @param itemsAdderName  The ItemsAdder item name (optional).
     * @param loreComponents  A list of Adventure Components for the lore.
     * @return a new ItemConfig instance.
     */
    public static ItemConfig fromComponents(
        @Nullable Material material,
        @NotNull String name,
        @Nullable String slotRange,
        boolean clickable,
        @Nullable Integer customModelData,
        @Nullable String itemsAdderName,
        @NotNull List<Component> loreComponents,
        @Nullable Map<String, Object> nbtData,
        @Nullable ButtonAction buttonAction) {
      List<String> lore =
          loreComponents.stream()
              .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
              .collect(Collectors.toList());
      return new ItemConfig(
          material,
          name,
          slotRange,
          clickable,
          customModelData,
          itemsAdderName,
          lore,
          nbtData,
          buttonAction);
    }

    /**
     * Creates an ItemConfig for regular items using Adventure Components for lore.
     *
     * @param material        The material for the item.
     * @param name            The display name.
     * @param slotRange       The slot range as a string.
     * @param clickable       Whether the item is clickable.
     * @param customModelData The custom model data (optional).
     * @param itemsAdderName  The ItemsAdder item name (optional).
     * @param loreComponents  A list of Adventure Components for the lore.
     * @return a new ItemConfig instance.
     */
    public static ItemConfig fromComponents(
        @Nullable Material material,
        @NotNull String name,
        @Nullable Rarity rarity,
        @Nullable String slotRange,
        boolean clickable,
        @Nullable Integer customModelData,
        @Nullable String itemsAdderName,
        @NotNull List<Component> loreComponents,
        @Nullable Map<String, Object> nbtData,
        @Nullable ButtonAction buttonAction) {
      List<String> lore =
          loreComponents.stream()
              .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
              .collect(Collectors.toList());
      return new ItemConfig(
          material,
          name,
          rarity,
          slotRange,
          clickable,
          customModelData,
          itemsAdderName,
          lore,
          nbtData,
          buttonAction);
    }

    /**
     * Creates an ItemConfig for regular items using Adventure Components for lore.
     *
     * @param material        The material for the item.
     * @param name            The display name.
     * @param slotRange       The slot range as a string.
     * @param clickable       Whether the item is clickable.
     * @param customModelData The custom model data (optional).
     * @param itemsAdderName  The ItemsAdder item name (optional).
     * @param loreComponents  A list of Adventure Components for the lore.
     * @return a new ItemConfig instance.
     */
    public static ItemConfig fromComponents(
        @Nullable Material material,
        @NotNull String name,
        @Nullable String slotRange,
        boolean clickable,
        @Nullable Integer customModelData,
        @Nullable String itemsAdderName,
        @Nullable List<Component> loreComponents,
        @Nullable ButtonAction buttonAction) {
      List<String> lore =
          loreComponents.stream()
              .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
              .collect(Collectors.toList());
      return new ItemConfig(
          material,
          name,
          slotRange,
          clickable,
          customModelData,
          itemsAdderName,
          lore,
          buttonAction);
    }

    /**
     * Creates an ItemConfig for player head items using Adventure Components for lore.
     *
     * @param material        The material for the item.
     * @param name            The display name.
     * @param slotRange       The slot range as a string.
     * @param clickable       Whether the item is clickable.
     * @param customModelData The custom model data (optional).
     * @param itemsAdderName  The ItemsAdder item name (optional).
     * @param loreComponents  A list of Adventure Components for the lore.
     * @param headOwner       The OfflinePlayer representing the head owner.
     * @return a new ItemConfig instance.
     */
    public static ItemConfig fromComponents(
        @Nullable Material material,
        @NotNull String name,
        @Nullable String slotRange,
        boolean clickable,
        @Nullable Integer customModelData,
        @Nullable String itemsAdderName,
        List<Component> loreComponents,
        @NotNull OfflinePlayer headOwner,
        @Nullable Map<String, Object> nbtData,
        @Nullable ButtonAction buttonAction) {
      List<String> lore =
          loreComponents.stream()
              .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
              .collect(Collectors.toList());
      return new ItemConfig(
          material,
          name,
          slotRange,
          clickable,
          customModelData,
          itemsAdderName,
          lore,
          headOwner,
          nbtData,
          buttonAction);
    }

    /**
     * Creates an ItemConfig for player head items using Adventure Components for lore.
     *
     * @param material        The material for the item.
     * @param name            The display name.
     * @param slotRange       The slot range as a string.
     * @param clickable       Whether the item is clickable.
     * @param customModelData The custom model data (optional).
     * @param itemsAdderName  The ItemsAdder item name (optional).
     * @param loreComponents  A list of Adventure Components for the lore.
     * @param headOwner       The OfflinePlayer representing the head owner.
     * @return a new ItemConfig instance.
     */
    public static ItemConfig fromComponents(
        @Nullable Material material,
        @NotNull String name,
        @Nullable String slotRange,
        boolean clickable,
        @Nullable Integer customModelData,
        @Nullable String itemsAdderName,
        List<Component> loreComponents,
        @NotNull OfflinePlayer headOwner,
        @Nullable ButtonAction buttonAction) {
      List<String> lore =
          loreComponents.stream()
              .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
              .collect(Collectors.toList());
      return new ItemConfig(
          material,
          name,
          slotRange,
          clickable,
          customModelData,
          itemsAdderName,
          lore,
          headOwner,
          buttonAction);
    }
  }

  public record AllowedUUID(UUID value) implements GuiCreator.ItemConfig.AllowedNBTType<UUID> {
    @Override
    public UUID getValue() {
      return value;
    }
  }

  public record AllowedInteger(Integer value)
      implements GuiCreator.ItemConfig.AllowedNBTType<Integer> {
    @Override
    public Integer getValue() {
      return value;
    }
  }

  public record AllowedEnum<E extends Enum<E>>(E value)
      implements GuiCreator.ItemConfig.AllowedNBTType<Enum<E>> {
    @Override
    public Enum<E> getValue() {
      return value;
    }
  }

  public record AllowedDouble(Double value)
      implements GuiCreator.ItemConfig.AllowedNBTType<Double> {
    @Override
    public Double getValue() {
      return value;
    }
  }

  public record AllowedString(String value)
      implements GuiCreator.ItemConfig.AllowedNBTType<String> {
    @Override
    public String getValue() {
      return value;
    }
  }

  public record AllowedItemStack(ItemStack value)
      implements GuiCreator.ItemConfig.AllowedNBTType<ItemStack> {
    @Override
    public ItemStack getValue() {
      return value;
    }
  }

  public record AllowedFloat(Float value) implements GuiCreator.ItemConfig.AllowedNBTType<Float> {
    @Override
    public Float getValue() {
      return value;
    }
  }
}
