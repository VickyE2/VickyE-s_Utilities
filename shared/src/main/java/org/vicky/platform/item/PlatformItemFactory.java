/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.item;

import org.jetbrains.annotations.Nullable;
import org.vicky.platform.item.data.SerializedItemStack;
import org.vicky.platform.utils.ResourceLocation;

public interface PlatformItemFactory {

	@Deprecated
	PlatformItemStack create(ResourceLocation item, int count, @Nullable String data) throws InvalidItemException;

	@Deprecated
	default PlatformItemStack create(ResourceLocation item) throws InvalidItemException {
		return create(item, 1, null);
	}

	@Deprecated
	default @Nullable PlatformItemStack createSafe(ResourceLocation item) {
		try {
			return create(item, 1, null);
		} catch (InvalidItemException e) {
			return null;
		}
	}

	SerializedItemStack serialize(PlatformItemStack stack);

	PlatformItemStack deserialize(SerializedItemStack serialized) throws ItemDeserializationException;

	PlatformItemStack getEmpty();

	class InvalidItemException extends Exception {
		public InvalidItemException(String itemID) {
			super("Invalid item: " + itemID);
		}

		public InvalidItemException(ResourceLocation itemID) {
			super("Invalid item: " + itemID);
		}

		public InvalidItemException() {
		}
	}

	final class ItemDeserializationException extends Exception {

		public ItemDeserializationException(String message) {
			super(message);
		}

		public ItemDeserializationException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}