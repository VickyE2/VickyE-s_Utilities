/* Licensed under Apache-2.0 2024. */
package org.vicky.ecosystem.plugin;

import com.google.gson.JsonObject;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.vicky.ecosystem.exceptions.HandlerNotFoundException;

public class PluginCommunicator {
  private final String name;
  private final ClassLoader loader;
  private final Communicateable impl;
  private final Map<String, CommunicatorHandler> handlers = new ConcurrentHashMap<>();

  public PluginCommunicator(String name, ClassLoader loader, Communicateable impl) {
    this.name = name;
    this.loader = loader;
    this.impl = impl;
  }

  public void registerHandler(String key, CommunicatorHandler handler) {
    handlers.put(key, handler);
  }

  public CompletableFuture<Object> receiveAsync(String key, JsonObject payload) {
    CommunicatorHandler handler = handlers.get(key);
    if (handler == null)
      return CompletableFuture.failedFuture(
          new HandlerNotFoundException("Handler " + key + " not found."));
    return handler.handle(payload);
  }

  public Communicateable getImpl() {
    return impl;
  }

  public ClassLoader getLoader() {
    return loader;
  }

  public Map<String, CommunicatorHandler> getHandlers() {
    return handlers;
  }

  public String getName() {
    return name;
  }
}
