/* Licensed under Apache-2.0 2024. */
package org.vicky.guiparent.subGui;

import static org.vicky.guiparent.GuiCreator.createItem;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.guiparent.BaseGui;
import org.vicky.guiparent.GuiCreator;
import org.vicky.listeners.BaseGuiListener;

public abstract class PagedGui extends BaseGui {
  private final int itemsPerPage;
  private final int currentPage;
  private final String pageSlots;
  private final int guiHeight;
  private final List<GuiCreator.ItemConfig> items = new ArrayList<>();
  protected final Map<String, Object> overrides = new HashMap<>();

  public PagedGui(
      JavaPlugin plugin,
      BaseGuiListener listener,
      int currentPage,
      int itemsPerPage,
      int guiHeight,
      String pageSlots) {
    super(plugin, listener, GuiType.PAGED); // Use null or pass a custom listener if needed
    this.itemsPerPage = itemsPerPage;
    this.pageSlots = pageSlots;
    this.guiHeight = guiHeight;
    this.currentPage = Math.max(1, currentPage);
  }

  /**
   * Displays the GUI to the specified player.
   * <p>
   * Each subclass must implement this method to open its particular GUI.
   * </p>
   *
   * @param player the player to whom the GUI is shown
   */
  @Override
  public final void showGui(Player player) {
    callableAddItems(player)
        .whenComplete(
            (result, ex) -> {
              if (ex != null) {
                plugin.getLogger().severe("Error in callableAddItems: " + ex.getMessage());
                ex.printStackTrace();
                return;
              }
              guiManager.openPaginatedGUI(
                  player,
                  guiHeight,
                  (GuiCreator.ArrowGap)
                      overrides.getOrDefault("arrowGap", GuiCreator.ArrowGap.SMALL),
                  getRegistered(),
                  items,
                  pageSlots,
                  currentPage - 1,
                  itemsPerPage,
                  (String) overrides.getOrDefault("title", ""),
                  (Boolean) overrides.getOrDefault("MPB", true),
                  (Boolean) overrides.getOrDefault("textured", false),
                  (String) overrides.getOrDefault("textureKey", ""),
                  (int) overrides.getOrDefault("offset", 0),
                  (Boolean) overrides.getOrDefault("fluent", false));
            });
  }

  protected abstract CompletableFuture<Void> callableAddItems(Player player);

  @Override
  protected CompletableFuture<Inventory> buildInventory(Player player) {
    CompletableFuture<Inventory> futureInventory = new CompletableFuture<>();
    Inventory inventory = Bukkit.createInventory(new GuiCreator.GUIHolder(), guiHeight * 9, "");
    callableAddItems(player)
        .thenRun(
            () ->
                Bukkit.getScheduler()
                    .runTask(
                        plugin,
                        () -> {
                          int start = (currentPage - 1) * itemsPerPage;
                          int end = Math.min(start + itemsPerPage, items.size());
                          List<GuiCreator.ItemConfig> pageItems = items.subList(start, end);
                          Set<Integer> slotSet = GuiCreator.parseSlots(getPageSlots());
                          List<Integer> sortedSlots = new ArrayList<>(slotSet);
                          Collections.sort(sortedSlots); // make sure they're in predictable order

                          int limit = Math.min(pageItems.size(), sortedSlots.size());

                          for (int i = 0; i < limit; i++) {
                            GuiCreator.ItemConfig itemConfig = pageItems.get(i);
                            ItemStack item = createItem(itemConfig, player, plugin);
                            int targetSlot = sortedSlots.get(i);

                            inventory.setItem(targetSlot, item);
                          }
                          /*
                          var spacing =
                              (GuiCreator.ArrowGap)
                                  overrides.getOrDefault("arrowGap", GuiCreator.ArrowGap.SMALL);
                          Optional<DatabasePlayer> opt =
                              new DatabasePlayerDAO().findById(player.getUniqueId());
                          if (opt.isEmpty()) {
                            throw new RuntimeException(
                                "Player that made request cannot be found on the database");
                          }
                          DatabasePlayer dbPlayer = opt.get();
                          String themeId = dbPlayer.getUserTheme();
                          int centerSlot = ((guiHeight - 1) * 9) + 4;
                          int prevSlot = Math.max(0, centerSlot - spacing.gap);
                          int nextSlot = Math.min((9 * guiHeight) - 1, centerSlot + spacing.gap);
                          if (currentPage > 1) {
                            int finalPage = currentPage;
                            GuiCreator.ItemConfig prevConfig =
                                new GuiCreator.ItemConfig(
                                    Material.ARROW,
                                    "ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ (" + (currentPage - 1) + ")",
                                    Integer.toString(prevSlot + 1),
                                    true,
                                    null,
                                    "vicky_themes:left_arrow_" + themeId,
                                    List.of(ChatColor.GREEN + "ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ᴛᴏ ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ"),
                                    ButtonAction.ofRunCode(
                                        p ->
                                            guiManager.openPaginatedGUI(
                                                p,
                                                guiHeight,
                                                spacing,
                                                getRegistered(),
                                                pageItems,
                                                getPageSlots(),
                                                finalPage - 1,
                                                itemsPerPage,
                                                (String) overrides.getOrDefault("title", ""),
                                                (Boolean) overrides.getOrDefault("MPB", true),
                                                (Boolean) overrides.getOrDefault("textured", false),
                                                (String) overrides.getOrDefault("textureKey", ""),
                                                (int) overrides.getOrDefault("offset", 0),
                                                (Boolean) overrides.getOrDefault("fluent", false)),
                                        true));
                            inventory.setItem(prevSlot, createItem(prevConfig, player, plugin));
                            listener.registerButton(inventory, prevConfig);
                          }
                          else {
                            GuiCreator.ItemConfig prevConfig =
                                new GuiCreator.ItemConfig(
                                    Material.ARROW,
                                    "ɴᴏ ᴘʀᴇᴠɪᴏᴜs ᴘᴀɢᴇ",
                                    Integer.toString(prevSlot + 1),
                                    true,
                                    null,
                                    "vicky_themes:left_arrow_" + themeId,
                                    List.of(ChatColor.GREEN + "ᴛʜɪs ɪs ᴛʜᴇ ғɪʀsᴛ ᴘᴀɢᴇ"),
                                    null);
                            inventory.setItem(prevSlot, createItem(prevConfig, player, plugin));
                          }
                          // Create next page button.
                          if (end < items.size()) {
                            int finalPage = currentPage;
                            GuiCreator.ItemConfig nextConfig =
                                new GuiCreator.ItemConfig(
                                    Material.ARROW,
                                    "ɴᴇxᴛ ᴘᴀɢᴇ (" + (currentPage + 1) + ")",
                                    Integer.toString(nextSlot + 1),
                                    true,
                                    null,
                                    "vicky_themes:right_arrow_" + themeId,
                                    List.of(ChatColor.GREEN + "ᴄʟɪᴄᴋ ᴛᴏ ɢᴏ ᴛᴏ ɴᴇxᴛ ᴘᴀɢᴇ"),
                                    ButtonAction.ofRunCode(
                                        p ->
                                            guiManager.openPaginatedGUI(
                                                p,
                                                guiHeight,
                                                spacing,
                                                getRegistered(),
                                                pageItems,
                                                getPageSlots(),
                                                finalPage + 1,
                                                itemsPerPage,
                                                (String) overrides.getOrDefault("title", ""),
                                                (Boolean) overrides.getOrDefault("MPB", true),
                                                (Boolean) overrides.getOrDefault("textured", false),
                                                (String) overrides.getOrDefault("textureKey", ""),
                                                (int) overrides.getOrDefault("offset", 0),
                                                (Boolean) overrides.getOrDefault("fluent", false)),
                                        true));
                            inventory.setItem(nextSlot, createItem(nextConfig, player, plugin));
                            listener.registerButton(inventory, nextConfig);
                          }
                          else {
                            GuiCreator.ItemConfig nextConfig =
                                new GuiCreator.ItemConfig(
                                    Material.ARROW,
                                    "ɴᴇxᴛ ᴘᴀɢᴇ (0)",
                                    Integer.toString(nextSlot + 1),
                                    true,
                                    null,
                                    "vicky_themes:right_arrow_" + themeId,
                                    List.of(ChatColor.GREEN + "ᴛʜɪs ɪs ᴛʜᴇ ʟᴀsᴛ ᴘᴀɢᴇ"),
                                    null);
                            inventory.setItem(nextSlot, createItem(nextConfig, player, plugin));
                          }
                          plugin
                              .getLogger()
                              .info("Inventory populated post-async for " + player.getName());
                          */
                          futureInventory.complete(
                              inventory); // Complete the future with the populated inventory
                        }));
    return futureInventory; // Return the CompletableFuture
  }

  protected void registerPagedItem(GuiCreator.ItemConfig config) {
    this.items.add(config);
  }

  protected void registerPagedItems(GuiCreator.ItemConfig... config) {
    this.items.addAll(Arrays.asList(config));
  }

  protected void registerPagedItems(List<GuiCreator.ItemConfig> config) {
    this.items.addAll(config);
  }

  public int getItemsPerPage() {
    return itemsPerPage;
  }

  public String getPageSlots() {
    return pageSlots;
  }

  public int getPage() {
    return currentPage;
  }

  public List<GuiCreator.ItemConfig> getItems() {
    return items;
  }
}
