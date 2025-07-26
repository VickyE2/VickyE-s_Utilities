/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.vicky.expansions.maths.engine.ExpressionEvaluator;
import org.vicky.expansions.maths.engine.FunctionManager;
import org.vicky.expansions.maths.engine.ScriptEngine;
import org.vicky.expansions.maths.engine.VariableManager;
import org.vicky.vicky_utils;

public class MathsPlaceholderExpansions extends PlaceholderExpansion {
  private final vicky_utils plugin;
  private final ScriptEngine engine;
  private final VariableManager variableManager;
  private final ExpressionEvaluator evaluator;
  private final FunctionManager manager;

  public MathsPlaceholderExpansions(vicky_utils plugin) {
    this.plugin = plugin;
    this.variableManager = new VariableManager();
    this.manager = new FunctionManager(variableManager);
    this.evaluator = new ExpressionEvaluator(variableManager, manager);
    this.engine = new ScriptEngine(variableManager, manager, evaluator);
  }

  @Override
  public boolean canRegister() {
    return true;
  }

  @Override
  public @NotNull String getIdentifier() {
    return "papimaths";
  }

  /*
  %papimaths_let:x=5*2% → assigns x = 10
  %papimaths_var:x% → returns 10
  %papimaths_x+2% → evaluates to 12 if x was 10
  %papimaths_let:y=papi:player_level% → sets y from a PAPI placeholder
  %papimaths_var:y + 10% → adds 10 to the stored value of y
  */

  @Override
  public @NotNull String getAuthor() {
    return String.join(", ", plugin.getDescription().getAuthors());
  }

  @Override
  public @NotNull String getVersion() {
    return plugin.getDescription().getVersion();
  }

  @Override
  public String onPlaceholderRequest(Player player, @NotNull String identifier) {
    if (identifier.equalsIgnoreCase("test")) {
      return "hello from maths! 🧮.... It works according to the plan :D";
    }

    try {
      info(identifier);
      return engine.evaluate(identifier, player);
    } catch (Exception e) {
      return "Math error";
    }
  }
}
