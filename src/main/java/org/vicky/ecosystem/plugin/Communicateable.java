/* Licensed under Apache-2.0 2024. */
package org.vicky.ecosystem.plugin;

import org.vicky.ecosystem.plugin.guide.HandlerRegistry;
import org.vicky.ecosystem.server.AutoHandler;
import org.vicky.utilities.ContextLogger.ContextLogger;

public abstract class Communicateable {
  private final ContextLogger logger =
      new ContextLogger(ContextLogger.ContextType.COMMUNICATION, getClass().getSimpleName());
  protected PluginCommunicator communicator;
  protected HandlerRegistry registry;

  /**
   * Registers this plugin into the Communicator system.
   */
  public void register(PluginCommunicator communicator) {
    registry = new HandlerRegistry();
    AutoHandler.registerAll(this, communicator, registry);
    onRegister();
  }

  public HandlerRegistry getRegistry() {
    return registry;
  }

  /**
   * Called once the plugin is successfully registered and handlers are attached.
   */
  protected abstract void onRegister();

  /**
   * Called when the plugin is being unregistered or the server is shutting down.
   * Override this to cancel tasks, close resources, or save state.
   */
  public void onShutdown() {
    // Optional override: plugin-specific cleanup logic
  }

  public ContextLogger getLogger() {
    return logger;
  }

  /**
   * Convenience access to this plugin's communicator.
   */
  public PluginCommunicator getCommunicator() {
    return communicator;
  }
}
