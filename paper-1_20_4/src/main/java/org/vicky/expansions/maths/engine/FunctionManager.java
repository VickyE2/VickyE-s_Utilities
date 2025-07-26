/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.vicky.expansions.maths.model.MimFunction;
import org.vicky.utilities.PermittedObject;
import org.vicky.utilities.PermittedObjects.AllowedDouble;

public class FunctionManager {
  private final Map<String, MimFunction> globalFunctions = new HashMap<>();
  private final Map<String, Map<String, MimFunction>> localFunctions = new HashMap<>();
  private final VariableManager variableManager;

  public FunctionManager(VariableManager variableManager) {
    this.variableManager = variableManager;
    registerBuiltIns(); // Like lerp, pow, etc.
  }

  public static double resolveArg(String arg, VariableManager vm, String scope) {
    if (arg.startsWith("var:")) {
      String varName = arg.substring(4);
      PermittedObject val = scope == null ? vm.getValue(varName) : vm.getValue(varName, scope);
      if (val instanceof AllowedDouble d) return d.getValue();
      throw new IllegalArgumentException("Variable not found or not a number: " + varName);
    } else {
      return Double.parseDouble(arg);
    }
  }

  public void registerFunction(String name, MimFunction function) {
    globalFunctions.put(name, function);
  }

  public void registerLocalFunction(String scopeId, String name, MimFunction function) {
    localFunctions.computeIfAbsent(scopeId, k -> new HashMap<>()).put(name, function);
  }

  public void clearLocalFunctions(String scopeId) {
    localFunctions.remove(scopeId);
  }

  public String evaluateCustomFunctions(
      String input, String scopeId, ExpressionEvaluator evaluator) {
    var parser = new FunctionParser(this, evaluator, scopeId);
    return parser.parseAndEvaluate(input);
  }

  public MimFunction getFunction(String scopeId, String name) {
    if (scopeId != null && localFunctions.containsKey(scopeId)) {
      Map<String, MimFunction> local = localFunctions.get(scopeId);
      if (local.containsKey(name)) return local.get(name);
    }
    return globalFunctions.get(name);
  }

  public VariableManager getVariableManager() {
    return variableManager;
  }

  private void registerBuiltIns() {
    registerFunction(
        "lerp",
        new MimFunction(
            (args, vm, scope) ->
                safeCall(
                    args,
                    3,
                    () -> {
                      double a = resolveArg(args.get(0), vm, scope);
                      double b = resolveArg(args.get(1), vm, scope);
                      double t = resolveArg(args.get(2), vm, scope);
                      return a + (b - a) * t;
                    })));

    registerFunction(
        "clamp",
        new MimFunction(
            (args, vm, scope) ->
                safeCall(
                    args,
                    3,
                    () -> {
                      double value = resolveArg(args.get(0), vm, scope);
                      double min = resolveArg(args.get(1), vm, scope);
                      double max = resolveArg(args.get(2), vm, scope);
                      return Math.max(min, Math.min(max, value));
                    })));

    registerFunction(
        "pow",
        new MimFunction(
            (args, vm, scope) ->
                safeCall(
                    args,
                    2,
                    () ->
                        Math.pow(
                            resolveArg(args.get(0), vm, scope),
                            resolveArg(args.get(1), vm, scope)))));

    registerFunction(
        "sqrt",
        new MimFunction(
            (args, vm, scope) ->
                safeCall(args, 1, () -> Math.sqrt(resolveArg(args.get(0), vm, scope)))));

    registerFunction(
        "mod",
        new MimFunction(
            (args, vm, scope) ->
                safeCall(
                    args,
                    2,
                    () ->
                        resolveArg(args.get(0), vm, scope) % resolveArg(args.get(1), vm, scope))));

    registerFunction(
        "sin",
        new MimFunction(
            (args, vm, scope) ->
                safeCall(args, 1, () -> Math.sin(resolveArg(args.get(0), vm, scope)))));

    registerFunction("rand", new MimFunction((args, vm, scope) -> String.valueOf(Math.random())));

    registerFunction(
        "randInt",
        new MimFunction(
            (args, vm, scope) ->
                safeCall(
                    args,
                    2,
                    () ->
                        new Random()
                            .nextInt(
                                (int) resolveArg(args.get(0), vm, scope),
                                (int) resolveArg(args.get(1), vm, scope)))));

    registerFunction(
        "randD",
        new MimFunction(
            (args, vm, scope) ->
                safeCall(
                    args,
                    2,
                    () ->
                        new Random()
                            .nextDouble(
                                resolveArg(args.get(0), vm, scope),
                                resolveArg(args.get(1), vm, scope)))));
  }

  private String safeCall(List<String> args, int expected, ThrowingDoubleSupplier supplier) {
    if (args.size() != expected) return "NaN";
    try {
      return String.valueOf(supplier.getAsDouble());
    } catch (Exception e) {
      e.printStackTrace();
      return "NaN";
    }
  }

  public boolean hasFunction(String funcName) {
    return globalFunctions.containsKey(funcName);
  }

  public boolean hasLocalFunction(String scope, String funcName) {
    return localFunctions.getOrDefault(scope, Map.of()).containsKey(funcName);
  }

  public Map<String, MimFunction> getAllGlobalFunctions() {
    return globalFunctions;
  }

  @FunctionalInterface
  interface ThrowingDoubleSupplier {
    double getAsDouble() throws Exception;
  }
}
