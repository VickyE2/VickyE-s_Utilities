/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.item.data;

import org.jetbrains.annotations.Nullable;
import org.vicky.platform.tags.ItemDataFormat;

public record SerializedItemStack(String item, int count, ItemDataFormat format, String payload,
		@Nullable Integer dataVersion) {
	public SerializedItemStack(String item, @Nullable Integer count, String nbt) {
		this(item, count != null ? count : 1, ItemDataFormat.LEGACY_NBT, nbt, null);
	}
}