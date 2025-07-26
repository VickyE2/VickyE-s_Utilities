/* Licensed under Apache-2.0 2024. */
package org.vicky.ecosystem.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

public class ItemSerializer {
  private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

  // Serialize ItemStack to JsonElement
  public static JsonElement serializeItem(ItemStack item) {
    if (item == null) return JsonNull.INSTANCE;

    Map<String, Object> map = item.serialize();
    return gson.toJsonTree(map);
  }

  // Deserialize JsonElement to ItemStack
  @SuppressWarnings("unchecked")
  public static ItemStack deserializeItem(JsonElement element) {
    if (element == null || element.isJsonNull()) return null;

    Map<String, Object> map = gson.fromJson(element, Map.class);
    // Note: Bukkit's deserialize expects raw Map<String, Object>, but Gson uses LinkedTreeMap
    // It still works for Bukkit's deserialize, but deep values might need extra handling.
    return ItemStack.deserialize(map);
  }
}
