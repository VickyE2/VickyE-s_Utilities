/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.PermittedObjects;

import org.vicky.utilities.PermittedObject;

public record AllowedString(String value) implements PermittedObject<String> {
  @Override
  public String getValue() {
    return value;
  }
}
