/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.PermittedObjects;

import org.vicky.utilities.PermittedObject;

public record AllowedBoolean(Boolean bool) implements PermittedObject<Boolean> {
	@Override
	public Boolean getValue() {
		return bool;
	}
}
