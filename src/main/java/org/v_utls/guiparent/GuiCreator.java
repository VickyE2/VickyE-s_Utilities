/* Licensed under Apache-2.0 2024. */
package org.v_utls.guiparent;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import dev.lone.itemsadder.api.FontImages.TexturedInventoryWrapper;
import java.util.*;
import java.util.function.BiFunction;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.v_utls.listeners.BaseGuiListener;

public class GuiCreator {

  private final JavaPlugin plugin;
  private final BaseGuiListener listener;

  public GuiCreator(JavaPlugin plugin, BaseGuiListener listener) {
    this.plugin = plugin;
    this.listener = listener;
  }

  boolean canBeMade = false;

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
            org.bukkit.inventory.meta.SkullMeta skullMeta =
                (org.bukkit.inventory.meta.SkullMeta) meta;
            if (itemConfig.getHeadOwner() == null) {
              skullMeta.setOwningPlayer(player);
            } else {
              skullMeta.setOwningPlayer(
                  itemConfig.getHeadOwner()); // Set the head to the specified owner
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
        plugin
            .getLogger()
            .warning(
                "ItemsAdder item '" + itemConfig.getItemsAdderName() + "' could not be found!");
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
          org.bukkit.inventory.meta.SkullMeta skullMeta =
              (org.bukkit.inventory.meta.SkullMeta) meta;
          if (itemConfig.getHeadOwner() == null) {
            skullMeta.setOwningPlayer(player);
          } else {
            skullMeta.setOwningPlayer(
                itemConfig.getHeadOwner()); // Set the head to the specified owner
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

  private static class GUIHolder implements InventoryHolder {
    @Override
    public Inventory getInventory() {
      return null; // Not implemented
    }
  }

  public static class ItemConfig {
    private final Material material;
    private final String name;
    private final String slotRange; // e.g., "1-10" or "1,8"
    private final boolean clickable;
    private final Integer customModelData; // Custom model data (optional)
    private final String itemsAdderName; // ItemsAdder item name (optional)
    private final List<String> lore; // New field for lore
    private final OfflinePlayer headOwner; // New field for head owner's player

    // Default constructor for regular items
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

    // New constructor for player heads
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

    // Getter methods...

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

    public List<String> getLore() {
      return lore;
    }

    public OfflinePlayer getHeadOwner() {
      return headOwner;
    }
  }
}
