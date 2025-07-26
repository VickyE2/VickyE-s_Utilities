/* Licensed under Apache-2.0 2024. */
package org.vicky.ecosystem.plugin.guide;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.vicky.ecosystem.server.Handler;

public class HandlerRegistry {
  private final Map<String, HandlerMeta> handlers;

  public HandlerRegistry() {
    this.handlers = new HashMap<>();
  }

  public void registerHandlers(Class<?> clazz) {
    for (Method method : clazz.getDeclaredMethods()) {
      if (method.isAnnotationPresent(Handler.class)) {
        Handler annotation = method.getAnnotation(Handler.class);
        handlers.put(annotation.key(), new HandlerMeta(method, annotation));
      }
    }
  }

  public HandlerMeta get(String key) {
    return handlers.get(key);
  }

  public Collection<HandlerMeta> all() {
    return handlers.values();
  }

  public JsonObject getHelpJson(String key) {
    HandlerMeta meta = get(key);
    if (meta == null) return null;

    JsonObject obj = new JsonObject();
    obj.addProperty("key", meta.annotation().key());
    obj.addProperty("description", meta.annotation().description());
    obj.addProperty("usage", meta.annotation().usage());

    JsonArray examples = new JsonArray();
    for (String ex : meta.annotation().examples()) examples.add(ex);
    obj.add("examples", examples);

    JsonArray params = new JsonArray();
    for (Parameter p : meta.method().getParameters()) {
      JsonObject param = new JsonObject();
      param.addProperty("name", p.getName());
      param.addProperty("type", p.getType().getSimpleName());
      params.add(param);
    }

    obj.add("parameters", params);
    return obj;
  }
}
