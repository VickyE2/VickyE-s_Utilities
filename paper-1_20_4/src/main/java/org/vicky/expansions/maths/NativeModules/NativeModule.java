/* Licensed under Apache-2.0 2024. */
package org.vicky.expansions.maths.NativeModules;

import org.vicky.expansions.maths.engine.ScriptEvaluator;

public interface NativeModule {
  void importInto(ScriptEvaluator evaluator);

  void importFunction(ScriptEvaluator scriptEvaluator, String funcName, String funcName1);
}
