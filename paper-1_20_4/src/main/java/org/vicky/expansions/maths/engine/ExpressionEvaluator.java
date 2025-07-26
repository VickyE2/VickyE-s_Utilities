/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.engine;

import java.util.HashSet;
import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.vicky.utilities.PermittedObject;
import org.vicky.utilities.PermittedObjects.AllowedDouble;
import org.vicky.vicky_utils;

public class ExpressionEvaluator {

  private final VariableManager variableManager;
  private final FunctionManager functionManager;

  public ExpressionEvaluator(
      @NotNull VariableManager variableManager, @NotNull FunctionManager functionManager) {
    this.variableManager = variableManager;
    this.functionManager = functionManager;
  }

  /**
   * Evaluate the expression with global scope.
   */
  public double evaluateRaw(String rawExpr, Player player) {
    return eval(rawExpr, null, player);
  }

  /**
   * Evaluate the expression with an optional scope for local variables.
   */
  public double eval(String rawExpr, String scope, Player player) {
    try {
      String varProcessed = variableManager.replaceVariables(rawExpr, scope, player);
      String funcProcessed = functionManager.evaluateCustomFunctions(varProcessed, scope, this);
      Set<String> allVariables = extractVariables(funcProcessed);
      Expression expression = new ExpressionBuilder(funcProcessed).build();
      for (String var : allVariables) {
        PermittedObject value =
            scope == null ? variableManager.getValue(var) : variableManager.getValue(var, scope);
        if (value instanceof AllowedDouble doubl3) expression.setVariable(var, doubl3.getValue());
      }
      vicky_utils.getPlugin().getLogger().info(String.format("String expr %s", funcProcessed));
      return expression.evaluate();
    } catch (Exception e) {
      e.printStackTrace();
      return Double.NaN;
    }
  }

  private Set<String> extractVariables(String expr) {
    // crude but fast: match all word-like tokens that aren't function names
    Set<String> variables = new HashSet<>();
    for (String token : expr.split("[^a-zA-Z0-9_]")) {
      if (!token.isEmpty() && !Character.isDigit(token.charAt(0))) {
        variables.add(token);
      }
    }
    return variables;
  }
}
