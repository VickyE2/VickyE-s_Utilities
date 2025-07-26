/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.engine;

import java.util.HashMap;
import java.util.Map;
import org.vicky.expansions.maths.NativeModules.BukkitNativeModule;
import org.vicky.expansions.maths.NativeModules.MathsNativeModule;
import org.vicky.expansions.maths.NativeModules.NativeModule;
import org.vicky.expansions.maths.model.MimScript;

public class LibraryManager {
  private final Map<String, MimScript> scripts = new HashMap<>();
  private final Map<String, NativeModule> nativeModules =
      Map.of(
          "bukkit", new BukkitNativeModule(),
          "math", new MathsNativeModule());

  public void registerScript(String name, MimScript script) {
    scripts.put(name, script);
  }

  public MimScript getScript(String name) {
    return scripts.get(name);
  }

  public boolean hasScript(String name) {
    return scripts.containsKey(name);
  }

  public Map<String, NativeModule> getNativeModules() {
    return nativeModules;
  }
}
