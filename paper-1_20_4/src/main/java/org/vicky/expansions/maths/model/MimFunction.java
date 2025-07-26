/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lonelibs.org.jetbrains.annotations.NotNull;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.vicky.expansions.maths.engine.VariableManager;
import org.vicky.utilities.TriFunction;

public class MimFunction {
  private final List<String> args;
  private final String body;
  private final boolean isInBuilt;
  private final Function<List<Double>, String> evaluator;
  private final TriFunction<List<String>, VariableManager, String, String> varEvaluator;

  public MimFunction(
      @NotNull TriFunction<List<String>, VariableManager, String, String> varEvaluator) {
    this.args = null;
    this.body = null;
    this.evaluator = null;
    this.varEvaluator = varEvaluator;
    this.isInBuilt = true;
  }

  public MimFunction(@NotNull List<String> args, @NotNull String functionBody) {
    this.args = args;
    this.body = functionBody;
    this.evaluator = null;
    this.varEvaluator = null;
    this.isInBuilt = false;
  }

  public MimFunction(@NotNull Function<List<Double>, String> evaluator) {
    this.args = null;
    this.body = null;
    this.evaluator = evaluator;
    this.varEvaluator = null;
    this.isInBuilt = false;
  }

  /**
   * Evaluate with passed arguments.
   * Returns evaluated result as a string or "NaN" if failed.
   */
  public String evaluate(List<Double> argValues) {
    if (evaluator != null) {
      return evaluator.apply(argValues);
    }

    if (args == null || body == null || args.size() != argValues.size()) {
      return "NaN"; // argument count mismatch
    }

    Map<String, Double> localVarMap = new HashMap<>();
    for (int i = 0; i < args.size(); i++) {
      localVarMap.put(args.get(i), argValues.get(i));
    }

    try {
      Expression expression = new ExpressionBuilder(body).variables(localVarMap.keySet()).build();

      for (Map.Entry<String, Double> entry : localVarMap.entrySet()) {
        expression.setVariable(entry.getKey(), entry.getValue());
      }

      return String.valueOf(expression.evaluate());
    } catch (Exception e) {
      e.printStackTrace();
      return "NaN";
    }
  }

  public String evaluateRawArgs(List<String> rawArgs, VariableManager vm, String scope) {
    if (varEvaluator != null) {
      return varEvaluator.apply(rawArgs, vm, scope);
    }
    return "NaN";
  }

  public List<String> getArguments() {
    return args;
  }

  public String getBody() {
    return body;
  }

  public boolean isInBuilt() {
    return isInBuilt;
  }
}
