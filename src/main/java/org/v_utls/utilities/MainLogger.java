package org.v_utls.utilities;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import static org.v_utls.global.Global.placeholderStorer;
import static org.v_utls.global.Global.stringStorer;

public class MainLogger {

    private final JavaPlugin plugin;

    public MainLogger(JavaPlugin plugin) {
        this.plugin = plugin;
    }
    public void getHooks() {
        if (Bukkit.getPluginManager().getPlugin("VickySBA") != null) {
            plugin.getLogger().info("Plugin 'Vicky's SBA' is present. Adding additional Functionality");
        }else{
            plugin.getLogger().warning("Plugin 'Vicky's SBA' is missing... You probably don't need bedwars functionality. Loaded either ways");
        }
    }

    public void getPlaceholders() {
        placeholderStorer.listPlaceholders("all", plugin);
    }

    public void getMechanics() {
        List<String> storedMechanics = stringStorer.returnStoredStrings(plugin.getName(), "mm_mechanic");
        if (storedMechanics != null) {
            for (String str : storedMechanics) {
                plugin.getLogger().info(str);
            }
        } else {
            plugin.getLogger().severe("No registered Mechanics found");
        }
    }

    public void logAll(){
        plugin.getLogger().info("MainLogger Will Proceed to log all...");
        getHooks();
        plugin.getLogger().info("Registered Stored PlaceHolders: ");
        getPlaceholders();
        plugin.getLogger().info("Registered MythicMobs Mechanics: ");
        getMechanics();
    }
}
