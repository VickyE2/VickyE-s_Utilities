package org.v_utls.utilities;

import org.bukkit.Bukkit;
import org.v_utls.vicky_utils;

import java.util.List;

public class MainLogger {
    public void getHooks() {
        if (Bukkit.getPluginManager().getPlugin("VickySBA") != null) {
            Bukkit.getLogger().info("Plugin 'Vicky's SBA' is present. Adding additional Functionality");
        }else{
            Bukkit.getLogger().info("Plugin 'Vicky's SBA' is missing... You probably don't need bedwars functionality. Loaded either ways");
        }
    }

    public void getPlaceholders() {
        PlaceholderStorer store = new PlaceholderStorer();
        store.listPlaceholders("all", vicky_utils.getPlugin());
    }

    public void getMechanics() {
        StringStore store = new StringStore();
        List<String> storedMechanics = store.returnStoredStrings(vicky_utils.getPlugin().getName(), "mm_mechanic");
        for (String str : storedMechanics) {
            Bukkit.getLogger().info(str);
        }
    }

    public void logAll(){
        Bukkit.getLogger().info("MainLogger Will Proceed to log all...");
        getHooks();
        Bukkit.getLogger().info("Registered Stored PlaceHolders: ");
        getPlaceholders();
        Bukkit.getLogger().info("Registered MythicMobs Mechanics: ");
        getMechanics();
    }
}
