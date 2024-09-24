package org.v_utls.guiparent;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.v_utls.listeners.BaseGuiListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuiCreator {

    private final JavaPlugin plugin;
    private final BaseGuiListener listener;

    public GuiCreator(JavaPlugin plugin, BaseGuiListener listener) {
        this.plugin = plugin;
        this.listener = listener;
    }

    boolean canBeMade = false;

    /**
     * Opens a customizable GUI with optional texture.
     *
     * @param player      The player to open the GUI for.
     * @param height      The number of rows in the GUI.
     * @param width       The number of slots per row.
     * @param title       The title of the GUI.
     * @param textured    Whether the GUI should be textured.
     * @param textureKey  The texture key (optional, required if textured = true, if textured is false make the value null).
     * @param itemConfigs Configuration for which items should be placed in which slots.
     */
    public void openGUI(Player player, int height, int width, String title, boolean textured, String textureKey, int offset, ItemConfig... itemConfigs) {
        if (height < 1 || width < 1 || width > 9) {
            player.sendMessage("Invalid dimensions for the GUI.");
            return;
        }

        int slots = height * width;
        Plugin itemsAdder = Bukkit.getPluginManager().getPlugin("ItemsAdder");
        Inventory inventory;

        if (textured && textureKey != null && !textureKey.isEmpty() && itemsAdder != null) {
            FontImageWrapper texture = new FontImageWrapper(textureKey);
            if (!texture.exists()) {
                Bukkit.getLogger().warning("Gui Texture: " + textureKey + " Does not exist");
                inventory = Bukkit.createInventory(new GUIHolder(), slots, title);
            } else {
                inventory = Bukkit.createInventory(new GUIHolder(), slots, title);
                canBeMade = true;
            }
        } else {
            inventory = Bukkit.createInventory(new GUIHolder(), slots, title);
        }

        for (ItemConfig itemConfig : itemConfigs) {
            Set<Integer> slotSet = parseSlots(itemConfig.getSlotRange(), width);
            for (int slot : slotSet) {
                if (slot < slots) {
                    ItemStack item = createItem(itemConfig, player); // Pass player here
                    inventory.setItem(slot, item);
                }
            }
        }


        // Set the inventory in the listener
        if (canBeMade) {
            FontImageWrapper texture = new FontImageWrapper(textureKey);
            listener.setGuiInventory(inventory);
            player.openInventory(inventory);
            TexturedInventoryWrapper.setPlayerInventoryTexture(player, texture, title, 0, offset);
        } else {
            listener.setGuiInventory(inventory);
            player.openInventory(inventory);
        }
    }

    /**
     * Parses a slot range string (e.g., "1-10", "1,8") into a set of integers representing the slot numbers.
     *
     * @param slotRange The slot range string.
     * @param width     The width of the GUI to ensure 1-based indexing.
     * @return A set of slot indices (0-based).
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
     * Helper method to create an ItemStack with a custom display name, lore, and custom model data.
     * If the item is an ItemsAdder item, it uses the ItemsAdder API to create the item.
     *
     * @param itemConfig The configuration for the item (whether it's a regular item or ItemsAdder item).
     * @return The created ItemStack.
     */
    private ItemStack createItem(ItemConfig itemConfig, Player player) {
        ItemStack item;

        // If it's an ItemsAdder item, create it using the ItemsAdder API
        if (itemConfig.isItemsAdderItem()) {
            CustomStack customItem = CustomStack.getInstance(itemConfig.getItemsAdderName());

            if (customItem != null) {
                ItemMeta meta = customItem.getItemStack().getItemMeta();

                // Special case: set player head meta if the ItemsAdder item is "vicky_utls:player_head_gui_size_1"
                if ("vicky_utls:player_head_gui_size_1".equals(itemConfig.getItemsAdderName())) {
                    if (meta instanceof org.bukkit.inventory.meta.SkullMeta) {
                        org.bukkit.inventory.meta.SkullMeta skullMeta = (org.bukkit.inventory.meta.SkullMeta) meta;
                        skullMeta.setOwningPlayer(player); // Set the head to the player who opened the inventory
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
                plugin.getLogger().warning("ItemsAdder item '" + itemConfig.getItemsAdderName() + "' could not be found!");
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
                    skullMeta.setOwningPlayer(player); // Set the head to the player who opened the inventory
                }
            }

            if (itemConfig.getCustomModelData() != null) {
                meta.setCustomModelData(itemConfig.getCustomModelData());
            }
            item.setItemMeta(meta);
        }
        return item;
    }


    // Custom InventoryHolder class to hold the GUI inventory
    private static class GUIHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    /**
     * Configuration class for items to be placed in the GUI.
     */
    public static class ItemConfig {
        private final Material material;
        private final String name;
        private final String slotRange;  // e.g., "1-10" or "1,8"
        private final boolean clickable;
        private final Integer customModelData;  // Custom model data (optional)
        private final String itemsAdderName;    // ItemsAdder item name (optional)
        private final List<String> lore;        // New field for lore

        public ItemConfig(Material material, String name, String slotRange, boolean clickable, Integer customModelData, String itemsAdderName, List<String> lore) {
            this.material = material;
            this.name = name;
            this.slotRange = slotRange;
            this.clickable = clickable;
            this.customModelData = customModelData;
            this.itemsAdderName = itemsAdderName;
            this.lore = lore;  // Assign lore to the item config
        }

        public Material getMaterial() {
            return material;
        }

        public String getName() {
            return name;
        }

        public String getSlotRange() {
            return slotRange;
        }

        public Integer getCustomModelData() {
            return customModelData;
        }

        public boolean isClickable() {
            return clickable;
        }

        public boolean isItemsAdderItem() {
            return itemsAdderName != null;
        }

        public String getItemsAdderName() {
            return itemsAdderName;
        }

        public List<String> getLore() {  // New getter for lore
            return lore;
        }
    }
}
