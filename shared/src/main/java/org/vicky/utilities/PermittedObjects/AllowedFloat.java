/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.PermittedObjects;

import org.vicky.utilities.PermittedObject;

public record AllowedFloat(Float value) implements PermittedObject<Float> {
	@Override
	public Float getValue() {
		return value;
	}
}
