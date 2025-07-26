/* Licensed under Apache-2.0 2024. */
package org.vicky.ecosystem.server;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import org.vicky.ecosystem.plugin.Communicateable;
import org.vicky.ecosystem.plugin.PluginCommunicator;
import org.vicky.ecosystem.plugin.guide.HandlerRegistry;

public class AutoHandler {
  public static void registerAll(
      Communicateable target, PluginCommunicator communicator, HandlerRegistry registry) {
    registry.registerHandlers(target.getClass());
    for (Method method : target.getClass().getDeclaredMethods()) {
      Handler annotation = method.getAnnotation(Handler.class);
      if (annotation != null) {
        if (method.getReturnType().equals(Void.TYPE)) {
          throw new IllegalStateException(
              "Method " + method.getName() + " is annotated with return a CompletableFuture void.");
        }
        if (method.getParameterCount() != 1) {
          throw new IllegalArgumentException(
              "Handler method must have exactly one parameter: " + method.getName());
        }
        String key = annotation.key();
        method.setAccessible(true);
        communicator.registerHandler(
            key,
            (payload) -> {
              try {
                Object result = method.invoke(target, payload);
                if (result == null) {
                  return CompletableFuture.completedFuture(null);
                }
                if (result instanceof CompletableFuture) {
                  return (CompletableFuture<Object>) result;
                } else {
                  return CompletableFuture.completedFuture(result);
                }
              } catch (Exception e) {
                return CompletableFuture.failedFuture(e);
              }
            });
      }
    }
  }
}
