/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities.PermittedObjects;

import java.util.List;

import org.vicky.utilities.PermittedObject;

public record AllowedList<T>(List<T> list) implements PermittedObject<List<T>> {
	@Override
	public List<T> getValue() {
		return list;
	}
}
