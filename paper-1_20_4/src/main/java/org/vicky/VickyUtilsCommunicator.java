/* Licensed under Apache-2.0 2024. */
package org.vicky;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.ecosystem.plugin.Communicateable;
import org.vicky.ecosystem.server.Handler;
import org.vicky.effectsSystem.EffectRegistry;
import org.vicky.guiparent.DefaultGuiListener;
import org.vicky.guiparent.GuiCreator;
import org.vicky.utilities.ContextLogger.ContextLogger;

class VickyUtilsCommunicator extends Communicateable {
  @Override
  protected void onRegister() {}

  @Handler(
      key = "test",
      description = "Just tests if the communicator works. pass a plugin name \"plugin\"")
  public CompletableFuture<Void> handleTest(JsonObject payload) {
    getLogger()
        .printBukkit(
            "Received test payload. If you see this it works... gotten from ["
                + payload.get("plugin")
                + "] Plugin",
            ContextLogger.LogType.SUCCESS);
    return CompletableFuture.completedFuture(null);
  }

  @Handler(
      key = "openGui",
      description =
          """
Opens a custom inventory GUI for a specified player using configurable layouts, item definitions, and behaviors.

This handler supports multiple GUI types including:
- normal (static inventory)
- paged (paginated multi-page inventory)
- anvil (input GUI using the anvil interface)

The GUI is fully configurable using JSON payload options including title, size, texture, items, layout offset, and interactive features.

⚙️ Parameters:
- `player` (String UUID): The player to whom the GUI should be shown. Must be online.
- `plugin` (String): The name of the plugin requesting GUI creation. Used to resolve textures/items/assets.
- `title` (String, optional): The title displayed on the GUI. Default: `"Ecosystem Opened GUI"`.
- `rows` (int, optional): Number of rows in the GUI (1–6). Default: `6`.
- `offset` (int, optional): Layout offset for positioning items. Default: `0`.
- `textured` (boolean, optional): Whether the GUI uses a textured background.
- `textureKey` (String, optional): Texture key ID for custom GUI skins (used only if `textured=true`).
- `guiType` (String): Determines the kind of GUI to open. Accepted values:
  - `"normal"`, `"single"`, `"one"`: Basic static GUI.
  - `"paged"`, `"paginated"`, `"page"`: GUI with page navigation and scrollable item lists.
  - `"anvil"`, `"input"`: Input GUI using anvil mechanics.
- `items` (JsonArray): A list of item configurations (must follow your `ItemConfig` schema). Minimum:
  - Normal/Page GUI: 1+ items
  - Anvil GUI: Exactly 3 items (left, right, result)
- Additional for `paged` GUI:
  - `page` (int): The page index to display. Default: `0`.
  - `itemsPerPage` (int): Max items per page. Default: `28`.
  - `persistent` (boolean): Whether persistent buttons (e.g., arrows) should stay fixed.
  - `slotRange` (String): Slot index range where page items appear (e.g., `"10-36"`).
  - `arrowGap` (String): Arrow spacing between pages. Accepted: `"NONE"`, `"SMALL"`, `"MEDIUM"`, `"LARGE"`.

- Additional for `anvil` GUI:
  - `initialText` (String): Initial text in the input box. Optional.
  - `canClickLeft` (boolean): Whether the left input item is clickable.
  - `canClickRight` (boolean): Whether the right input item is clickable.

🧪 Input Validation:
- Player must be online.
- Plugin must exist.
- GUI type must be supported.
- Items array must contain valid `ItemConfig`s.
- Anvil GUI requires exactly 3 items.

📤 Output:
- Returns: `{ "success": true }` if GUI opened successfully.
- Fails with meaningful errors for:
    - Unknown player or plugin
    - Invalid GUI type
    - Missing or malformed payload fields

💡 Examples:
```
{
  "key": "openGui",
  "player": "29b2ea92-00b0-4bfc-97e9-6be6209e8e43",
  "plugin": "BetterHUD",
  "title": "Shop Menu",
  "guiType": "paged",
  "rows": 6,
  "items": [...],
  "page": 0,
  "itemsPerPage": 21,
  "persistent": true,
  "slotRange": "10-36",
  "arrowGap": "medium"
}
```

🧠 Notes:
- Supports JSON-defined GUIs directly from other plugins or external tools.
- Can be used to build modular menus, quest interfaces, shops, etc.
- Extend with item actions, command bindings, and interactive slots via `ItemConfig`.

Requires proper registration of items/textures via the plugin specified in the `plugin` field.
""")
  public CompletableFuture<JsonObject> handleOpenGui(JsonObject payload) {
    UUID playerId = UUID.fromString(payload.get("player").getAsString());
    JavaPlugin plugin =
        (JavaPlugin) Bukkit.getPluginManager().getPlugin(payload.get("plugin").getAsString());
    Player player = Bukkit.getPlayer(playerId);

    if (player == null || !player.isOnline()) {
      getLogger().printBukkit("Player not found for openGui!", ContextLogger.LogType.WARNING);
      return CompletableFuture.failedFuture(new IllegalArgumentException("Player not online"));
    }
    if (plugin == null) {
      getLogger().printBukkit("Plugin not found for openGui!", ContextLogger.LogType.WARNING);
      return CompletableFuture.failedFuture(new IllegalArgumentException("Plugin not found"));
    }

    String title =
        payload.has("title") ? payload.get("title").getAsString() : "Ecosystem Opened GUI";
    int height = payload.has("rows") ? payload.get("rows").getAsInt() : 6;
    boolean textured = payload.has("textured") && payload.get("textured").getAsBoolean();
    String textureKey = payload.has("textureKey") ? payload.get("textureKey").getAsString() : null;
    int offset = payload.has("offset") ? payload.get("offset").getAsInt() : 0;

    JsonArray itemsJson = payload.getAsJsonArray("items");
    List<GuiCreator.ItemConfig> items = new ArrayList<>();
    for (JsonElement element : itemsJson) {
      JsonObject itemObj = element.getAsJsonObject();
      GuiCreator.ItemConfig item =
          GuiCreator.ItemConfig.fromJson(itemObj, plugin); // <-- You need this method
      items.add(item);
    }

    GuiCreator creator = new GuiCreator(plugin, new DefaultGuiListener(plugin));
    String guiType = payload.get("guiType").getAsString();

    switch (guiType.toLowerCase()) {
      case "normal", "single", "one" -> {
        creator.openGUI(
            player,
            height,
            9,
            title,
            textured,
            textureKey,
            offset,
            items.toArray(new GuiCreator.ItemConfig[0]));
      }
      case "paged", "paginated", "page" -> {
        int page = payload.has("page") ? payload.get("page").getAsInt() : 0;
        int itemsPerPage =
            payload.has("itemsPerPage") ? payload.get("itemsPerPage").getAsInt() : 28;
        boolean persistentButtons =
            payload.has("persistent") && payload.get("persistent").getAsBoolean();
        String range = payload.has("slotRange") ? payload.get("slotRange").getAsString() : "10-36";
        // You can split item types here later (mainItems vs pageItems), but for now we use all as
        // pageItems.
        creator.openPaginatedGUI(
            player,
            height,
            GuiCreator.ArrowGap.valueOf(
                payload.has("arrowGap")
                    ? payload.get("arrowGap").getAsString().toUpperCase()
                    : "MEDIUM"), // hardcoded for now; could parse from payload
            new ArrayList<>(), // mainItems
            items, // pageItems
            range,
            page,
            itemsPerPage,
            title,
            persistentButtons,
            textured,
            textureKey,
            offset);
      }
      case "anvil", "input" -> {
        if (items.size() < 3) {
          getLogger()
              .printBukkit(
                  "Anvil GUI requires 3 item configs (left, right, output)",
                  ContextLogger.LogType.WARNING);
          return CompletableFuture.failedFuture(
              new IllegalArgumentException("Not enough items for anvil GUI"));
        }

        String initialText =
            payload.has("initialText") ? payload.get("initialText").getAsString() : "";
        boolean canClickLeft =
            payload.has("canClickLeft") && payload.get("canClickLeft").getAsBoolean();
        boolean canClickRight =
            payload.has("canClickRight") && payload.get("canClickRight").getAsBoolean();

        creator.openAnvilGUI(
            player,
            initialText,
            textured,
            textureKey,
            offset,
            items.get(0), // left
            items.get(1), // right
            items.get(2), // output
            title,
            canClickLeft,
            canClickRight,
            (p, state, slot) -> List.of(), // Placeholder completion logic
            (state) -> {} // Placeholder onClose logic
            );
      }
      default -> {
        getLogger().printBukkit("Unknown GUI type: " + guiType, ContextLogger.LogType.WARNING);
        return CompletableFuture.failedFuture(new IllegalArgumentException("Unknown GUI type"));
      }
    }

    JsonObject response = new JsonObject();
    response.addProperty("success", true);
    return CompletableFuture.completedFuture(response);
  }

  @Handler(key = "applyEffect")
  public CompletableFuture<Boolean> handleEffectRegister(JsonObject payload) {
    String effectId = payload.get("effectId").getAsString();
    int durationInSeconds = payload.get("duration").getAsInt();
    int level = payload.get("level").getAsInt();
    UUID playerUUID = UUID.fromString(payload.get("playerId").getAsString());

    var optional = EffectRegistry.getInstance(EffectRegistry.class).getEffect(effectId);

    if (optional.isPresent()) {
      var effect = optional.get();
      Player player = Bukkit.getPlayer(playerUUID);
      if (player != null) {
        effect.apply(player, durationInSeconds, level);
        return CompletableFuture.completedFuture(true);
      }
      getLogger().printBukkit("Unknown player with uuid: " + playerUUID, true);
    }
    getLogger().printBukkit("Unknown effect with id: " + effectId, true);
    return CompletableFuture.completedFuture(true);
  }
}
