/* Licensed under Apache-2.0 2024. */
package org.vicky.effectsSystem.enums;

public enum EffectArrangementType {
  LEFT("(t % 10) * 40", "floor(t / 10) * 10"),
  RIGHT("-(t % 10) * 40", "floor(t / 10) * 10"),
  TOP("(t % 10) * 40", "-floor(t / 10) * 10"),
  BOTTOM("(t % 10) * 40", "floor(t / 10) * 10 + 100"),
  CENTERED("(t % 10) * 40 - 200", "floor(t / 10) * 10"),
  DIAGONAL("t * 10", "t * 10"),
  SPIRAL("cos(t) * 50 + 100", "sin(t) * 50 + 100"),
  CIRCLE("cos(t * 0.5) * 50 + 100", "sin(t * 0.5) * 50 + 100"),
  STACK_VERTICAL("0", "t * 20"),
  STACK_HORIZONTAL("t * 20", "0");

  private final String xEquation;
  private final String yEquation;

  EffectArrangementType(String xEquation, String yEquation) {
    this.xEquation = xEquation;
    this.yEquation = yEquation;
  }

  public String getxEquation() {
    return xEquation;
  }

  public String getyEquation() {
    return yEquation;
  }
}
