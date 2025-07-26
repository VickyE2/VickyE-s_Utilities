/* Licensed under Apache-2.0 2024. */
package org.vicky.ecosystem.plugin;

import com.google.gson.JsonObject;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface CommunicatorHandler {
  CompletableFuture<Object> handle(JsonObject message);
}
