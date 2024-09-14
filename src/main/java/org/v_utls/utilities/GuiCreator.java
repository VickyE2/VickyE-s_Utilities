package org.v_utls.utilities;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GuiCreator {

    private final JavaPlugin plugin;

    public GuiCreator(JavaPlugin plugin) {
        this.plugin = plugin; // Set the GUI title here
    }

    /**
     * Opens a customizable GUI with optional texture.
     *
     * @param player      The player to open the GUI for.
     * @param height      The number of rows in the GUI.
     * @param width       The number of slots per row.
     * @param title       The title of the GUI.
     * @param textured    Whether the GUI should be textured.
     * @param textureKey  The texture key (optional, required if textured = true).
     * @param itemConfigs Configuration for which items should be placed in which slots.
     */
    public void openGUI(Player player, int height, int width, String title, boolean textured, String textureKey, ItemConfig... itemConfigs) {
        if (height < 1 || width < 1 || width > 9) {
            player.sendMessage("Invalid dimensions for the GUI.");
            return;
        }

        int slots = height * width;
        Plugin itemsAdder = Bukkit.getPluginManager().getPlugin("ItemsAdder");

        Inventory inventory;

        if (textured && textureKey != null && !textureKey.isEmpty() && itemsAdder != null) {
            FontImageWrapper texture = new FontImageWrapper(textureKey);
            TexturedInventoryWrapper texturedInventory = new TexturedInventoryWrapper(null, slots, title, texture);
            inventory = texturedInventory.getInternal();
        } else {
            inventory = Bukkit.createInventory(new GUIHolder(), slots, title);  // Set the title here
        }

        // Apply item configurations (place items in the GUI)
        for (ItemConfig itemConfig : itemConfigs) {
            Set<Integer> slotSet = parseSlots(itemConfig.getSlotRange(), width);
            ItemStack item = createItem(itemConfig.getMaterial(), itemConfig.getName(), itemConfig.customModelData());
            for (int slot : slotSet) {
                if (slot < slots) {
                    inventory.setItem(slot, item);
                }
            }
        }

        player.openInventory(inventory);
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
     * Helper method to create an ItemStack with a custom display name and custom model data.
     *
     * @param material      The material for the item.
     * @param name          The display name of the item.
     * @param customModelData The custom model data to be applied to the item (optional).
     * @return The created ItemStack.
     */
    private ItemStack createItem(Material material, String name, Integer customModelData) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);  // Set custom model data
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
        private final String slotRange; // e.g., "1-10" or "1,8"
        private final boolean clickable;
        private final Integer customModelData;  // Custom model data (optional)

        public ItemConfig(Material material, String name, String slotRange, boolean clickable, Integer customModelData) {
            this.material = material;
            this.name = name;
            this.slotRange = slotRange;
            this.clickable = clickable;
            this.customModelData = customModelData;  // Can be null
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

        public Integer customModelData() {
            return customModelData;
        }

        public boolean isClickable() {
            return clickable;
        }
    }
}

