/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities;

public record Pair<T, U>(T key, U value) {
	public T getKey() {
		return key;
	}

	public U getValue() {
		return value;
	}
}
