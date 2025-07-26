/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.PermittedObjects;

import org.vicky.utilities.PermittedObject;

public record AllowedEnum<E extends Enum<E>>(E value) implements PermittedObject<Enum<E>> {
  @Override
  public Enum<E> getValue() {
    return value;
  }
}
