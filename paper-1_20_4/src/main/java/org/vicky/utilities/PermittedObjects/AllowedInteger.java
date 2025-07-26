/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.PermittedObjects;

import org.vicky.utilities.PermittedObject;

public record AllowedInteger(Integer value) implements PermittedObject<Integer> {
  @Override
  public Integer getValue() {
    return value;
  }
}
