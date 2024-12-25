/* Licensed under Apache-2.0 2024. */
package org.vicky.guiparent;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.listeners.BaseGuiListener;

public abstract class BaseGui {
  protected final JavaPlugin plugin;
  protected final GuiCreator guiManager;
  protected BaseGuiListener listener; // Make the listener flexible

  // Default constructor for any GUI class, allowing a custom listener to be passed
  public BaseGui(JavaPlugin plugin, BaseGuiListener listener) {
    this.plugin = plugin;
    this.listener =
        listener != null
            ? listener
            : new DefaultGuiListener(); // Use provided listener or default one
    this.guiManager = new GuiCreator(plugin, listener);

    // Register the listener for GUI actions
    plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);
  }

  // Abstract method that each child GUI class must implement to display the GUI
  public abstract void showGui(Player player);

  // Method to change the listener dynamically
  public void setListener(BaseGuiListener listener) {
    // Unregister the current listener if needed (depends on implementation)
    // plugin.getServer().getPluginManager().unregisterListener(this.listener);

    this.listener = listener;
    // Re-register the new listener
    plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);
  }
}
