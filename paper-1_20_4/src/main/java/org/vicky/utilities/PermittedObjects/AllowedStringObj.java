/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.PermittedObjects;

import org.vicky.utilities.PermittedObject;

public record AllowedStringObj(Object value) implements PermittedObject<String> {
  @Override
  public String getValue() {
    if (value instanceof Double doubl3) return doubl3.toString();
    else {
      return value.toString();
    }
  }
}
