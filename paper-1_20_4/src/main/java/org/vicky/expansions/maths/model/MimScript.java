/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.model;

import static org.vicky.expansions.maths.engine.ScriptEvaluator.fromMimFunction;

import java.util.*;
import org.bukkit.entity.Player;
import org.vicky.expansions.maths.NativeModules.NativeModule;
import org.vicky.expansions.maths.engine.ScriptEngine;
import org.vicky.expansions.maths.engine.ScriptEvaluator;
import org.vicky.utilities.PermittedObject;
import org.vicky.utilities.PermittedObjects.AllowedDouble;
import org.vicky.utilities.PermittedObjects.AllowedStringObj;

/**
 * Represents a single Mim script, containing named functions and
 * an optional persistent state store for script-level variables.
 * <p>
 * Scripts are defined using a simple DSL with lines such as:
 * <pre>
 * func:add(a, b) = a + b
 * import:utils
 * </pre>
 * The parser registers function definitions and handles imports.
 *
 * @author VickyE
 */
public class MimScript {
  private final String name;
  private final String rawSource;
  private final Map<String, MimFunction> functions = new HashMap<>();
  private final List<String> imports = new ArrayList<>();
  private final Map<String, PermittedObject> mappedScope;
  private final ScriptEvaluator scriptEvaluator;
  private final ScriptEngine engine;
  private List<String> lines;

  /**
   * Constructs a MimScript by parsing the provided source.
   *
   * @param name   Unique name of the script, used for imports and state.
   * @param source Raw DSL source text containing function definitions and imports.
   */
  public MimScript(String name, String source, ScriptEngine engine) {
    this.name = name;
    this.rawSource = source;
    this.engine = engine;
    this.mappedScope = engine.getVariableManager().getMapOf(this.name);
    this.scriptEvaluator = new ScriptEvaluator(this.name); // Pass script and engine
    parseSource(source);
  }

  /**
   * Parses raw DSL source, extracting imports and function definitions.
   * <p>
   * Supported directives:
   * <ul>
   *     <li>{@code import:<scriptName>} to import external scripts</li>
   *     <li>{@code func:<name>(arg1,arg2)=<expression>} to define a function</li>
   *     <li>Lines starting with {@code #} or blank lines are ignored (comments)</li>
   * </ul>
   *
   * @param source Raw DSL source to parse.
   */
  private void parseSource(String source) {
    String[] lines = source.split("\\r?\\n");
    this.lines = List.of(lines);
    for (String rawLine : lines) {
      String line = rawLine.trim();
      if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
        // Skip comments and blank lines
        continue;
      }
      if (line.startsWith("import:")) {
        String allImports = line.substring("import:".length()).trim();
        String[] imports = allImports.split(",");

        for (String imp : imports) {
          imp = imp.trim();

          // Handle function aliasing like: bukkit.print@ as log
          if (imp.contains("@ as ")) {
            String[] parts = imp.split("@ as");
            String funcPath = parts[0].trim(); // e.g., bukkit.print
            String alias = parts[1].trim(); // e.g., log

            if (funcPath.contains(".")) {
              String[] pathParts = funcPath.split("\\.");
              String moduleName = pathParts[0];
              String funcName = pathParts[1];

              NativeModule module = engine.getScriptManager().getNativeModules().get(moduleName);
              if (module != null) {
                module.importFunction(scriptEvaluator, funcName, alias);
              }
            }
            continue;
          }

          // Handle regular function import like: bukkit.print@
          if (imp.endsWith("@")) {
            String funcPath = imp.substring(0, imp.length() - 1).trim(); // remove '@'
            if (funcPath.contains(".")) {
              String[] pathParts = funcPath.split("\\.");
              String moduleName = pathParts[0];
              String funcName = pathParts[1];

              NativeModule module = engine.getScriptManager().getNativeModules().get(moduleName);
              if (module != null) {
                module.importFunction(scriptEvaluator, funcName, funcName); // no alias
              }
            }
            continue;
          }

          // Handle module alias like: myscript as something
          if (imp.contains(" as ")) {
            String[] parts = imp.split(" as ");
            String target = parts[0].trim();
            String alias = parts[1].trim();

            MimScript script = engine.getScriptManager().getScript(target);
            if (script != null) {
              scriptEvaluator.importScript(alias, script.getScriptEvaluator());
            }
            continue;
          }

          // Handle regular native module or script import
          if (engine.getScriptManager().getNativeModules().containsKey(imp)) {
            NativeModule nativeModule = engine.getScriptManager().getNativeModules().get(imp);
            nativeModule.importInto(scriptEvaluator);
          } else {
            MimScript importedScript = engine.getScriptManager().getScript(imp);
            if (importedScript != null) {
              scriptEvaluator.importScript(imp, importedScript.getScriptEvaluator());
            }
          }
        }
        continue;
      }
      if (line.startsWith("func:")) {
        // Handle function definition
        String def = line.substring("func:".length()).trim();
        String[] parts = def.split("=", 2);
        if (parts.length != 2) {
          throw new IllegalArgumentException("Invalid function definition: " + line);
        }
        // Parse signature: name(arg1,arg2,...)
        String signature = parts[0].trim();
        String body = parts[1].trim();
        int parenOpen = signature.indexOf('(');
        int parenClose = signature.indexOf(')', parenOpen);
        if (parenOpen < 0 || parenClose < 0) {
          throw new IllegalArgumentException(
              "Malformed function signature in script '" + name + "' at line: " + signature);
        }
        String funcName = signature.substring(0, parenOpen).trim();
        String argsContent = signature.substring(parenOpen + 1, parenClose).trim();
        List<String> argList = new ArrayList<>();
        if (!argsContent.isEmpty()) {
          for (String arg : argsContent.split(",")) {
            argList.add(arg.trim());
          }
        }
        // Create and register the function
        MimFunction function = new MimFunction(argList, body);
        functions.put(funcName, function);
      }
    }
    for (Map.Entry<String, MimFunction> entry : functions.entrySet()) {
      String name = entry.getKey();
      MimFunction fn = entry.getValue();

      scriptEvaluator.defineFunction(
          name, fromMimFunction(fn, engine.getVariableManager(), this.name));
    }
  }

  /**
   * Returns the set of imported script names.
   * These can be used to load additional functions from other MimScript instances.
   *
   * @return List of script names imported by this script, in declaration order.
   */
  public List<String> getImports() {
    return Collections.unmodifiableList(imports);
  }

  /**
   * Retrieves a defined function by name.
   *
   * @param functionName Name of the function to fetch.
   * @return The corresponding MimFunction, or null if not found.
   */
  public MimFunction getFunction(String functionName) {
    return functions.get(functionName);
  }

  /**
   * Gets all functions defined in this script.
   *
   * @return Unmodifiable map of function names to MimFunction objects.
   */
  public Map<String, MimFunction> getFunctions() {
    return Collections.unmodifiableMap(functions);
  }

  /**
   * Returns the raw DSL source used to create this script.
   *
   * @return Original DSL source text.
   */
  public String getRawSource() {
    return rawSource;
  }

  /**
   * Returns the unique script name.
   *
   * @return Script name.
   */
  public String getName() {
    return name;
  }

  /**
   * Evaluate a multi-line script with optional local scope and conditionals.
   */
  public Object evaluate(Player player) {
    boolean lastConditionPassed = false;
    boolean insideConditionalBlock = false;

    for (String line : lines) {
      engine.getLogger().log(player, "Evaluating line: " + line);
      line = line.trim();
      if (line.isEmpty() || line.startsWith("// ")) continue;

      if (line.contains("->")) {
        String[] parts = line.split("->", 3);
        String keyword = parts[0].trim();

        if (keyword.equalsIgnoreCase("if") && parts.length == 3) {
          insideConditionalBlock = true;
          lastConditionPassed = parseCondition(parts[1].trim(), player, mappedScope);
          if (lastConditionPassed) return scriptEvaluator.evaluate(parts[2].trim());
          continue;
        }

        if (keyword.equalsIgnoreCase("elseif") && parts.length == 3) {
          if (!insideConditionalBlock) continue;
          if (lastConditionPassed) continue;
          boolean conditionPassed = parseCondition(parts[1].trim(), player, mappedScope);
          lastConditionPassed = conditionPassed;
          if (conditionPassed) return scriptEvaluator.evaluate(parts[2].trim());
          continue;
        }

        if (keyword.equalsIgnoreCase("else") && parts.length == 2) {
          if (!insideConditionalBlock || lastConditionPassed) continue;
          return scriptEvaluator.evaluate(parts[1].trim());
        }

        continue;
      }

      if (line.startsWith("return:")) {
        return scriptEvaluator.evaluate(line.substring("return:".length()).trim());
      }

      if (line.startsWith("func:")) {
        String def = line.substring("func:".length()).trim();
        String[] parts = def.split("=", 2);
        if (parts.length == 2) {
          String[] functionParts = parts[0].split("\\(", 2);
          if (functionParts.length != 2) continue;
          String funcName = functionParts[0].trim();
          String argsStr = functionParts[1].replace(")", "").trim();
          List<String> argList = new ArrayList<>();
          if (!argsStr.isEmpty()) {
            for (String arg : argsStr.split(",")) argList.add(arg.trim());
          }
          MimFunction func = new MimFunction(argList, parts[1].trim());
          functions.put(funcName, func);
        }
        continue;
      }

      if (line.startsWith("#")) {
        String[] parts = line.substring("#".length()).trim().split(":");
        switch (parts[0]) {
          case "debug" ->
              engine.getLogger().toggles.put(player.getUniqueId(), Boolean.parseBoolean(parts[1]));
        }
      }

      if (line.startsWith("let:")) {
        String let = line.substring("let:".length()).trim();
        String[] parts = let.split("=", 2);
        if (parts.length == 2) {
          String varName = parts[0].trim();
          String varExpr = parts[1].trim();
          Object value = scriptEvaluator.evaluate(varExpr);
          if (value instanceof Double) mappedScope.put(varName, new AllowedDouble(value));
          else if (value instanceof String) mappedScope.put(varName, new AllowedStringObj(value));
          scriptEvaluator.setVariable(varName, value);
        }
        continue;
      }

      if (line.startsWith("state:")) {
        String[] parts = line.substring(6).split("\\.", 2);
        if (parts.length == 2) {
          return engine
              .getVariableManager()
              .getStateValue(parts[0].equals("this") ? this.name : parts[0], parts[1])
              .getValue()
              .toString();
        }
      }

      // Evaluate any other expression line (side effects or variable usage)
      scriptEvaluator.evaluate(line);

      // Reset conditional states if non-conditional line
      insideConditionalBlock = false;
    }

    return "";
  }

  private boolean parseCondition(
      String expr, Player player, Map<String, PermittedObject> localScope) {
    String[] operators = {"===", "!==", ">=", "<=", "==", "!=", ">", "<"};

    for (String op : operators) {
      int idx = expr.indexOf(op);
      if (idx > -1) {
        String left = expr.substring(0, idx).trim();
        String right = expr.substring(idx + op.length()).trim();

        Object leftVal = evaluateValue(left, player, localScope);
        Object rightVal = evaluateValue(right, player, localScope);

        // Handle === and !== (strict equality)
        if (op.equals("==="))
          return leftVal.equals(rightVal) && leftVal.getClass() == rightVal.getClass();
        if (op.equals("!=="))
          return !leftVal.equals(rightVal) || leftVal.getClass() != rightVal.getClass();

        // Handle == and !=
        boolean same = leftVal.toString().equals(rightVal.toString());
        try {
          double l = Double.parseDouble(leftVal.toString());
          double r = Double.parseDouble(rightVal.toString());
          return switch (op) {
            case ">" -> l > r;
            case "<" -> l < r;
            case ">=" -> l >= r;
            case "<=" -> l <= r;
            case "==" -> l == r;
            case "!=" -> l != r;
            default -> false;
          };
        } catch (NumberFormatException e) {
          // Fallback to string compare
          if (op.equals("==")) return same;
          if (op.equals("!=")) return !same;
        }

        // Numeric comparisons
        if (leftVal instanceof Number && rightVal instanceof Number) {
          double l = ((Number) leftVal).doubleValue();
          double r = ((Number) rightVal).doubleValue();
          return switch (op) {
            case ">" -> l > r;
            case "<" -> l < r;
            case ">=" -> l >= r;
            case "<=" -> l <= r;
            default -> false;
          };
        }

        return false; // mismatched types for numeric comparison
      }
    }

    // No operator matched — fallback to boolean check
    Object result = evaluateValue(expr, player, localScope);
    if (result instanceof Boolean) return (Boolean) result;
    if (result instanceof Number) return ((Number) result).doubleValue() != 0;
    return Boolean.parseBoolean(result.toString());
  }

  private Object evaluateValue(
      String input, Player player, Map<String, PermittedObject> localScope) {
    input = input.trim();

    // Try number first
    try {
      return Double.parseDouble(input);
    } catch (NumberFormatException ignored) {
    }

    // Try boolean
    if ("true".equalsIgnoreCase(input)) return true;
    if ("false".equalsIgnoreCase(input)) return false;

    // Check if it's a variable
    if (localScope.containsKey(input)) return localScope.get(input).getValue();

    if ((input.startsWith("\"") && input.endsWith("\""))
        || (input.startsWith("'") && input.endsWith("'"))) {
      return input.substring(1, input.length() - 1);
    }

    return input.replaceAll("^\"|\"$", ""); // strip quotes if present
  }

  public ScriptEvaluator getScriptEvaluator() {
    return scriptEvaluator;
  }
}
