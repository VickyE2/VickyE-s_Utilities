/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.item.data;

import org.vicky.platform.item.ItemData;
import org.vicky.platform.tags.CompoundData;
import org.vicky.platform.tags.ItemDataFormat;
import org.vicky.platform.tags.SerializedItemData;

import java.util.Objects;

public final class LegacyItemData implements ItemData {

	private final CompoundData tag;

	public LegacyItemData(CompoundData tag) {
		this.tag = Objects.requireNonNull(tag, "tag");
	}

	public CompoundData tag() {
		return tag;
	}

	@Override
	public ItemDataFormat format() {
		return ItemDataFormat.LEGACY_NBT;
	}

	@Override
	public boolean isEmpty() {
		return tag.isEmpty();
	}

	@Override
	public ItemData copy() {
		return new LegacyItemData(tag.copy());
	}

	@Override
	public SerializedItemData serialize() {
		return SerializedItemData.legacy(tag.asString());
	}
}