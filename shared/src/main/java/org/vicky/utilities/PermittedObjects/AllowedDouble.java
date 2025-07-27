/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.PermittedObjects;

import org.vicky.utilities.PermittedObject;

public record AllowedDouble(Object value) implements PermittedObject<Double> {
  @Override
  public Double getValue() {
    if (value instanceof Double doubl3) return doubl3;
    else {
      try {
        return Double.parseDouble(value.toString());
      } catch (NumberFormatException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
