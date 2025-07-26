/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.vicky.global.Global;
import org.vicky.utilities.PermittedObjects.AllowedItemStack;

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
   * {@link AllowedItemStack} isn't allowed...
   */
  public <T> void addConfig(@NotNull String key, PermittedObject<T> value) {
    configs.put(key, value.getValue());
  }

  public void registerConfigs() {
    for (String key : configs.keySet()) {
      if (!Global.globalConfigManager.doesPathExist(key)) {
        String[] split = key.split("\\.");
        String contextKey = split[0];
        String contextChild = String.join(".", Arrays.copyOfRange(split, 1, split.length));
        Global.globalConfigManager.setConfigValue(
            contextKey, contextChild, configs.getOrDefault(key, ""), null);
      }
      Global.globalConfigManager.saveConfig();
    }
  }
}
