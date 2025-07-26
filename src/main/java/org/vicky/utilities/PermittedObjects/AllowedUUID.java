/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities.PermittedObjects;

import java.util.UUID;
import org.vicky.utilities.PermittedObject;

public record AllowedUUID(UUID value) implements PermittedObject<UUID> {
  @Override
  public UUID getValue() {
    return value;
  }
}
