/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

/**
 * Marker interface to enforce allowed types at compile-time.
 */
public interface PermittedObject<T> {
  T getValue();
}
