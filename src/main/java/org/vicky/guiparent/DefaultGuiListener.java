/* Licensed under Apache-2.0 2024. */
package org.vicky.guiparent;

import org.bukkit.plugin.java.JavaPlugin;
import org.vicky.listeners.BaseGuiListener;

/**
 * This is a dummy gui listener. Basically does nothing. No event canceling or listening.
 */
public class DefaultGuiListener extends BaseGuiListener {
  public DefaultGuiListener(JavaPlugin plugin) {
    super(plugin);
  }
}
