/* Licensed under Apache-2.0 2024. */
package org.vicky.items_adder;

import dev.lone.itemsadder.api.FontImages.FontImageWrapper;
import org.bukkit.Bukkit;

public class FontImageSender {
  public static String getImage(String fontImageName) {
    if (Bukkit.getServer().getPluginManager().getPlugin("ItemsAdder") != null) {
      FontImageWrapper fontImage = new FontImageWrapper(fontImageName);
      if (fontImage == null) {
        return "\uDBC0\uDC01";
      }
      return fontImage.getString();
    }
    return "\uDBC0\uDC01";
  }
}
