/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.global.Global;
import org.vicky.guiparent.GuiCreator;

public class Config {
  public static final Map<String, Object> configs = new HashMap<>();

  static {
    configs.put("Debug", false);
  }

  private final JavaPlugin plugin;

  public Config(JavaPlugin plugin) {
    this.plugin = plugin;
  }

  /**
   * {@link org.vicky.guiparent.GuiCreator.AllowedItemStack} isn't allowed...
   */
  public <T> void addConfig(String key, GuiCreator.ItemConfig.AllowedNBTType<T> value) {
    configs.put(key, value.getValue());
  }

  public void registerConfigs() {
    for (String key : configs.keySet()) {
      if (!Global.globalConfigManager.doesPathExist(key)) {
        Global.globalConfigManager.setBracedConfigValue(key, configs.getOrDefault(key, ""), "");
      }
      Global.globalConfigManager.saveConfig();
    }
  }
}
