/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.engine;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.vicky.expansions.maths.model.MimFunction;
import org.vicky.expansions.maths.model.MimScript;
import org.vicky.utilities.PermittedObject;
import org.vicky.utilities.PermittedObjects.AllowedDouble;
import org.vicky.utilities.PermittedObjects.AllowedString;

public class ScriptEngine {
  private final VariableManager variableManager;
  private final FunctionManager functionManager;
  private final ExpressionEvaluator evaluator;
  private final DebugLogger logger;
  private final LibraryManager scriptManager;

  public ScriptEngine(
      VariableManager variableManager,
      FunctionManager functionManager,
      ExpressionEvaluator evaluator) {
    this.variableManager = variableManager;
    this.functionManager = functionManager;
    this.evaluator = evaluator;
    this.logger = new DebugLogger();
    this.variableManager.registerDefaultVariables();
    this.scriptManager = new LibraryManager();
  }

  public String evaluate(String expr, Player player) {
    return evaluate(expr, player, null);
  }

  /**
   * Main evaluation entry point.
   */
  public String evaluate(String expr, Player player, String localScope) {
    expr = expr.trim();

    if (expr.startsWith("return:")) {
      String returnExpr = expr.substring("return:".length()).trim();
      return String.valueOf(evaluateExpression(returnExpr, player, localScope));
    }
    if (expr.startsWith("state:")) {
      String[] parts = expr.substring(6).split("\\.", 2);
      if (parts.length == 2) {
        return variableManager.getStateValue(parts[0], parts[1]).getValue().toString();
      }
    }
    // Variable assignment with let:
    if (expr.startsWith("let:")) {
      int eqIdx = expr.indexOf('=');
      if (eqIdx > 4) {
        String varName = expr.substring(4, eqIdx).trim();
        String varExpr = expr.substring(eqIdx + 1).trim();
        if (varExpr.contains("\"'")) {
          variableManager.set(varName, new AllowedString(varExpr), localScope, localScope == null);
          return varExpr;
        }
        double value = evaluateExpression(varExpr, player, null); // No local scope
        variableManager.set(
            varName,
            new AllowedDouble(value),
            localScope,
            localScope != null); // Global variable set
        return String.valueOf(value);
      }
      return "Invalid let syntax";
    }
    // Function registration
    if (expr.startsWith("func:") || expr.startsWith("funcOverride:")) {
      boolean override = false;
      String unparsedFunc;
      if (expr.startsWith("func:")) {
        unparsedFunc = expr.substring("func:".length());
      } else {
        override = true;
        unparsedFunc = expr.substring("funcOverride:".length());
      }

      String[] parts = unparsedFunc.split("=", 2);
      if (parts.length == 2) {
        String[] functionParts = parts[0].split("\\(", 2);
        if (functionParts.length != 2) return "Invalid function definition";
        String funcName = functionParts[0].trim();
        String argsStr = functionParts[1].replace(")", "").trim();
        List<String> argsList = new ArrayList<>();
        if (!argsStr.isEmpty()) {
          for (String arg : argsStr.split(",")) {
            argsList.add(arg.trim());
          }
        }
        // Check if function exists and override flag
        if (functionManager.hasFunction(funcName) && !override) {
          return "Function with name "
              + funcName
              + " already registered. Use funcOverride instead.";
        }
        MimFunction mimFunction = new MimFunction(argsList, parts[1].trim());
        functionManager.registerFunction(funcName, mimFunction);
        return "Function " + funcName + " registered.";
      } else {
        return "Invalid function format";
      }
    }
    // Variable access
    if (expr.startsWith("var:")) {
      String varName = expr.substring(4).trim();
      PermittedObject val =
          variableManager.getValue(varName); // Let's assume this returns double or NaN
      return String.valueOf(val);
    }
    // Debug toggle
    if (expr.startsWith("debug:")) {
      boolean enable = expr.endsWith("on");
      logger.toggles.put(player.getUniqueId(), enable);
      return "Debug mode " + (enable ? "enabled" : "disabled") + ".";
    }
    // Script storage
    if (expr.startsWith("script:") || expr.startsWith("scriptOverride:")) {
      boolean scriptOverride = expr.startsWith("scriptOverride:");
      String[] split = expr.split(":", 3);
      if (split.length < 3) return "Invalid script format";
      String scriptName = split[1];
      String fullBody = split[2].replace("|", "\n");
      if (scriptManager.hasScript(scriptName) && !scriptOverride) {
        return "Script '" + scriptName + "' already exists. Use scriptOverride:... to overwrite.";
      }
      var script = new MimScript(scriptName, fullBody, this);
      scriptManager.registerScript(scriptName, script);
      return "Script '" + scriptName + "' stored.";
    }
    // Spript evaluation
    if (expr.startsWith("eval:")) {
      // Example: eval:scriptName*global or eval:scriptName
      String evalPart = expr.substring(5);
      boolean isGlobal = evalPart.endsWith("*global");
      String scriptName = isGlobal ? evalPart.substring(0, evalPart.length() - 7) : evalPart;
      MimScript script = scriptManager.getScript(scriptName);
      if (script == null) return "No script with name: " + scriptName;
      return script.evaluate(player).toString();
    }
    // Default: Evaluate as expression with global variables only
    double result = evaluateExpression(expr, player, localScope);
    return String.valueOf(result);
  }

  /**
   * Evaluate a mathematical expression string with variables.
   * Here you must implement the logic to parse and evaluate expressions,
   * respecting variable scoping and function calls.
   * <p>
   * For now, this is a placeholder stub.
   */
  public double evaluateExpression(String expr, Player player, String localScope) {
    try {
      return evaluator.eval(expr, localScope, player);
    } catch (Exception e) {
      logger.log(player, e.getMessage());
      return Double.NaN;
    }
  }

  public DebugLogger getLogger() {
    return logger;
  }

  public VariableManager getVariableManager() {
    return variableManager;
  }

  public FunctionManager getFunctionManager() {
    return functionManager;
  }

  public ExpressionEvaluator getEvaluator() {
    return evaluator;
  }

  public LibraryManager getScriptManager() {
    return scriptManager;
  }
}
