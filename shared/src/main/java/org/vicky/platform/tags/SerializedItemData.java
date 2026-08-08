/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.tags;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record SerializedItemData(ItemDataFormat format, String payload, @Nullable Integer dataVersion) {
	public SerializedItemData {
		Objects.requireNonNull(format, "format");
		Objects.requireNonNull(payload, "payload");
	}

	public static SerializedItemData legacy(String snbt) {
		return new SerializedItemData(ItemDataFormat.LEGACY_NBT, snbt, null);
	}

	public static SerializedItemData components(String encodedComponents, @Nullable Integer dataVersion) {
		return new SerializedItemData(ItemDataFormat.DATA_COMPONENTS, encodedComponents, dataVersion);
	}
}