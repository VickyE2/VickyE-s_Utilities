/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.guiparent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.lone.LoneLibs.nbt.nbtapi.NBT;
import dev.lone.LoneLibs.nbt.nbtapi.NBTCompound;
import dev.lone.LoneLibs.nbt.nbtapi.NBTItem;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.guiparent.subGui.PagedGui;
import org.vicky.listeners.BaseGuiListener;
import org.vicky.utilities.ContextLogger.ContextLogger;
import org.vicky.utilities.DatabaseManager.dao_s.DatabasePlayerDAO;
import org.vicky.utilities.DatabaseManager.templates.DatabasePlayer;
import org.vicky.utilities.Denoations.UnstableRecommended;
import org.vicky.utilities.PermittedObject;
import org.vicky.utilities.TriFunction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.vicky.utilities.FileManager.getCallerFrame;

/**
 * GuiCreator is a utility class responsible for creating and opening both GUI
 * inventories and anvil GUIs for players.
 * <p>
 * It leverages various APIs including ItemsAdder and AnvilGUI to build custom
 * GUIs.
 * </p>
 */
@SuppressWarnings("deprecation")
public class GuiCreator {

	private static final ContextLogger logger = new ContextLogger(ContextLogger.ContextType.SYSTEM, "GUI");
	private final BaseGuiListener listener;
	private final JavaPlugin plugin;
	private BaseGui baseGui;
	// Flag indicating if a textured GUI can be created.
	boolean canBeMade = false;

	/**
	 * Constructs a new GuiCreator with the specified plugin and GUI listener.
	 *
	 * @param plugin
	 *            The JavaPlugin instance for the plugin.
	 * @param listener
	 *            The BaseGuiListener responsible for handling GUI events.
	 */
	public GuiCreator(JavaPlugin plugin, BaseGuiListener listener) {
		this.plugin = plugin;
		this.listener = listener;
	}

	/**
	 * Parses a slot range string into a set of zero-based slot indices.
	 * <p>
	 * The slotRange string can be a comma-separated list of numbers or ranges
	 * (e.g., "1-10,12,15-18"). This method converts the values to zero-based
	 * indices and ensures they are within valid bounds.
	 * </p>
	 *
	 * @param slotRange
	 *            The slot range string to parse.
	 * @return A set of integer slot indices.
	 */
	public static Set<Integer> parseSlots(String slotRange) {
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

	// Add this field in your GUI class
	private final Map<Inventory, Integer> scrollOffsets = new ConcurrentHashMap<>();

	/**
	 * Helper method to set a value on the NBT compound based on the string value.
	 * It tries boolean, then integer, then double, and falls back to string.
	 *
	 * @param nbt
	 *            the NBT compound to modify
	 * @param key
	 *            the key to set
	 * @param value
	 *            the string value from the configuration
	 */
	private static void setNBTValue(NBTCompound nbt, String key, Object value) {
		if (value instanceof Boolean bool)
			nbt.setBoolean(key, bool);
		else if (value instanceof UUID uuid)
			nbt.setUUID(key, uuid);
		else if (value instanceof Integer integer)
			nbt.setInteger(key, integer);
		else if (value instanceof Enum<?> enumerated)
			nbt.setEnum(key, enumerated);
		else if (value instanceof Double doublee)
			nbt.setDouble(key, doublee);
		else if (value instanceof String str)
			nbt.setString(key, str);
		else if (value instanceof ItemStack stack)
			nbt.setItemStack(key, stack);
		else if (value instanceof Float floater)
			nbt.setFloat(key, floater);
	}

	/**
	 * Creates an ItemStack based on the provided ItemConfig and player context.
	 * <p>
	 * If the item is configured as an ItemsAdder item, this method attempts to
	 * create it via the ItemsAdder API. For player head items, the owner is set
	 * accordingly. Otherwise, a regular item is created.
	 * </p>
	 *
	 * @param itemConfig
	 *            The configuration for the item.
	 * @param player
	 *            The player context (used for setting head owner, etc.).
	 * @return An ItemStack created according to the configuration.
	 */
	public static ItemStack createItem(ItemConfig itemConfig, Player player) {
		if (itemConfig.isItemsAdderItem()) {
			CustomStack customItem = CustomStack.getInstance(itemConfig.getItemsAdderName());
			if (customItem != null) {
				ItemStack customStack = customItem.getItemStack();
				// Apply NBT changes first.
				if (!itemConfig.getNBTData().isEmpty()) {
					NBT.modify(customStack, nbt -> {
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
					if (customStack.getType() == Material.PLAYER_HEAD && meta instanceof SkullMeta skullMeta) {
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
				logger.print("ItemsAdder item '" + itemConfig.getItemsAdderName() + "' could not be found!",
						ContextLogger.LogType.ERROR, true);
				throw new RuntimeException("ItemsAdder item not found");
			}
		}

		// Fallback: Create a regular item.
		ItemStack item = new ItemStack(itemConfig.getMaterial());

		// First, modify the item's NBT data.
		if (!itemConfig.getNBTData().isEmpty()) {
			NBT.modify(item, nbt -> {
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
			if (itemConfig.isEnchanted()) {
				meta.addEnchant(Enchantment.LUCK, 1, true);
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
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
			if (itemConfig.getItemMetaFunction() != null)
				itemConfig.getItemMetaFunction().apply(meta);
			item.setItemMeta(meta);
		}
		return item;
	}

	/**
	 * Retrieves NBT data from the given ItemStack using the NBT API. Supported
	 * types: UUID, Integer, Double, String, ItemStack, and Enum.
	 *
	 * @param item
	 *            the ItemStack from which to retrieve NBT data
	 * @param nbtKey
	 *            the key for the NBT tag
	 * @param clazz
	 *            the Class representing the expected type
	 * @param <T>
	 *            the expected type of the NBT data
	 * @return the NBT data cast to type T, or null if the tag is not present
	 * @throws IllegalArgumentException
	 *             if the type is unsupported
	 */
	public static <T> T getNBTData(ItemStack item, String nbtKey, Class<T> clazz) {
		if (item == null)
			return null;
		NBTItem nbtItem = new NBTItem(item);
		if (!nbtItem.hasKey(nbtKey))
			return null;

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
	 * Checks if the given ItemStack contains custom NBT data for the specified key
	 * using a key derived from the ItemConfig's plugin.
	 * <p>
	 * This method creates a {@link NamespacedKey} using the plugin obtained from
	 * {@link JavaPlugin#getProvidingPlugin(Class)} for the ItemConfig class and
	 * checks the item's {@link org.bukkit.persistence.PersistentDataContainer} for
	 * a STRING value.
	 * </p>
	 *
	 * @param item
	 *            the ItemStack to check; if null or lacking ItemMeta, false is
	 *            returned
	 * @param nbtKey
	 *            the custom NBT key to check for
	 * @return true if the item contains a STRING value for the given key, false
	 *         otherwise
	 */
	public static boolean hasNBTData(ItemStack item, String nbtKey) {
		if (item == null)
			return false;
		NBTItem nbtItem = new NBTItem(item);
		return nbtItem.hasKey(nbtKey);
	}

	/**
	 * Extracts an ItemConfig from the given ItemStack.
	 *
	 * @param item
	 *            the ItemStack to convert; if null, null is returned
	 * @return an ItemConfig populated with the extracted data from the ItemStack,
	 *         or null if the item is null
	 */
	public static ItemConfig fromItemStack(ItemStack item) {
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

		// The slotRange and clickable properties are typically provided by the GUI
		// configuration,
		// and are not stored in the ItemStack. We set them to default values.
		String slotRange = ""; // Default: empty string (or you could choose a specific default)
		boolean clickable = false; // Default: false

		// Create and return the ItemConfig. This constructor must match your ItemConfig
		// class.
		return new ItemConfig(material, name, slotRange, clickable, customModelData, itemsAdderName, lore, headOwner,
				nbtData, null);
	}

	/**
	 * Derive content slots from grid height, arrow mode and spacing. If you already
	 * have slotRangeStr, prefer that instead.
	 */
	public static List<Integer> deriveContentSlots(int height, ArrowGap spacing, ArrowBarMode arrowMode) {
		int totalSlots = 9 * height;
		// collect all grid slots
		List<Integer> allSlots = new ArrayList<>(totalSlots);
		for (int i = 0; i < totalSlots; i++)
			allSlots.add(i);

		// compute bounding rows & cols for the whole inventory
		int minRow = 0;
		int maxRow = height - 1;
		int minCol = 0;
		int maxCol = 8;
		int gridWidth = maxCol - minCol + 1;

		Set<Integer> reserved = new HashSet<>();

		// center columns & rows used for arrows
		int centerCol = 4;
		int middleRow = (minRow + maxRow) / 2;
		int topCenter = minRow * 9 + centerCol;
		int bottomCenter = maxRow * 9 + centerCol;
		int leftCenter = middleRow * 9 + minCol;
		int rightCenter = middleRow * 9 + maxCol;

		// If spacing.gap is used to push arrows left/right from center, compute them:
		int prevCol = Math.max(0, centerCol - spacing.gap);
		int nextCol = Math.min(8, centerCol + spacing.gap);
		int prevSlot = (minRow + (maxRow - minRow) / 2) * 9 + prevCol;
		int nextSlot = (minRow + (maxRow - minRow) / 2) * 9 + nextCol;
		// Note: adjust logic to match your old center/gap usage if needed.

		// Reserve arrow slots based on mode (top/bottom/left/right combos)
		switch (arrowMode) {
			case UP -> reserved.add(topCenter);
			case DOWN -> reserved.add(bottomCenter);
			case UP_DOWN -> {
				reserved.add(topCenter);
				reserved.add(bottomCenter);
			}
			case LEFT -> reserved.add(leftCenter);
			case RIGHT -> reserved.add(rightCenter);
			case LEFT_RIGHT -> {
				reserved.add(leftCenter);
				reserved.add(rightCenter);
			}
		}

		// Optionally reserve extra arrow gap neighbours if you want spacing effect:
		// e.g. reserved.add(Math.max(0, topCenter - spacing.gap));
		// (Use carefully — can eat content slots.)

		// Remove reserved slots from allSlots
		List<Integer> contentSlots = new ArrayList<>();
		for (int s : allSlots) {
			if (!reserved.contains(s))
				contentSlots.add(s);
		}

		// Sort ascending, row-major order (top-left -> bottom-right)
		Collections.sort(contentSlots);
		return contentSlots;
	}

	/**
	 * Opens a custom GUI inventory for the given player.
	 * <p>
	 * The GUI is created based on the specified dimensions, title, texture
	 * settings, and item configurations.
	 * </p>
	 *
	 * @param player
	 *            The player who will see the GUI.
	 * @param height
	 *            The number of rows in the GUI inventory (must be >= 1).
	 * @param width
	 *            The number of columns in the GUI inventory (must be between 1 and
	 *            9).
	 * @param title
	 *            The title of the GUI inventory.
	 * @param textured
	 *            Whether to use a texture for the inventory background.
	 * @param textureKey
	 *            The key for the texture if textured is true.
	 * @param offset
	 *            The vertical offset for the texture application.
	 * @param itemConfigs
	 *            Varargs of ItemConfig objects that define items in the GUI (e.g.,
	 *            positions, names, lore, etc.).
	 */
	public void simple(Player player, int height, int width, String title, boolean textured, String textureKey,
					   int offset, ItemConfig... itemConfigs) {

		if (height < 1 || width < 1 || width > 9) {
			if (player.isOp()) {
				player.sendMessage("Invalid dimensions for the GUI. Report this to a dev.");
				getCallerFrame().ifPresent(f -> player.sendMessage("caller: " + f.getClassName() + "#"
						+ f.getMethodName() + " (" + f.getFileName() + ":" + f.getLineNumber() + ")"));
			}
			return;
		}

		int slots = height * width;
		Plugin itemsAdder = Bukkit.getPluginManager().getPlugin("ItemsAdder");
		Inventory inventory;

		// Create an inventory based on texture availability.
		if (textured && textureKey != null && !textureKey.isEmpty() && itemsAdder != null) {
			FontImageWrapper texture = new FontImageWrapper(textureKey);
			if (!texture.exists()) {
				logger.print("Gui Texture: " + textureKey + " Does not exist", ContextLogger.LogType.WARNING, true);
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
			Set<Integer> slotSet = parseSlots(itemConfig.getSlotRange());
			for (int slot : slotSet) {
				if (slot < slots) {
					ItemStack item = createItem(itemConfig, player);
					inventory.setItem(slot, item);
					if (itemConfig.buttonAction != null)
						listener.registerButton(inventory, itemConfig);
				}
			}
		}

		// Open the inventory and apply texture if available.
		listener.addGuiInventory(inventory);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			baseGui.addInventory(player.getUniqueId(), inventory);
			player.openInventory(inventory);
			if (canBeMade) {
				FontImageWrapper texture = new FontImageWrapper(textureKey);
				TexturedInventoryWrapper.setPlayerInventoryTexture(player, texture, title, 0, offset);
			}
		});
	}

	static boolean compareInventories(Inventory inv1, Inventory inv2) {
		if (inv1.getSize() != inv2.getSize()) {
			return false;
		}
		for (int i = 0; i <= inv1.getSize(); i++) {
			if (!Objects.requireNonNull(inv1.getItem(i)).isSimilar(inv2.getItem(i)))
				return false;
		}
		return true;
	}

	/**
	 * Opens a paginated GUI for the given player. fluent in this case is false
	 *
	 * @param player
	 *            The player who will see the GUI.
	 * @param height
	 *            The number of rows in the GUI.
	 * @param spacing
	 *            The spacing gap for navigation buttons.
	 * @param mainItems
	 *            The list of persistent ItemConfig objects (e.g., settings, exit)
	 *            that are present on every page.
	 * @param pageItems
	 *            The list of page-specific ItemConfig objects (the items that
	 *            change per page).
	 * @param slotRangeStr
	 *            A string specifying the allowed slots for page items (e.g.,
	 *            "10-12,19-21,28-30").
	 * @param page
	 *            The current page number.
	 * @param itemsPerPage
	 *            The maximum number of page items per page.
	 * @param title
	 *            The title of the GUI.
	 * @param maintainPersistentButtons
	 *            Whether persistent main buttons should be shown.
	 * @param textured
	 *            Whether the GUI should be textured.
	 * @param textureKey
	 *            The texture key (if textured is true).
	 * @param offset
	 *            The vertical texture offset.
	 */
	public void paginated(Player player, int height, ArrowGap spacing, List<GuiCreator.ItemConfig> mainItems,
						  List<GuiCreator.ItemConfig> pageItems, String slotRangeStr, int page, int itemsPerPage, String title,
						  boolean maintainPersistentButtons, boolean textured, String textureKey, int offset) {
		paginated(player, height, spacing, mainItems, pageItems, slotRangeStr, page, itemsPerPage, title,
				maintainPersistentButtons, textured, textureKey, offset, false);
	}

	/**
	 * Opens a paginated GUI for the given player.
	 *
	 * @param player
	 *            The player who will see the GUI.
	 * @param height
	 *            The number of rows in the GUI.
	 * @param spacing
	 *            The spacing gap for navigation buttons.
	 * @param mainItems
	 *            The list of persistent ItemConfig objects (e.g., settings, exit)
	 *            that are present on every page.
	 * @param pageItems
	 *            The list of page-specific ItemConfig objects (the items that
	 *            change per page).
	 * @param slotRangeStr
	 *            A string specifying the allowed slots for page items (e.g.,
	 *            "10-12,19-21,28-30").
	 * @param page
	 *            The current page number.
	 * @param itemsPerPage
	 *            The maximum number of page items per page.
	 * @param title
	 *            The title of the GUI.
	 * @param maintainPersistentButtons
	 *            Whether persistent main buttons should be shown.
	 * @param textured
	 *            Whether the GUI should be textured.
	 * @param textureKey
	 *            The texture key (if textured is true).
	 * @param offset
	 *            The vertical texture offset.
	 * @param fluent
	 *            Weather or not to reuse the same gui and not reopen the gui for
	 *            the player. <b>Undergoing serious development and tweaking :O</b>
	 */
	public void paginated(Player player, int height, ArrowGap spacing, List<GuiCreator.ItemConfig> mainItems,
						  List<GuiCreator.ItemConfig> pageItems, String slotRangeStr, int page, int itemsPerPage, String title,
						  boolean maintainPersistentButtons, boolean textured, String textureKey, int offset,
						  @UnstableRecommended(reason = "Undergoing serious development and tweaking") boolean fluent) {

		Optional<DatabasePlayer> opt = new DatabasePlayerDAO().findById(player.getUniqueId());
		if (opt.isEmpty()) {
			throw new RuntimeException("Player that made request cannot be found on the database");
		}
		DatabasePlayer dbPlayer = opt.get();
		String themeId = dbPlayer.getUserTheme();

		List<GuiCreator.ItemConfig> items;
		int totalItems;
		int startIndex;
		int endIndex;

		// same here
		if (baseGui instanceof PagedGui pagedGui) {
			items = pagedGui.getItems(); // Fetch fresh items
			totalItems = items.size();
		} else {
			items = pageItems; // fallback / passed-in list
			totalItems = items.size();
			int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
			page = Math.max(1, Math.min(page, totalPages));
		}
		startIndex = (page - 1) * itemsPerPage;
		endIndex = Math.min(startIndex + itemsPerPage, totalItems);
		items = items.subList(startIndex, endIndex); // only slice after computing endIndex
		int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

		boolean reuseInventory = fluent && baseGui.getInventory(player.getUniqueId()) != null
				&& player.getOpenInventory().getTopInventory().equals(baseGui.getInventory(player.getUniqueId()));

		// Create an inventory with a fixed size.
		int slots = 9 * height;
		Inventory inventory;
		if (reuseInventory) {
			inventory = baseGui.getInventory(player.getUniqueId());
			Set<Integer> slotsToClear = parseSlots(slotRangeStr);
			listener.removeButtons(inventory, slotsToClear.stream().mapToInt(Integer::intValue).toArray());
			for (int slot : slotsToClear) {
				inventory.setItem(slot, null); // Clear old content
			}
		} else {
			inventory = Bukkit.createInventory(new GUIHolder(), slots, title);
		}

		// Place persistent main buttons if enabled.
		if (maintainPersistentButtons) {
			// plugin.getLogger().info("MPB-B: " +
			// Arrays.toString(inventory.getContents()));
			for (GuiCreator.ItemConfig config : mainItems) {
				for (int x : parseSlots(config.getSlotRange())) {
					if (x >= 0 && x < slots) {
						inventory.setItem(x, createItem(config, player));
						if (config.getButtonAction() != null)
							listener.registerButton(inventory, config);
					}
				}
			}
			// plugin.getLogger().info("MPB-A" + Arrays.toString(inventory.getContents()));
		}

		// Parse the allowed slot range for page items.
		Set<Integer> slotSet = parseSlots(slotRangeStr);
		List<Integer> allowedSlots = new ArrayList<>(slotSet);
		Collections.sort(allowedSlots); // sort ascending

		// Populate page items into the allowed slots.
		for (int i = 0; i < items.size() && i < allowedSlots.size(); i++) {
			GuiCreator.ItemConfig config = items.get(i);
			ItemStack item = createItem(config, player);
			int targetSlot = allowedSlots.get(i);

			if (targetSlot < slots) {
				inventory.setItem(targetSlot, item);
				config.setSlotRange(Integer.toString(targetSlot + 1));
				if (config.getButtonAction() != null) {
					listener.registerButton(inventory, config);
				}
			}
		}

		// Determine navigation button slots.
		int centerSlot = ((height - 1) * 9) + 4;
		int prevSlot = Math.max(0, centerSlot - spacing.gap);
		int nextSlot = Math.min(slots - 1, centerSlot + spacing.gap);

		// Create previous page button.
		if (page > 1) {
			int finalPage = page;
			GuiCreator.ItemConfig prevConfig = new GuiCreator.ItemConfig(Material.ARROW,
					"ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ (" + (page - 1) + ")", Integer.toString(prevSlot + 1), true, null,
					"vicky_themes:left_arrow_" + themeId, List.of(ChatColor.GREEN + "ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ᴛᴏ ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ"),
					ButtonAction.ofRunCode(p -> paginated(p, height, spacing, mainItems, pageItems, slotRangeStr,
							finalPage - 1, itemsPerPage, title, maintainPersistentButtons, textured, textureKey, offset,
							fluent), true));
			inventory.setItem(prevSlot, createItem(prevConfig, player));
			listener.registerButton(inventory, prevConfig);
		} else {
			GuiCreator.ItemConfig prevConfig = new GuiCreator.ItemConfig(Material.ARROW, "ɴᴏ ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ",
					Integer.toString(prevSlot + 1), true, null, "vicky_themes:left_arrow_" + themeId,
					List.of(ChatColor.GREEN + "ᴛʜɪs ɪs ᴛʜᴇ ғɪʀsᴛ ᴘᴀɢᴇ"), null);
			inventory.setItem(prevSlot, createItem(prevConfig, player));
		}
		// Create next page button.
		if (page < totalPages) {
			int finalPage = page;
			GuiCreator.ItemConfig nextConfig = new GuiCreator.ItemConfig(Material.ARROW,
					"ɴᴇxᴛ ᴘᴀɢᴇ (" + (page + 1) + ")", Integer.toString(nextSlot + 1), true, null,
					"vicky_themes:right_arrow_" + themeId, List.of(ChatColor.GREEN + "ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ᴛᴏ ɴᴇxᴛ ᴘᴀɢᴇ"),
					ButtonAction.ofRunCode(p -> paginated(p, height, spacing, mainItems, pageItems, slotRangeStr,
							finalPage + 1, itemsPerPage, title, maintainPersistentButtons, textured, textureKey, offset,
							fluent), true));
			inventory.setItem(nextSlot, createItem(nextConfig, player));
			listener.registerButton(inventory, nextConfig);
		} else {
			GuiCreator.ItemConfig nextConfig = new GuiCreator.ItemConfig(Material.ARROW, "ɴᴇxᴛ ᴘᴀɢᴇ (0)",
					Integer.toString(nextSlot + 1), true, null, "vicky_themes:right_arrow_" + themeId,
					List.of(ChatColor.GREEN + "ᴛʜɪs ɪs ᴛʜᴇ ʟᴀsᴛ ᴘᴀɢᴇ"), null);
			inventory.setItem(nextSlot, createItem(nextConfig, player));
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			if (!reuseInventory || !player.getOpenInventory().getTopInventory().equals(inventory)) {
				player.openInventory(inventory);
				baseGui.addInventory(player.getUniqueId(), inventory);
				listener.addGuiInventory(inventory);
			}

			if (textured && textureKey != null && !textureKey.isEmpty()) {
				FontImageWrapper texture = new FontImageWrapper(textureKey);
				if (texture.exists()) {
					TexturedInventoryWrapper.setPlayerInventoryTexture(player, texture, title, 0, offset);
				} else {
					logger.print("Gui Texture: " + textureKey + " does not exist", ContextLogger.LogType.WARNING);
				}
			}
		});
	}

	/**
	 * Opens a paginated GUI where each page displays a number of ItemConfigClasses.
	 * Each ItemConfigClass contains a mapping of elements (either an ItemConfig or
	 * an ItemConfigGroup) to specific slot positions within the page.
	 * <p>
	 * If the number of ItemConfigClasses exceeds itemsPerPage, subsequent pages are
	 * created. If any element in an ItemConfigClass would be placed outside the
	 * inventory bounds, a GuiSpaceExceededException is thrown.
	 * </p>
	 *
	 * @param player
	 *            the player who will see the GUI.
	 * @param height
	 *            the number of rows in the GUI.
	 * @param spacing
	 *            the spacing gap for navigation buttons.
	 * @param mainItems
	 *            the list of persistent ItemConfig objects (present on every page).
	 * @param classes
	 *            the list of ItemConfigClass objects (each representing a page
	 *            element group).
	 * @param itemsPerPage
	 *            the maximum number of ItemConfigClasses to show per page.
	 * @param title
	 *            the title of the GUI.
	 * @param maintainPersistentButtons
	 *            whether persistent main buttons are to be shown.
	 * @param textured
	 *            whether the GUI should be textured.
	 * @param textureKey
	 *            the texture key if textured.
	 * @param offset
	 *            the vertical texture offset.
	 */
	public void paginated(Player player, int height, int page, ArrowGap spacing, List<ItemConfig> mainItems,
						  List<ItemConfigClass> classes, int itemsPerPage, String title, boolean maintainPersistentButtons,
						  boolean textured, String textureKey, int offset) {

		Optional<DatabasePlayer> opt = new DatabasePlayerDAO().findById(player.getUniqueId());
		if (opt.isEmpty()) {
			throw new RuntimeException("Player that made request cannot be found on the database");
		}
		DatabasePlayer dbPlayer = opt.get();
		String themeId = dbPlayer.getUserTheme();

		// Compute total pages based on the number of ItemConfigClasses and
		// itemsPerPage.
		int totalClasses = classes.size();
		int totalPages = (int) Math.ceil((double) totalClasses / itemsPerPage);

		// Partition the classes for the current page.
		int startIdx = (page - 1) * itemsPerPage;
		int endIdx = Math.min(startIdx + itemsPerPage, totalClasses);
		List<ItemConfigClass> pageClasses = !classes.isEmpty() ? classes.subList(startIdx, endIdx) : new ArrayList<>();

		// Create an inventory.
		int slots = 9 * height;
		Inventory inventory = Bukkit.createInventory(new GUIHolder(), slots, title);

		// Place persistent main buttons.
		if (maintainPersistentButtons) {
			for (ItemConfig config : mainItems) {
				for (int slot : parseSlots(config.getSlotRange())) {
					if (slot >= 0 && slot < slots) {
						inventory.setItem(slot, createItem(config, player));
						if (config.buttonAction != null)
							listener.registerButton(inventory, config);
					}
				}
			}
		}

		// Iterate over each ItemConfigClass along with its vertical index.
		for (int classIndex = 0; classIndex < pageClasses.size(); classIndex++) {
			ItemConfigClass icc = pageClasses.get(classIndex);
			// For each element in this class, arrange it in the inventory
			int finalClassIndex = classIndex;
			icc.getElements().forEach((baseSlot, element) -> {
				// Calculate the starting slot by adding a vertical offset based on the class
				// index.
				int initialSlot = baseSlot + finalClassIndex * 9;
				// Find the next available slot (to the right) from the initial slot.
				int targetSlot = findNextAvailableSlot(inventory, initialSlot, slots);
				if (targetSlot < 0 || targetSlot >= slots) {
					throw new GuiSpaceExceededException(
							"No available slot starting at " + initialSlot + " for element " + element);
				}
				// Now, place the element.
				if (element instanceof ItemConfig config) {
					inventory.setItem(targetSlot, createItem(config, player));
					if (config.getButtonAction() != null) {
						listener.registerButton(inventory, config);
					}
				} else if (element instanceof ItemConfigGroup group) {
					List<ItemConfig> groupItems = group.items();
					if (group.orientation() == ItemConfigGroup.Orientation.HORIZONTAL) {
						// For horizontal groups, place each item to the right of the starting slot.
						for (int i = 0; i < groupItems.size(); i++) {
							int baseGroupSlot = targetSlot + i;
							int finalSlot = findNextAvailableSlot(inventory, baseGroupSlot, slots);
							if (finalSlot < 0 || finalSlot >= slots) {
								throw new GuiSpaceExceededException(
										"Group horizontal item exceeds inventory size at slot " + baseGroupSlot);
							}
							inventory.setItem(finalSlot, createItem(groupItems.get(i), player));
							if (groupItems.get(i).getButtonAction() != null) {
								listener.registerButton(inventory, groupItems.get(i));
							}
						}
					} else {
						// VERTICAL
						// For vertical groups, each subsequent item is placed 9 slots below.
						for (int i = 0; i < groupItems.size(); i++) {
							int baseGroupSlot = targetSlot + i * 9;
							int finalSlot = findNextAvailableSlot(inventory, baseGroupSlot, slots);
							if (finalSlot < 0 || finalSlot >= slots) {
								throw new GuiSpaceExceededException(
										"Group vertical item exceeds inventory size at slot " + baseGroupSlot);
							}
							inventory.setItem(finalSlot, createItem(groupItems.get(i), player));
							if (groupItems.get(i).getButtonAction() != null) {
								listener.registerButton(inventory, groupItems.get(i));
							}
						}
					}
				}
			});
		}

		// Determine navigation button slots.
		int centerSlot = ((height - 1) * 9) + 4;
		int prevSlot = Math.max(0, centerSlot - spacing.gap);
		int nextSlot = Math.min(slots - 1, centerSlot + spacing.gap);

		// Create previous page button.
		// Create previous page button.
		if (page > 1) {
			GuiCreator.ItemConfig prevConfig = new GuiCreator.ItemConfig(Material.ARROW,
					"ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ (" + (page - 1) + ")", Integer.toString(prevSlot + 1), true, null,
					"vicky_themes:left_arrow_" + themeId, List.of(ChatColor.GREEN + "ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ᴛᴏ ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ"),
					ButtonAction.ofRunCode(p -> paginated(p, height, page + 1, spacing, mainItems, classes,
							itemsPerPage, title, maintainPersistentButtons, textured, textureKey, offset), true));
			inventory.setItem(prevSlot, createItem(prevConfig, player));
			listener.registerButton(inventory, prevConfig);
		} else {
			// No previous page: fill with air.
			inventory.setItem(prevSlot, createItem(GuiCreator.ItemConfig.ofAir(), player));
		}

		// Create next page button.
		if (page < totalPages) {
			GuiCreator.ItemConfig nextConfig = new GuiCreator.ItemConfig(Material.ARROW,
					"ɴᴇxᴛ ᴘᴀɢᴇ (" + (page + 1) + ")", Integer.toString(nextSlot + 1), true, null,
					"vicky_themes:right_arrow_" + themeId, List.of(ChatColor.GREEN + "ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ᴛᴏ ɴᴇxᴛ ᴘᴀɢᴇ"),
					ButtonAction.ofRunCode(p -> paginated(p, height, page - 1, spacing, mainItems, classes,
							itemsPerPage, title, maintainPersistentButtons, textured, textureKey, offset), true));
			inventory.setItem(nextSlot, createItem(nextConfig, player));
			listener.registerButton(inventory, nextConfig);
		} else {
			inventory.setItem(nextSlot, createItem(GuiCreator.ItemConfig.ofAir(), player));
		}

		// Set the inventory in the listener and open it.
		listener.addGuiInventory(inventory);
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			baseGui.addInventory(player.getUniqueId(), inventory);
			player.openInventory(inventory);
			if (textured && textureKey != null && !textureKey.isEmpty()) {
				FontImageWrapper texture = new FontImageWrapper(textureKey);
				if (texture.exists()) {
					TexturedInventoryWrapper.setPlayerInventoryTexture(player, texture, title, 0, offset);
				} else {
					logger.print("Gui Texture: " + textureKey + " does not exist", ContextLogger.LogType.WARNING);
				}
			}
		});
	}

	/**
	 * Opens an anvil GUI for the given player with custom item configurations and a
	 * completion action.
	 *
	 * @param player
	 *            The player who will interact with the anvil GUI.
	 * @param initialText
	 *            The initial text to display in the anvil GUI's input field.
	 * @param leftItemConfig
	 *            The ItemConfig for the left input slot (can be null).
	 * @param rightItemConfig
	 *            The ItemConfig for the right input slot (can be null).
	 * @param outputItemConfig
	 *            The ItemConfig for the output slot (can be null).
	 * @param title
	 *            The title of the anvil GUI window.
	 * @param canClickLeft
	 *            If true, the left slot is intractable.
	 * @param canClickRight
	 *            If true, the right slot is intractable.
	 * @param completionAction
	 *            A BiFunction that takes a Player and a String (the input text) and
	 *            returns a list of AnvilGUI.ResponseAction to determine the anvil
	 *            GUI's behavior upon a click.
	 */
	public void anvil(Player player, String initialText, boolean textured, String textureKey, int offset,
					  @NotNull ItemConfig leftItemConfig, ItemConfig rightItemConfig, ItemConfig outputItemConfig, String title,
					  boolean canClickLeft, boolean canClickRight,
					  @NotNull TriFunction<Player, AnvilGUI.StateSnapshot, Integer, List<AnvilGUI.ResponseAction>> completionAction,
					  @NotNull Consumer<AnvilGUI.StateSnapshot> onCLose) {
		ItemStack leftItem = null;
		ItemStack rightItem = null;
		ItemStack outputItem = null;

		leftItem = createItem(leftItemConfig, player);
		if (rightItemConfig != null) {
			rightItem = createItem(rightItemConfig, player);
		}
		if (outputItemConfig != null) {
			outputItem = createItem(outputItemConfig, player); // Generate ItemStack for output slot
		}

		AnvilGUI.Builder builder = new AnvilGUI.Builder().plugin(plugin).text(initialText).title(title)
				.disableGeyserCompat().preventClose()
				.onClick((slot, stateSnapshot) -> completionAction.apply(player, stateSnapshot, slot)).onClose(onCLose);

		if (canClickLeft && canClickRight) {
			builder.interactableSlots(AnvilGUI.Slot.INPUT_LEFT, AnvilGUI.Slot.INPUT_RIGHT);
		} else if (canClickLeft) {
			builder.interactableSlots(AnvilGUI.Slot.INPUT_LEFT);
		} else if (canClickRight) {
			builder.interactableSlots(AnvilGUI.Slot.INPUT_RIGHT);
		}

		if (rightItemConfig != null) {
			builder.itemRight(rightItem);
		}
		builder.itemLeft(leftItem);
		if (outputItemConfig != null) {
			builder.itemOutput(outputItem);
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
			builder.open(player);
			if (textured && textureKey != null && !textureKey.isEmpty()) {
				Bukkit.getScheduler().runTaskLater(plugin, () -> {
					FontImageWrapper texture = new FontImageWrapper(textureKey);
					if (texture.exists()) {
						TexturedInventoryWrapper.setPlayerInventoryTexture(player, texture, title, 0, offset);
					} else {
						logger.print("Gui Texture: " + textureKey + " does not exist", ContextLogger.LogType.WARNING);
					}
				}, 1L);
			}
		});
	}

	/**
	 * Render content window for an already-open inventory. This only updates
	 * allowedSlots, re-registers content buttons, and leaves persistent/main
	 * buttons and arrow buttons intact.
	 */
	private void renderScrollableWindow(Plugin plugin, Inventory inventory, List<GuiCreator.ItemConfig> itemsSource,
										List<Integer> allowedSlots) {
		// ensure we run on main thread
		Bukkit.getScheduler().runTask(plugin, () -> {
			if (inventory == null)
				return;
			// get current offset for this inventory (default 0)
			int offset = scrollOffsets.getOrDefault(inventory, 0);
			int totalItems = itemsSource.size();
			int windowSize = allowedSlots.size();

			// clamp offset safely
			if (totalItems <= windowSize)
				offset = 0;
			else
				offset = Math.max(0, Math.min(offset, totalItems - windowSize));
			scrollOffsets.put(inventory, offset);

			// Clear existing content slots (remove button registrations first)
			int[] toClear = allowedSlots.stream().mapToInt(Integer::intValue).toArray();
			listener.removeButtons(inventory, toClear);
			for (int s : allowedSlots)
				inventory.setItem(s, null);

			// Place visible items and register buttons
			for (int i = 0; i < windowSize; i++) {
				int itemIndex = offset + i;
				int targetSlot = allowedSlots.get(i);
				if (itemIndex >= totalItems) {
					inventory.setItem(targetSlot, null);
					continue;
				}

				GuiCreator.ItemConfig cfg = itemsSource.get(itemIndex);
				// We should not mutate original config that might be shared; create shallow
				// copy if needed
				GuiCreator.ItemConfig copyCfg = new GuiCreator.ItemConfig(cfg.getMaterial(), cfg.getName(),
						Integer.toString(targetSlot + 1), cfg.isClickable(), cfg.getCustomModelData(),
						cfg.getItemsAdderName(), cfg.getLore(), cfg.getButtonAction());
				// NOTE: if ItemConfig has more fields (potion, headOwner, etc.), include them
				// in constructor/copy

				ItemStack item = createItem(copyCfg, /* player irrelevant here */ null);
				inventory.setItem(targetSlot, item);

				if (copyCfg.getButtonAction() != null) {
					// register the action for this inventory context
					listener.registerButton(inventory, copyCfg);
				}
			}
		});
	}

	/**
	 * Open a scrollable GUI that will update in-place when arrows clicked. This
	 * function creates arrow ItemConfigs whose ButtonAction updates the offset and
	 * calls renderScrollableWindow.
	 */
	public void scrollable(Player player, Plugin plugin, int height, ArrowGap spacing,
						   List<GuiCreator.ItemConfig> mainItems, List<GuiCreator.ItemConfig> itemsSource, String slotRangeStr, // optional:
						   // if
						   // null
						   // ->
						   // derive
						   int initialOffset, ScrollOrientation orientation, ArrowBarMode arrowMode, String title,
						   boolean maintainPersistentButtons, boolean textured, String textureKey, int offsetTexture) {

		// derive allowedSlots (fall back to deriveContentSlots if slotRangeStr
		// null/blank)
		Set<Integer> slotSet = (slotRangeStr == null || slotRangeStr.isBlank())
				? new LinkedHashSet<>(deriveContentSlots(height, spacing, arrowMode))
				: parseSlots(slotRangeStr);
		List<Integer> allowedSlots = new ArrayList<>(slotSet);
		Collections.sort(allowedSlots);
		if (allowedSlots.isEmpty())
			throw new IllegalArgumentException("No content slots.");

		int slots = 9 * height;
		Inventory inventory = Bukkit.createInventory(new GUIHolder(), slots, title);

		// place persistent main buttons
		if (maintainPersistentButtons) {
			for (GuiCreator.ItemConfig config : mainItems) {
				for (int x : parseSlots(config.getSlotRange())) {
					if (x >= 0 && x < slots) {
						inventory.setItem(x, createItem(config, player));
						if (config.getButtonAction() != null)
							listener.registerButton(inventory, config);
					}
				}
			}
		}

		// register arrow buttons that update scrollOffsets and call
		// renderScrollableWindow
		// Arrow click actions MUST be safe when invoked (check player open inventory)
		// compute positions (re-use your center/gap or derived top/left/right centers)
		int minSlot = allowedSlots.get(0);
		int maxSlot = allowedSlots.get(allowedSlots.size() - 1);
		int minRow = minSlot / 9;
		int maxRow = maxSlot / 9;
		int minCol = 9;
		int maxCol = -1;
		for (int s : allowedSlots) {
			int c = s % 9;
			minCol = Math.min(minCol, c);
			maxCol = Math.max(maxCol, c);
		}
		int topCenter = minRow * 9 + 4;
		int bottomCenter = maxRow * 9 + 4;
		int middleRow = (minRow + maxRow) / 2;
		int leftCenter = middleRow * 9 + minCol;
		int rightCenter = middleRow * 9 + maxCol;

		// helper to make arrow button actions — they update the offset map and call
		// render
		int finalMaxCol = maxCol;
		int finalMinCol = minCol;
		BiFunction<Integer, Integer, ItemConfig> makeArrow = (delta, slotPos) -> {
			// displayName uses delta sign; we choose arrow symbol externally
			String display = delta < 0 ? "◄/▲" : "►/▼";
			ButtonAction<?> action = ButtonAction.ofRunCode(p -> {
				Inventory inv = p.getOpenInventory().getTopInventory();
				if (!inv.equals(inventory))
					return;

				// calculate new offset based on orientation and delta semantics
				int cur = scrollOffsets.getOrDefault(inv, initialOffset);
				int gridWidth = finalMaxCol - finalMinCol + 1;
				int gridHeight = maxRow - minRow + 1;
				int windowSize = allowedSlots.size();
				int totalItems = itemsSource.size();

				int newOffset;
				if (orientation == ScrollOrientation.VERTICAL) {
					// delta is number of rows to shift
					newOffset = cur + delta * gridWidth;
				} else {
					// horizontal, delta is columns -> shift by delta
					newOffset = cur + delta;
				}

				// clamp and save then render
				if (totalItems <= windowSize)
					newOffset = 0;
				else
					newOffset = Math.max(0, Math.min(newOffset, totalItems - windowSize));
				scrollOffsets.put(inv, newOffset);

				renderScrollableWindow(plugin, inv, itemsSource, allowedSlots);
			}, true);

			return new GuiCreator.ItemConfig(Material.ARROW, display, Integer.toString(slotPos + 1), true, null,
					"vicky_themes:arrow", List.of(ChatColor.GREEN + "Click to scroll"), action);
		};

		// place the arrow items according to arrowMode
		if (arrowMode == ArrowBarMode.LEFT || arrowMode == ArrowBarMode.LEFT_RIGHT) {
			GuiCreator.ItemConfig leftCfg = makeArrow.apply(-1, leftCenter);
			inventory.setItem(leftCenter, createItem(leftCfg, player));
			listener.registerButton(inventory, leftCfg);
		}
		if (arrowMode == ArrowBarMode.RIGHT || arrowMode == ArrowBarMode.LEFT_RIGHT) {
			GuiCreator.ItemConfig rightCfg = makeArrow.apply(+1, rightCenter);
			inventory.setItem(rightCenter, createItem(rightCfg, player));
			listener.registerButton(inventory, rightCfg);
		}
		if (arrowMode == ArrowBarMode.UP || arrowMode == ArrowBarMode.UP_DOWN) {
			GuiCreator.ItemConfig upCfg = makeArrow.apply(-1, topCenter); // negative -> go up (rows)
			inventory.setItem(topCenter, createItem(upCfg, player));
			listener.registerButton(inventory, upCfg);
		}
		if (arrowMode == ArrowBarMode.DOWN || arrowMode == ArrowBarMode.UP_DOWN) {
			GuiCreator.ItemConfig downCfg = makeArrow.apply(+1, bottomCenter); // positive -> go down
			inventory.setItem(bottomCenter, createItem(downCfg, player));
			listener.registerButton(inventory, downCfg);
		}

		// initialize offset and render first window
		scrollOffsets.put(inventory, Math.max(0, initialOffset));
		renderScrollableWindow(plugin, inventory, itemsSource, allowedSlots);

		// finally open and track the inventory
		player.openInventory(inventory);
		baseGui.addInventory(player.getUniqueId(), inventory);
		listener.addGuiInventory(inventory);

		if (textured && textureKey != null && !textureKey.isEmpty()) {
			FontImageWrapper texture = new FontImageWrapper(textureKey);
			if (texture.exists()) {
				TexturedInventoryWrapper.setPlayerInventoryTexture(player, texture, title, 0, offsetTexture);
			} else {
				logger.print("Gui Texture: " + textureKey + " does not exist", ContextLogger.LogType.WARNING);
			}
		}
	}

	/**
	 * Finds the next available (null) slot in the inventory starting from a given
	 * index.
	 *
	 * @param inventory
	 *            the inventory to check
	 * @param startSlot
	 *            the slot index to start checking from
	 * @param maxSlots
	 *            the maximum number of slots (inventory size)
	 * @return the first available slot index, or -1 if none found
	 */
	private int findNextAvailableSlot(Inventory inventory, int startSlot, int maxSlots) {
		int slot = startSlot;
		while (slot < maxSlots && inventory.getItem(slot) != null) {
			slot++;
		}
		return slot < maxSlots ? slot : -1;
	}

	public void setOwner(BaseGui baseGui) {
		this.baseGui = baseGui;
	}

	public enum ArrowGap {
		SMALL(1), MEDIUM(2), WIDE(3);

		public final int gap;

		ArrowGap(int gap) {
			this.gap = gap;
		}
	}

	public enum ScrollOrientation {
		HORIZONTAL, VERTICAL
	}

	public enum ArrowBarMode {
		UP, DOWN, UP_DOWN, LEFT, RIGHT, LEFT_RIGHT
	}

	public enum Rarity {
		COMMON(ChatColor.WHITE, ChatColor.GRAY), UNCOMMON(ChatColor.GREEN, ChatColor.DARK_GREEN), RARE(ChatColor.BLUE,
				ChatColor.DARK_BLUE), EPIC(ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE), LEGENDARY(ChatColor.GOLD,
						ChatColor.YELLOW), MYTHICAL(ChatColor.DARK_RED,
								ChatColor.RED), GOD_LIKE(ChatColor.AQUA, ChatColor.LIGHT_PURPLE);

		private final List<ChatColor> colors;

		Rarity(ChatColor... color) {
			this.colors = new ArrayList<>(List.of(color));
		}

		public String getColor() {
			return colors.stream().map(item -> item.name().toLowerCase()).collect(Collectors.joining("-"));
		}

		public String getSimpleColor() {
			return colors.get(0).name().toLowerCase();
		}
	}

	/**
	 * GUIHolder is a simple InventoryHolder implementation used for creating GUI
	 * inventories.
	 * <p>
	 * In this implementation, getInventory() returns null as it is not used.
	 * </p>
	 */
	public static class GUIHolder implements InventoryHolder {
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
	 * ItemConfig encapsulates configuration details for an item in a GUI, including
	 * material, name, slot range, custom model data, lore, and an optional
	 * ItemsAdder item name or head owner.
	 * <p>
	 * Two constructors are provided: one for regular items and one for player head
	 * items.
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
		private final PotionData basePotionData;
		private final List<PotionEffect> customPotionEffects;
		private final Color potionColor;
		@Nullable
		private final Rarity rarity; // Custom NBT data
		@Nullable
		private final Function<ItemMeta, Void> itemMetaFunction;
		private String name;
		private String slotRange; // e.g., "1-10" or "1,8"
		private boolean clickable;
		private boolean hasEnchantmentEffect = false;

		/**
		 * Constructor for regular items using a list of lore strings.
		 *
		 * @param material
		 *            The material for the item.
		 * @param name
		 *            The display name for the item.
		 * @param slotRange
		 *            The slot range as a string (e.g., "1-10" or "1,8").
		 * @param clickable
		 *            Whether the item is clickable.
		 * @param customModelData
		 *            Custom model data for the item (can be null).
		 * @param itemsAdderName
		 *            The ItemsAdder item name (optional, may be null).
		 * @param lore
		 *            A list of strings representing the lore for the item.
		 * @param nbtData
		 *            A map of custom NBT keys and values (can be empty or null).
		 *
		 * @deprecated In favour of using {@link ItemConfigBuilder}
		 */
		@Deprecated
		public ItemConfig(@Nullable Material material, @NotNull String name, @Nullable String slotRange,
				boolean clickable, @Nullable Integer customModelData, @Nullable String itemsAdderName,
				@NotNull List<String> lore, @Nullable Map<String, Object> nbtData,
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
			this.basePotionData = null;
			this.potionColor = null;
			this.customPotionEffects = null;
			this.itemMetaFunction = null;
		}

		/**
		 * Constructor for regular items using a list of lore strings.
		 *
		 * @param material
		 *            The material for the item.
		 * @param name
		 *            The display name for the item.
		 * @param rarity
		 *            The item color rarity.
		 * @param slotRange
		 *            The slot range as a string (e.g., "1-10" or "1,8").
		 * @param clickable
		 *            Whether the item is clickable.
		 * @param customModelData
		 *            Custom model data for the item (can be null).
		 * @param itemsAdderName
		 *            The ItemsAdder item name (optional, may be null).
		 * @param lore
		 *            A list of strings representing the lore for the item.
		 * @param nbtData
		 *            A map of custom NBT keys and values (can be empty or null).
		 *
		 * @deprecated In favour of using {@link ItemConfigBuilder}
		 */
		@Deprecated
		public ItemConfig(@Nullable Material material, @NotNull String name, @Nullable Rarity rarity,
				@Nullable String slotRange, boolean clickable, @Nullable Integer customModelData,
				@Nullable String itemsAdderName, @NotNull List<String> lore, @Nullable Map<String, Object> nbtData,
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
			this.basePotionData = null;
			this.potionColor = null;
			this.customPotionEffects = null;
			this.itemMetaFunction = null;
		}

		/**
		 * Constructor for regular items using a list of lore strings.
		 *
		 * @param material
		 *            The material for the item.
		 * @param name
		 *            The display name for the item.
		 * @param slotRange
		 *            The slot range as a string (e.g., "1-10" or "1,8").
		 * @param clickable
		 *            Whether the item is clickable.
		 * @param customModelData
		 *            Custom model data for the item (can be null).
		 * @param itemsAdderName
		 *            The ItemsAdder item name (optional, may be null).
		 * @param lore
		 *            A list of strings representing the lore for the item.
		 *
		 * @deprecated In favour of using {@link ItemConfigBuilder}
		 */
		@Deprecated
		public ItemConfig(@Nullable Material material, @NotNull String name, @Nullable String slotRange,
				boolean clickable, @Nullable Integer customModelData, @Nullable String itemsAdderName,
				@NotNull List<String> lore, @Nullable ButtonAction buttonAction) {
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
			this.basePotionData = null;
			this.potionColor = null;
			this.customPotionEffects = null;
			this.itemMetaFunction = null;
		}

		/**
		 * Constructor for player head items using a list of lore strings.
		 *
		 * @param material
		 *            The material for the item (typically PLAYER_HEAD).
		 * @param name
		 *            The display name for the item.
		 * @param slotRange
		 *            The slot range as a string.
		 * @param clickable
		 *            Whether the item is clickable.
		 * @param customModelData
		 *            Custom model data for the item (optional).
		 * @param itemsAdderName
		 *            The ItemsAdder item name (optional).
		 * @param lore
		 *            A list of strings representing the lore for the item.
		 * @param headOwner
		 *            The OfflinePlayer representing the head owner.
		 * @param nbtData
		 *            A map of custom NBT keys and values (can be empty or null).
		 *
		 * @deprecated In favour of using {@link ItemConfigBuilder}
		 */
		@Deprecated
		public ItemConfig(@Nullable Material material, @NotNull String name, @Nullable String slotRange,
				boolean clickable, @Nullable Integer customModelData, @Nullable String itemsAdderName,
				@NotNull List<String> lore, @NotNull OfflinePlayer headOwner, @Nullable Map<String, Object> nbtData,
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
			this.basePotionData = null;
			this.potionColor = null;
			this.customPotionEffects = null;
			this.itemMetaFunction = null;
		}

		/**
		 * Constructor for player head items using a list of lore strings.
		 *
		 * @param material
		 *            The material for the item (typically PLAYER_HEAD).
		 * @param name
		 *            The display name for the item.
		 * @param slotRange
		 *            The slot range as a string.
		 * @param clickable
		 *            Whether the item is clickable.
		 * @param customModelData
		 *            Custom model data for the item (optional).
		 * @param itemsAdderName
		 *            The ItemsAdder item name (optional).
		 * @param lore
		 *            A list of strings representing the lore for the item.
		 * @param headOwner
		 *            The OfflinePlayer representing the head owner.
		 *
		 * @deprecated In favour of using {@link ItemConfigBuilder}
		 */
		@Deprecated
		public ItemConfig(@Nullable Material material, @NotNull String name, @Nullable String slotRange,
				boolean clickable, @Nullable Integer customModelData, @Nullable String itemsAdderName,
				@NotNull List<String> lore, @NotNull OfflinePlayer headOwner, @Nullable ButtonAction buttonAction) {
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
			this.basePotionData = null;
			this.potionColor = null;
			this.customPotionEffects = null;
			this.itemMetaFunction = null;
		}

		/**
		 * @deprecated In favour of using {@link ItemConfigBuilder}
		 */
		@Deprecated
		public ItemConfig(@Nullable Material material, @NotNull String name, @Nullable String slotRange,
				boolean clickable, @Nullable Integer customModelData, @Nullable String itemsAdderName,
				@NotNull List<String> lore, @NotNull OfflinePlayer headOwner, @Nullable ButtonAction buttonAction,
				@Nullable PotionData potionData, @Nullable List<PotionEffect> potionEffects,
				@Nullable Color potionColor, @Nullable Rarity rarity) {
			this.material = material;
			this.name = name;
			this.rarity = rarity;
			this.slotRange = slotRange;
			this.clickable = clickable;
			this.customModelData = customModelData;
			this.itemsAdderName = itemsAdderName;
			this.lore = lore;
			this.headOwner = headOwner;
			this.nbtData = new HashMap<>();
			this.buttonAction = buttonAction;
			this.basePotionData = potionData;
			this.potionColor = potionColor;
			this.customPotionEffects = potionEffects;
			this.itemMetaFunction = null;
		}

		/**
		 * @deprecated In favour of using {@link ItemConfigBuilder}
		 */
		@Deprecated
		public ItemConfig(@NotNull Material material, String name, @Nullable Rarity rarity, String slotRange,
						  boolean clickable, Integer customModelData, String itemsAdderName, @NotNull List<String> lore,
						  @Nullable Map<String, Object> nbtData, ButtonAction<?> buttonAction, @Nullable OfflinePlayer headOwner,
						  PotionData basePotionData, List<PotionEffect> customPotionEffects, Color potionColor,
						  boolean hasEnchantmentEffect, Function<ItemMeta, Void> itemMetaFunction) {
			this.material = material;
			this.name = name;
			this.rarity = rarity;
			this.slotRange = slotRange;
			this.clickable = clickable;
			this.customModelData = customModelData;
			this.itemsAdderName = itemsAdderName;
			this.lore = lore;
			this.headOwner = headOwner;
			this.nbtData = nbtData != null ? nbtData : new HashMap<>();
			this.buttonAction = buttonAction;
			this.basePotionData = basePotionData;
			this.potionColor = potionColor;
			this.customPotionEffects = customPotionEffects;
			this.hasEnchantmentEffect = hasEnchantmentEffect;
			this.itemMetaFunction = itemMetaFunction;
		}

		/**
		 * Returns an ItemConfig representing an "air" item. This can be used to fill a
		 * slot with nothing (i.e. a blank placeholder).
		 *
		 * @return an ItemConfig that creates an ItemStack of Material.AIR.
		 */
		public static ItemConfig ofAir() {
			return new ItemConfig(Material.AIR, // Material is AIR
					"", // Empty display name
					"", // No slot range
					false, // Not clickable
					null, // No custom model data
					null, // No ItemsAdder name
					new ArrayList<>(), // Empty lore
					null // No button action (and no head owner, since it's not a head)
			);
		}

		public static ItemConfig fromJson(JsonObject json, JavaPlugin plugin) {
			Material material = Material.getMaterial(json.get("material").getAsString());
			String name = json.get("name").getAsString();
			String slotRange = json.get("slot").getAsString();
			Integer customModelData = null;
			if (json.has("customModelData"))
				customModelData = json.get("customModelData").getAsInt();
			String itemsAdderName = null;
			if (json.has("itemsAdderName"))
				itemsAdderName = json.get("itemsAdderName").getAsString();
			ButtonAction buttonAction = null;
			if (json.has("buttonAction")) {
				JsonObject actionObj = json.getAsJsonObject("buttonAction");
				buttonAction = ButtonAction.fromJson(actionObj, plugin);
			}
			List<String> lore = new ArrayList<>();

			if (json.has("lore")) {
				JsonArray loreArray = json.getAsJsonArray("lore");
				for (JsonElement line : loreArray) {
					lore.add(line.getAsString());
				}
			}

			return new ItemConfig(material, name, slotRange, buttonAction != null, customModelData, itemsAdderName,
					lore, null, null, buttonAction);
		}

		/**
		 * Adds Enchantment glowing effect to the item.
		 */
		public ItemConfig enchant() {
			this.hasEnchantmentEffect = true;
			return this;
		}

		public boolean isEnchanted() {
			return hasEnchantmentEffect;
		}

		private Map<String, Object> getNBTData() {
			return nbtData;
		}

		/**
		 * Adds NBT data to an ItemStack with only allowed types.
		 *
		 * @param key
		 *            The NBT key.
		 * @param <T>
		 *            The type of the value (restricted to UUID, Integer, Enum, Double,
		 *            String, Float or ItemStack).
		 * @return The modified ItemStack with the added NBT data.
		 */
		public <T> ItemConfig addNbtData(String key, PermittedObject<T> valueWrapper) {
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
					throw new IllegalArgumentException("Unsupported NBT type: " + value.getClass().getSimpleName());
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
		 * Gets the transformer for the specified itemMeta. Mostly internal
		 *
		 * @return the meta transforming function
		 */
		@Nullable
		public Function<ItemMeta, Void> getItemMetaFunction() {
			return itemMetaFunction;
		}

		/**
		 * Gets the display name for this item.
		 *
		 * @return the name of the item
		 */
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Nullable
		public Rarity getRarity() {
			return rarity;
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
		 * @return the OfflinePlayer representing the head owner, or null if not
		 *         applicable
		 */
		public OfflinePlayer getHeadOwner() {
			return headOwner;
		}
	}

	public static class ItemConfigFactory {

		/**
		 * Creates an ItemConfig for regular items using Adventure Components for lore.
		 *
		 * @param material
		 *            The material for the item.
		 * @param name
		 *            The display name.
		 * @param slotRange
		 *            The slot range as a string.
		 * @param clickable
		 *            Whether the item is clickable.
		 * @param customModelData
		 *            The custom model data (optional).
		 * @param itemsAdderName
		 *            The ItemsAdder item name (optional).
		 * @param loreComponents
		 *            A list of Adventure Components for the lore.
		 * @return a new ItemConfig instance.
		 */
		public static ItemConfig fromComponents(@Nullable Material material, @NotNull String name,
				@Nullable String slotRange, boolean clickable, @Nullable Integer customModelData,
				@Nullable String itemsAdderName, @NotNull List<Component> loreComponents,
				@Nullable Map<String, Object> nbtData, @Nullable ButtonAction buttonAction) {
			List<String> lore = loreComponents.stream()
					.map(component -> LegacyComponentSerializer.legacySection().serialize(component))
					.collect(Collectors.toList());
			return new ItemConfig(material, name, slotRange, clickable, customModelData, itemsAdderName, lore, nbtData,
					buttonAction);
		}

		/**
		 * Creates an ItemConfig for regular items using Adventure Components for lore.
		 *
		 * @param material
		 *            The material for the item.
		 * @param name
		 *            The display name.
		 * @param slotRange
		 *            The slot range as a string.
		 * @param clickable
		 *            Whether the item is clickable.
		 * @param customModelData
		 *            The custom model data (optional).
		 * @param itemsAdderName
		 *            The ItemsAdder item name (optional).
		 * @param loreComponents
		 *            A list of Adventure Components for the lore.
		 * @return a new ItemConfig instance.
		 */
		public static ItemConfig fromComponents(@Nullable Material material, @NotNull String name,
				@Nullable Rarity rarity, @Nullable String slotRange, boolean clickable,
				@Nullable Integer customModelData, @Nullable String itemsAdderName,
				@NotNull List<Component> loreComponents, @Nullable Map<String, Object> nbtData,
				@Nullable ButtonAction buttonAction) {
			List<String> lore = loreComponents.stream()
					.map(component -> LegacyComponentSerializer.legacySection().serialize(component))
					.collect(Collectors.toList());
			return new ItemConfig(material, name, rarity, slotRange, clickable, customModelData, itemsAdderName, lore,
					nbtData, buttonAction);
		}

		/**
		 * Creates an ItemConfig for regular items using Adventure Components for lore.
		 *
		 * @param material
		 *            The material for the item.
		 * @param name
		 *            The display name.
		 * @param slotRange
		 *            The slot range as a string.
		 * @param clickable
		 *            Whether the item is clickable.
		 * @param customModelData
		 *            The custom model data (optional).
		 * @param itemsAdderName
		 *            The ItemsAdder item name (optional).
		 * @param loreComponents
		 *            A list of Adventure Components for the lore.
		 * @return a new ItemConfig instance.
		 */
		public static ItemConfig fromComponents(@Nullable Material material, @NotNull String name,
				@Nullable String slotRange, boolean clickable, @Nullable Integer customModelData,
				@Nullable String itemsAdderName, @Nullable List<Component> loreComponents,
				@Nullable ButtonAction buttonAction) {
			List<String> lore = loreComponents.stream()
					.map(component -> LegacyComponentSerializer.legacySection().serialize(component))
					.collect(Collectors.toList());
			return new ItemConfig(material, name, slotRange, clickable, customModelData, itemsAdderName, lore,
					buttonAction);
		}

		/**
		 * Creates an ItemConfig for player head items using Adventure Components for
		 * lore.
		 *
		 * @param material
		 *            The material for the item.
		 * @param name
		 *            The display name.
		 * @param slotRange
		 *            The slot range as a string.
		 * @param clickable
		 *            Whether the item is clickable.
		 * @param customModelData
		 *            The custom model data (optional).
		 * @param itemsAdderName
		 *            The ItemsAdder item name (optional).
		 * @param loreComponents
		 *            A list of Adventure Components for the lore.
		 * @param headOwner
		 *            The OfflinePlayer representing the head owner.
		 * @return a new ItemConfig instance.
		 */
		public static ItemConfig fromComponents(@Nullable Material material, @NotNull String name,
				@Nullable String slotRange, boolean clickable, @Nullable Integer customModelData,
				@Nullable String itemsAdderName, List<Component> loreComponents, @NotNull OfflinePlayer headOwner,
				@Nullable Map<String, Object> nbtData, @Nullable ButtonAction buttonAction) {
			List<String> lore = loreComponents.stream()
					.map(component -> LegacyComponentSerializer.legacySection().serialize(component))
					.collect(Collectors.toList());
			return new ItemConfig(material, name, slotRange, clickable, customModelData, itemsAdderName, lore,
					headOwner, nbtData, buttonAction);
		}

		/**
		 * Creates an ItemConfig for player head items using Adventure Components for
		 * lore.
		 *
		 * @param material
		 *            The material for the item.
		 * @param name
		 *            The display name.
		 * @param slotRange
		 *            The slot range as a string.
		 * @param clickable
		 *            Whether the item is clickable.
		 * @param customModelData
		 *            The custom model data (optional).
		 * @param itemsAdderName
		 *            The ItemsAdder item name (optional).
		 * @param loreComponents
		 *            A list of Adventure Components for the lore.
		 * @param headOwner
		 *            The OfflinePlayer representing the head owner.
		 * @return a new ItemConfig instance.
		 */
		public static ItemConfig fromComponents(@Nullable Material material, @NotNull String name,
				@Nullable String slotRange, boolean clickable, @Nullable Integer customModelData,
				@Nullable String itemsAdderName, List<Component> loreComponents, @NotNull OfflinePlayer headOwner,
				@Nullable ButtonAction buttonAction) {
			List<String> lore = loreComponents.stream()
					.map(component -> LegacyComponentSerializer.legacySection().serialize(component))
					.collect(Collectors.toList());
			return new ItemConfig(material, name, slotRange, clickable, customModelData, itemsAdderName, lore,
					headOwner, buttonAction);
		}
	}

	/**
	 * ItemConfigGroup encapsulates a group of {@link ItemConfig} objects that are
	 * arranged together in either a vertical or horizontal layout. All items in the
	 * group share the same button action.
	 *
	 * @param commonAction
	 *            If non-null, this action is applied to every item in the group.
	 */
	public record ItemConfigGroup(GuiCreator.ItemConfigGroup.Orientation orientation, List<ItemConfig> items,
			ButtonAction<?> commonAction) {

		/**
		 * Constructs an ItemConfigGroup.
		 *
		 * @param orientation
		 *            the orientation (VERTICAL or HORIZONTAL) of the group.
		 * @param items
		 *            the list of ItemConfigs in this group.
		 * @param commonAction
		 *            the common ButtonAction to assign to all items (may be null).
		 */
		public ItemConfigGroup(Orientation orientation, List<ItemConfig> items, ButtonAction<?> commonAction) {
			this.orientation = Objects.requireNonNull(orientation, "Orientation cannot be null");
			this.items = Objects.requireNonNull(items, "Items list cannot be null");
			this.commonAction = commonAction;
		}

		/**
		 * Returns the orientation of the group.
		 *
		 * @return the orientation.
		 */
		@Override
		public Orientation orientation() {
			return orientation;
		}

		/**
		 * Returns the list of ItemConfigs in this group.
		 *
		 * @return the list of items.
		 */
		@Override
		public List<ItemConfig> items() {
			return items;
		}

		/**
		 * Returns the common ButtonAction for this group.
		 *
		 * @return the common ButtonAction, or null if none.
		 */
		@Override
		public ButtonAction<?> commonAction() {
			return commonAction;
		}

		/**
		 * Represents the orientation of the group.
		 */
		public enum Orientation {
			VERTICAL, HORIZONTAL
		}
	}

	/**
	 * ItemConfigClass represents a collection of item groups (and optionally
	 * individual ItemConfigs) that are intended to be displayed on a single GUI
	 * page. The {@code orderIndex} determines the order in which these classes
	 * appear (lower numbers first).
	 */
	public record ItemConfigClass(int orderIndex, Map<Integer, Object> elements) {
		/**
		 * Constructs an ItemConfigClass.
		 *
		 * @param orderIndex
		 *            the order index (lower numbers appear first).
		 * @param elements
		 *            the Configurable Elements to be in the class
		 */
		public ItemConfigClass(int orderIndex, Map<Integer, Object> elements) {
			// Validate that all values are of the allowed types.
			elements.values().forEach(e -> {
				if (!(e instanceof GuiCreator.ItemConfig || e instanceof ItemConfigGroup)) {
					throw new IllegalArgumentException("Element must be either an ItemConfig or an ItemConfigGroup");
				}
			});
			this.elements = new TreeMap<>(Objects.requireNonNull(elements, "Elements map cannot be null"));
			this.orderIndex = orderIndex;
		}

		/**
		 * Returns the order index.
		 *
		 * @return the order index.
		 */
		@Override
		public int orderIndex() {
			return orderIndex;
		}

		/**
		 * Returns the map of elements.
		 *
		 * @return a sorted map of slot position to element.
		 */
		public Map<Integer, Object> getElements() {
			return elements;
		}
	}

	/**
	 * Fluent builder for {@link ItemConfig}. Usage: ItemConfig config =
	 * ItemConfigBuilder.from(Material.DIAMOND_SWORD) .setName("Cool Sword")
	 * .addLoreLine("Sharp!") .build();
	 */
	public final static class ItemConfigBuilder {

		private Material material = Material.STONE;
		private String name = "";
		private final List<PotionEffect> customPotionEffects = new ArrayList<>();
		@Nullable
		private Rarity rarity = null;
		private boolean clickable = false;
		@Nullable
		private String slotRange = null;
		@Nullable
		private Integer customModelData = null;
		private List<String> lore = new ArrayList<>();
		@Nullable
		private String itemsAdderName = null;
		private Map<String, Object> nbtData = new HashMap<>();
		@Nullable
		private OfflinePlayer headOwner = null;
		private boolean hasEnchantmentEffect = false;
		@Nullable
		private ButtonAction<?> buttonAction = null;
		// Potion stuff
		@Nullable
		private PotionData basePotionData = null;
		@Nullable
		private Color potionColor = null;
		@Nullable
		private Function<ItemMeta, Void> itemMetaTransformer = (it) -> null;

		private ItemConfigBuilder(Material material) {
			this.material = material;
		}

		/**
		 * Entry point: ItemConfigBuilder.from(Material).[…]
		 */
		public static ItemConfigBuilder from(Material material) {
			return new ItemConfigBuilder(material);
		}

		public ItemConfigBuilder setMaterial(Material material) {
			this.material = material;
			return this;
		}

		public ItemConfigBuilder setName(String name) {
			this.name = name;
			return this;
		}

		public ItemConfigBuilder setRarity(@Nullable Rarity rarity) {
			this.rarity = rarity;
			return this;
		}

		public ItemConfigBuilder setSlotRange(@Nullable String slotRange) {
			this.slotRange = slotRange;
			return this;
		}

		public ItemConfigBuilder setClickable(boolean clickable) {
			this.clickable = clickable;
			return this;
		}

		public ItemConfigBuilder setCustomModelData(@Nullable Integer customModelData) {
			this.customModelData = customModelData;
			return this;
		}

		public ItemConfigBuilder setItemsAdderName(@Nullable String itemsAdderName) {
			this.itemsAdderName = itemsAdderName;
			return this;
		}

		public ItemConfigBuilder setLore(List<String> lore) {
			this.lore = new ArrayList<>(lore);
			return this;
		}

		public ItemConfigBuilder addLoreLine(String line) {
			this.lore.add(line);
			return this;
		}

		public ItemConfigBuilder setHeadOwner(@Nullable OfflinePlayer headOwner) {
			this.headOwner = headOwner;
			return this;
		}

		public ItemConfigBuilder setNbtData(Map<String, Object> nbtData) {
			this.nbtData = new HashMap<>(nbtData);
			return this;
		}

		public ItemConfigBuilder addNbtData(String key, Object value) {
			this.nbtData.put(key, value);
			return this;
		}

		public ItemConfigBuilder setButtonAction(@Nullable ButtonAction<?> buttonAction) {
			this.buttonAction = buttonAction;
			return this;
		}

		public ItemConfigBuilder setEnchanted(boolean enchanted) {
			this.hasEnchantmentEffect = enchanted;
			return this;
		}

		public ItemConfigBuilder setBasePotionData(@Nullable PotionData potionData) {
			this.basePotionData = potionData;
			return this;
		}

		public ItemConfigBuilder addCustomPotionEffect(PotionEffect effect) {
			this.customPotionEffects.add(effect);
			return this;
		}

		public ItemConfigBuilder setPotionColor(@Nullable Color color) {
			this.potionColor = color;
			return this;
		}

		public ItemConfigBuilder setItemMetaTransformer(@Nullable Function<ItemMeta, Void> itemMetaTransformer) {
			this.itemMetaTransformer = itemMetaTransformer;
			return this;
		}

		/**
		 * Build an {@link ItemConfig} with the current builder state.
		 *
		 * NOTE: This uses the same constructor argument order as your original build()
		 * call. If your actual ItemConfig constructor differs, adjust the argument
		 * order here.
		 */
		public ItemConfig build() {
			return new ItemConfig(material, name, rarity, slotRange, clickable, customModelData, itemsAdderName, lore,
					nbtData, buttonAction, headOwner, basePotionData, customPotionEffects, potionColor,
					hasEnchantmentEffect, itemMetaTransformer);
		}
	}
}
