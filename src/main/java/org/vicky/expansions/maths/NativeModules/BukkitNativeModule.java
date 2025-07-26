/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.NativeModules;

import java.util.Map;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.vicky.expansions.maths.engine.ScriptEvaluator;

public class BukkitNativeModule implements NativeModule {
  Map<String, ScriptEvaluator.FunctionDefinition> functionDefinitions =
      Map.of(
          "print",
          (ctx, args) -> {
            String msg = !args.isEmpty() ? String.valueOf(args.get(0)) : "";
            Bukkit.getConsoleSender().sendMessage(msg);
            return null;
          });

  @Override
  public void importInto(ScriptEvaluator evaluator) {
    for (Map.Entry<String, ScriptEvaluator.FunctionDefinition> var : functionDefinitions.entrySet())
      evaluator.defineFunction("bukkit." + var.getKey(), var.getValue());
  }

  @Override
  public void importFunction(
      ScriptEvaluator scriptEvaluator, @NotNull String funcName, @Nullable String alias) {
    if (functionDefinitions.containsKey(funcName)) {
      scriptEvaluator.defineFunction(
          alias != null ? alias : funcName, functionDefinitions.get(funcName));
    }
  }
}
