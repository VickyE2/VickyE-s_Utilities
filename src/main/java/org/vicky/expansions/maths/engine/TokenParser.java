/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.engine;

import org.bukkit.entity.Player;
import org.vicky.utilities.PermittedObject;
import org.vicky.utilities.PermittedObjects.AllowedDouble;

public class TokenParser {
  public String parseAndEvaluate(
      VariableManager manager, String expression, String scope, Player player) {
    StringBuilder result = new StringBuilder();
    int length = expression.length();
    int i = 0;

    while (i < length) {
      char ch = expression.charAt(i);

      // Handle ${varName}
      if (ch == '$' && i + 1 < length && expression.charAt(i + 1) == '{') {
        int start = i + 2;
        int end = expression.indexOf('}', start);
        if (end != -1) {
          String varName = expression.substring(start, end);
          String replacement = getVariableValueOrFallback(manager, varName, scope);
          result.append(replacement);
          i = end + 1; // Skip past closing brace
          continue;
        } else {
          // malformed, just append literal
          result.append(ch);
          i++;
          continue;
        }
      }

      // Handle papi:variableName (letters, digits, underscore after papi:)
      if (ch == 'p' && i + 4 < length && expression.startsWith("papi:", i)) {
        int varEnd = i + 5;
        // Variable names are usually letters, digits, underscore, colon maybe
        while (varEnd < length) {
          char c = expression.charAt(varEnd);
          if (!Character.isLetterOrDigit(c) && c != '_' && c != ':' && c != '.') {
            break;
          }
          varEnd++;
        }

        String varName = expression.substring(i, varEnd);
        // Strip "papi:" prefix for lookup if your manager expects that
        String key = varName.substring(5); // remove "papi:"
        String placeholder = "%" + key + "%";
        String value = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, placeholder);
        result.append(value);
        i = varEnd;
        continue;
      }

      // Default: append char and move on
      result.append(ch);
      i++;
    }

    return result.toString();
  }

  private String getVariableValueOrFallback(VariableManager manager, String name, String scope) {
    PermittedObject obj = (scope == null) ? manager.getValue(name) : manager.getValue(name, scope);
    if (obj instanceof AllowedDouble d) {
      return String.valueOf(d.value());
    } else {
      return obj.getValue().toString();
    }
  }
}
