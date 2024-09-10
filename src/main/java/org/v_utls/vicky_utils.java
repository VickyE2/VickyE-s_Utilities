package org.v_utls;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.v_utls.effects.Bleeding;
import org.v_utls.expansions.PlaceholderExpansions;
import org.v_utls.handlers.CustomDamageHandler;
import org.v_utls.listeners.DeathListener;
import org.v_utls.listeners.DeathMessageListener;
import org.v_utls.listeners.SpawnListener;
import org.v_utls.mythic.MechanicRegistrar;

public final class vicky_utils extends JavaPlugin {

    public static vicky_utils plugin;
    private CustomDamageHandler customDamageHandler;

    public static vicky_utils getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {

        // Plugin startup logic
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().getPlugin("MythicMobs") != null) {

            customDamageHandler = new CustomDamageHandler(this);
            Bleeding bleeding = new Bleeding(this);

            getLogger().info("Rendering Vicky's Utilities Accessible");
            new PlaceholderExpansions(this).register();

            MechanicRegistrar registrations = new MechanicRegistrar(this);
            registrations.registerAll();

            getServer().getPluginManager().registerEvents(new DeathListener(bleeding), this);
            getServer().getPluginManager().registerEvents(new DeathMessageListener(this), this);
            getServer().getPluginManager().registerEvents(new SpawnListener(customDamageHandler), this);
            getServer().getPluginManager().registerEvents(new DeathMessageListener(this), this);


        } else if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null){
            getLogger().severe("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }else{
            getLogger().severe("Could not find MythicAPI! Ensure MythicMobs is Present.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Rendering Utilities Inaccessible");
    }

    public CustomDamageHandler getCustomDamageHandler() {
        return customDamageHandler;
    }
}
