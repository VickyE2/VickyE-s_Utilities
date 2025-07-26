/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.engine;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.vicky.utilities.PermittedObject;
import org.vicky.utilities.PermittedObjects.AllowedDouble;

public class VariableManager {
  private final Map<String, PermittedObject> globals = new HashMap<>();
  private final Map<String, Map<String, PermittedObject>> locals = new HashMap<>();
  private final Map<String, Map<String, PermittedObject>> stateStore = new HashMap<>();

  public void registerDefaultVariables() {
    globals.put("pi", new AllowedDouble(3.141592653589793));
    globals.put("π", new AllowedDouble(Math.PI));
    globals.put("φ", new AllowedDouble(1.61803398874));
    globals.put("e", new AllowedDouble(Math.E));
  }

  public <T> void set(@NotNull String key, PermittedObject<T> value) {
    globals.put(key, value);
  }

  public <T> void set(
      @NotNull String key, PermittedObject<T> value, @NotNull String scope, boolean isGlobal) {
    if (isGlobal) {
      set(key, value);
    } else {
      locals.computeIfAbsent(scope, s -> new HashMap<>()).put(key, value);
    }
  }

  public PermittedObject getStateValue(String script, String key) {
    return stateStore.getOrDefault(script, Map.of()).get(key);
  }

  public void setStateValue(String script, String key, PermittedObject value) {
    stateStore.computeIfAbsent(script, k -> new HashMap<>()).put(key, value);
  }

  public PermittedObject getValue(String key) {
    return globals.getOrDefault(key, new AllowedDouble(0.0D));
  }

  public PermittedObject getValue(String key, String scope) {
    return locals.getOrDefault(scope, Map.of()).getOrDefault(key, new AllowedDouble(0.0D));
  }

  public String get(String key) {
    return String.valueOf(getValue(key));
  }

  public String get(String key, String scope) {
    return String.valueOf(getValue(key, scope));
  }

  public void remove(String key) {
    globals.remove(key);
  }

  public void remove(String key, String scope) {
    Map<String, PermittedObject> scoped = locals.get(scope);
    if (scoped != null) scoped.remove(key);
  }

  public String replaceVariables(String expression, String scope, Player player) {
    TokenParser parser = new TokenParser();
    return parser.parseAndEvaluate(this, expression, scope, player);
  }

  public Map<String, PermittedObject> getAllVariables() {
    return globals;
  }

  public Map<String, PermittedObject> getMapOf(String localScope) {
    return locals.get(localScope) != null
        ? locals.get(localScope)
        : locals.put(localScope, new HashMap<>());
  }
}
