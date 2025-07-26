/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.engine;

import static org.vicky.expansions.maths.engine.FunctionManager.resolveArg;

import java.util.ArrayList;
import java.util.List;
import org.vicky.expansions.maths.model.MimFunction;

public class FunctionParser {

  private final FunctionManager functionManager;
  private final ExpressionEvaluator evaluator;
  private final String scope;

  public FunctionParser(
      FunctionManager functionManager, ExpressionEvaluator evaluator, String scope) {
    this.functionManager = functionManager;
    this.evaluator = evaluator;
    this.scope = scope;
  }

  public String parseAndEvaluate(String input) {
    StringBuilder output = new StringBuilder();
    int i = 0;
    while (i < input.length()) {
      if (Character.isLetter(input.charAt(i))) {
        int start = i;
        while (i < input.length()
            && (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
          i++;
        }
        String funcName = input.substring(start, i);

        if (i < input.length() && input.charAt(i) == '(') {
          int openParenIndex = i;
          int closeParenIndex = findMatchingParen(input, openParenIndex);
          if (closeParenIndex == -1) throw new IllegalArgumentException("Mismatched parentheses");

          String argsString = input.substring(openParenIndex + 1, closeParenIndex);
          List<String> rawArgs = splitArgs(argsString);

          List<String> evaluatedArgs = new ArrayList<>();
          for (String arg : rawArgs) {
            String parsedArg = parseAndEvaluate(arg); // Recursively resolve nested functions
            evaluatedArgs.add(parsedArg); // We pass string expressions to MimFunction
          }

          MimFunction function = functionManager.getFunction(scope, funcName);
          if (function == null) {
            throw new IllegalArgumentException("Function not found: " + funcName);
          }

          String result;
          if (function.isInBuilt()) {
            result =
                function.evaluateRawArgs(
                    evaluatedArgs, functionManager.getVariableManager(), scope);
          } else {
            List<Double> parsedArgs = new ArrayList<>();
            try {
              for (String arg : evaluatedArgs) {
                parsedArgs.add(resolveArg(arg, functionManager.getVariableManager(), scope));
              }
              result = function.evaluate(parsedArgs);
            } catch (Exception e) {
              e.printStackTrace();
              result = "NaN"; // fallback on failure
            }
          }
          output.append(result);
          i = closeParenIndex + 1;
        } else {
          output.append(funcName);
        }
      } else {
        output.append(input.charAt(i));
        i++;
      }
    }
    return output.toString();
  }

  private int findMatchingParen(String s, int openPos) {
    int count = 0;
    for (int i = openPos; i < s.length(); i++) {
      if (s.charAt(i) == '(') count++;
      else if (s.charAt(i) == ')') count--;
      if (count == 0) return i;
    }
    return -1; // no matching
  }

  private List<String> splitArgs(String argsString) {
    List<String> args = new ArrayList<>();
    int parenCount = 0;
    StringBuilder currentArg = new StringBuilder();

    for (int i = 0; i < argsString.length(); i++) {
      char c = argsString.charAt(i);
      if (c == ',' && parenCount == 0) {
        args.add(currentArg.toString().trim());
        currentArg.setLength(0);
      } else {
        if (c == '(') parenCount++;
        else if (c == ')') parenCount--;
        currentArg.append(c);
      }
    }
    args.add(currentArg.toString().trim());

    return args;
  }
}
