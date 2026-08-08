/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.item;

import org.vicky.platform.tags.ItemDataFormat;
import org.vicky.platform.tags.SerializedItemData;

public interface ItemData {

	ItemDataFormat format();

	boolean isEmpty();

	ItemData copy();

	SerializedItemData serialize();
}