package org.vicky.utilities.ContextLogger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.utilities.ANSIColor;

public class ContextLogger {
    private final ContextType context;
    private final String contextName;
    private final JavaPlugin plugin;

    public ContextLogger(ContextType context, String contextName) {
        this.context = context;
        this.plugin = null;
        this.contextName = contextName.toUpperCase();
    }

    public ContextLogger(ContextType context, String contextName, JavaPlugin plugin) {
        this.context = context;
        this.plugin = plugin;
        this.contextName = contextName.toUpperCase();
    }

    public void print(String message, boolean isError) {
        String contextTag = "[" + ANSIColor.colorize((isError ? "red" : "cyan") + "[" + context + "-" + contextName + "]") + "] ";
        String finalContext = contextTag + (isError ? ANSIColor.colorize(message, ANSIColor.RED) : message);
        plugin.getLogger().info(finalContext);
    }

    public void print(String message) {
        String contextTag = "[" + ANSIColor.colorize("cyan[" + context + "-" + contextName + "]") + "] ";
        String finalContext = contextTag + message;
        plugin.getLogger().info(finalContext);
    }

    public void printBukkit(String message, boolean isError) {
        String contextTag = "[" + ANSIColor.colorize((isError ? "red" : "cyan") + "[" + context + "-" + contextName + "]") + "] ";
        String finalContext = contextTag + (isError ? ANSIColor.colorize(message, ANSIColor.RED) : message);
        Bukkit.getLogger().info(finalContext);
    }

    public void printBukkit(String message) {
        String contextTag = "[" + ANSIColor.colorize("cyan[" + context + "-" + contextName + "]") + "] ";
        String finalContext = contextTag + message;
        Bukkit.getLogger().info(finalContext);
    }

    public enum ContextType {
        SYSTEM,
        FEATURE,
        HIBERNATE
    }
}
