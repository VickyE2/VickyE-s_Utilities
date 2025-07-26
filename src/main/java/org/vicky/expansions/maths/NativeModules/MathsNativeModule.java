/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.NativeModules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.expansions.maths.engine.ScriptEvaluator;

public class MathsNativeModule implements NativeModule {

  Map<String, ScriptEvaluator.FunctionDefinition> functionDefinitions = new HashMap<>();

  public MathsNativeModule() {
    functionDefinitions.put("sin", (ctx, args) -> Math.sin(toDouble(args, 0)));
    functionDefinitions.put("cos", (ctx, args) -> Math.cos(toDouble(args, 0)));
    functionDefinitions.put("tan", (ctx, args) -> Math.tan(toDouble(args, 0)));
    functionDefinitions.put("asin", (ctx, args) -> Math.asin(toDouble(args, 0)));
    functionDefinitions.put("acos", (ctx, args) -> Math.acos(toDouble(args, 0)));
    functionDefinitions.put("atan", (ctx, args) -> Math.atan(toDouble(args, 0)));

    functionDefinitions.put("sqrt", (ctx, args) -> Math.sqrt(toDouble(args, 0)));
    functionDefinitions.put("pow", (ctx, args) -> Math.pow(toDouble(args, 0), toDouble(args, 1)));
    functionDefinitions.put("log", (ctx, args) -> Math.log(toDouble(args, 0))); // natural log
    functionDefinitions.put("log10", (ctx, args) -> Math.log10(toDouble(args, 0)));
    functionDefinitions.put("exp", (ctx, args) -> Math.exp(toDouble(args, 0)));

    functionDefinitions.put("abs", (ctx, args) -> Math.abs(toDouble(args, 0)));
    functionDefinitions.put("floor", (ctx, args) -> Math.floor(toDouble(args, 0)));
    functionDefinitions.put("ceil", (ctx, args) -> Math.ceil(toDouble(args, 0)));
    functionDefinitions.put("round", (ctx, args) -> (double) Math.round(toDouble(args, 0)));

    functionDefinitions.put("min", (ctx, args) -> Math.min(toDouble(args, 0), toDouble(args, 1)));
    functionDefinitions.put("max", (ctx, args) -> Math.max(toDouble(args, 0), toDouble(args, 1)));

    functionDefinitions.put(
        "clamp",
        (ctx, args) -> {
          double val = toDouble(args, 0);
          double min = toDouble(args, 1);
          double max = toDouble(args, 2);
          return Math.max(min, Math.min(max, val));
        });

    functionDefinitions.put("sign", (ctx, args) -> Math.signum(toDouble(args, 0)));

    functionDefinitions.put("degToRad", (ctx, args) -> Math.toRadians(toDouble(args, 0)));
    functionDefinitions.put("radToDeg", (ctx, args) -> Math.toDegrees(toDouble(args, 0)));
  }

  @Override
  public void importInto(ScriptEvaluator evaluator) {
    for (Map.Entry<String, ScriptEvaluator.FunctionDefinition> entry :
        functionDefinitions.entrySet()) {
      evaluator.defineFunction("math." + entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void importFunction(
      ScriptEvaluator evaluator, @NotNull String funcName, @Nullable String alias) {
    ScriptEvaluator.FunctionDefinition func = functionDefinitions.get(funcName);
    if (func != null) {
      evaluator.defineFunction(alias != null ? alias : funcName, func);
    }
  }

  private double toDouble(List<Object> args, int index) {
    if (index < args.size()) {
      Object val = args.get(index);
      if (val instanceof Number) {
        return ((Number) val).doubleValue();
      }
      try {
        return Double.parseDouble(String.valueOf(val));
      } catch (NumberFormatException ignored) {
      }
    }
    return 0.0;
  }
}
