/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

/**
 * A functional interface for a lambda that accepts three arguments.
 *
 * @param <A> Type of the first argument.
 * @param <B> Type of the second argument.
 * @param <C> Type of the third argument.
 * @param <R> Return type.
 */
@FunctionalInterface
public interface TriFunction<A, B, C, R> {
  R apply(A a, B b, C c);
}
