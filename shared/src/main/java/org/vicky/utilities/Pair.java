/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

public record Pair<T, U>(T key, U value) {
  public static <T, U> Pair<T, U> of(T key, U value) {
    return new Pair<>(key, value);
  }

  public T getKey() {
    return key;
  }

  public U getValue() {
    return value;
  }

  public T getFirst() {
    return key;
  }

  public U getSecond() {
    return value;
  }

  public T component1() { return key; }

  public U component2() { return value; }
}
