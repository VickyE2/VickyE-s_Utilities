/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.vicky.expansions.maths.model.MimFunction;
import org.vicky.utilities.PermittedObject;

public class ScriptEvaluator {
  private final Map<String, Object> variables = new HashMap<>();
  private final Map<String, FunctionDefinition> functions = new HashMap<>();
  private final Map<String, ScriptEvaluator> aliasedImports = new HashMap<>();

  private final String scriptName;

  public ScriptEvaluator(String scriptName) {
    this.scriptName = scriptName;
  }

  public static ScriptEvaluator.FunctionDefinition fromMimFunction(
      MimFunction fn, VariableManager variableManager, String scriptName) {
    return (context, args) -> {
      try {
        if (fn.getArguments() != null && fn.getBody() != null) {
          // Standard expression mode (using exp4j)
          List<Double> doubleArgs =
              args.stream()
                  .map(
                      o -> {
                        if (o instanceof Number) return ((Number) o).doubleValue();
                        try {
                          return Double.parseDouble(o.toString());
                        } catch (Exception e) {
                          return Double.NaN;
                        }
                      })
                  .toList();

          return Double.parseDouble(fn.evaluate(doubleArgs));

        } else if (fn.getArguments() == null
            && fn.getBody() == null
            && fn.evaluate(List.of(1.0)) != null) {
          // Lambda-style
          List<Double> doubleArgs =
              args.stream()
                  .map(
                      o -> {
                        if (o instanceof Number) return ((Number) o).doubleValue();
                        try {
                          return Double.parseDouble(o.toString());
                        } catch (Exception e) {
                          return Double.NaN;
                        }
                      })
                  .toList();

          return Double.parseDouble(fn.evaluate(doubleArgs));

        } else {
          // Variable-based
          List<String> stringArgs = args.stream().map(Object::toString).toList();

          String result = fn.evaluateRawArgs(stringArgs, variableManager, scriptName);
          return Double.parseDouble(result);
        }

      } catch (Exception e) {
        e.printStackTrace();
        return Double.NaN;
      }
    };
  }

  public void importScript(String alias, ScriptEvaluator otherScript) {
    aliasedImports.put(alias, otherScript);
  }

  public void defineFunction(String name, FunctionDefinition fn) {
    functions.put(name, fn);
  }

  public Object evaluate(String expr) {
    return evaluateExpression(expr.trim());
  }

  private Object evaluateExpression(String expr) {
    expr = expr.trim();

    // Handle function calls like: name(arg1, arg2)
    if (expr.matches("[a-zA-Z_][a-zA-Z0-9_]*\\(.*\\)")) {
      String name = expr.substring(0, expr.indexOf("("));
      String argsRaw = expr.substring(expr.indexOf("(") + 1, expr.lastIndexOf(")"));
      List<String> args = splitArgs(argsRaw);

      List<Object> evaluatedArgs =
          args.stream().map(this::evaluateExpression).collect(Collectors.toList());

      FunctionDefinition fn = resolveFunction(name);
      if (fn != null) {
        return fn.call(this, evaluatedArgs); // self-passing
      } else {
        throw new RuntimeException("Function not found: " + name);
      }
    }

    // Handle variables and numeric literals
    if (variables.containsKey(expr)) return variables.get(expr);
    if (expr.matches("-?\\d+(\\.\\d+)?")) return Double.parseDouble(expr); // simple math

    throw new RuntimeException("Invalid expression: " + expr);
  }

  public Object evaluateWithContext(
      String expr, Player player, Map<String, PermittedObject> scope) {
    // Add runtime player data
    if (player != null) {
      setVariable("player_name", player.getName());
      setVariable("player_health", player.getHealth());
    }

    // Add scope entries
    for (Map.Entry<String, PermittedObject> entry : scope.entrySet()) {
      setVariable(entry.getKey(), entry.getValue());
    }

    return evaluate(expr);
  }

  private FunctionDefinition resolveFunction(String name) {
    // Local
    if (functions.containsKey(name)) return functions.get(name);

    // Aliased import e.g. U.func
    if (name.contains(".")) {
      String[] split = name.split("\\.");
      if (split.length == 2 && aliasedImports.containsKey(split[0])) {
        return aliasedImports.get(split[0]).functions.get(split[1]);
      }
    }

    // Search all imports if not qualified
    for (ScriptEvaluator imported : aliasedImports.values()) {
      if (imported.functions.containsKey(name)) {
        return imported.functions.get(name);
      }
    }

    return null;
  }

  private List<String> splitArgs(String raw) {
    List<String> parts = new ArrayList<>();
    int depth = 0;
    StringBuilder sb = new StringBuilder();
    for (char c : raw.toCharArray()) {
      if (c == ',' && depth == 0) {
        parts.add(sb.toString().trim());
        sb = new StringBuilder();
      } else {
        if (c == '(') depth++;
        else if (c == ')') depth--;
        sb.append(c);
      }
    }
    if (sb.length() > 0) parts.add(sb.toString().trim());
    return parts;
  }

  public void setVariable(String name, Object value) {
    variables.put(name, value);
  }

  public String getScriptName() {
    return scriptName;
  }

  // Example of FunctionDefinition:
  public interface FunctionDefinition {
    Object call(ScriptEvaluator context, List<Object> args);
  }
}
