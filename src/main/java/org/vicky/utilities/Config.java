package org.vicky.utilities;

import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.global.Global;

import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final Map<String, Object> configs = new HashMap<>();

    static {
        configs.put("Debug", false);
    }

    private final JavaPlugin plugin;

    public Config(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerConfigs() {
        for (String key : configs.keySet()) {
            if (!Global.configManager.doesPathExist(key)) {
                Global.configManager.setBracedConfigValue(key, configs.getOrDefault(key, ""), "");
            }

            Global.configManager.saveConfig();
        }
    }
}
