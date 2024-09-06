package org.v_utls.mythic;

import io.lumine.mythic.bukkit.events.MythicConditionLoadEvent;
import io.lumine.mythic.bukkit.events.MythicDropLoadEvent;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.Plugin;

public class MechanicRegistrar {

    private final Plugin plugin;

    public MechanicRegistrar(Plugin plugin) {
        this.plugin = plugin;
    }

    public void registerAll() {
        // Register custom mechanics
        plugin.getServer().getPluginManager().registerEvents(new MechanicsListener(), plugin);
    }

    private class MechanicsListener implements org.bukkit.event.Listener {

        @EventHandler
        public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
            switch (event.getMechanicName().toUpperCase()) {
                case "BLEEDING":
                    event.register(new BleedingMechanic(event.getConfig(), plugin));
                    plugin.getLogger().info("-- Registered Bleeding mechanic!");
                    break;
                // Add cases for other mechanics
                default:
                    plugin.getLogger().warning("-- Unrecognized mechanic: " + event.getMechanicName());
                    break;
            }
        }

    }
}
