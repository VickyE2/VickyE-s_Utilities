/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeathMessages {
  private static final Map<String, Map<String, Boolean>> deathMessages = new HashMap<>();

  // Method to add messages for a specific cause
  public static void add(String customCause, String message, boolean requiresKiller) {
    deathMessages.computeIfAbsent(customCause, k -> new HashMap<>()).put(message, requiresKiller);
  }

  // Method to get messages for a specific cause
  public static Map<String, Boolean> getMessages(String customCause) {
    return deathMessages.getOrDefault(customCause, new HashMap<>());
  }
}
