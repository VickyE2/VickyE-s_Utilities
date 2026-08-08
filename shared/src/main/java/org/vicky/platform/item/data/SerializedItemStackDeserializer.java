/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.item.data;

import com.google.gson.*;
import org.vicky.platform.tags.ItemDataFormat;

import java.lang.reflect.Type;

public class SerializedItemStackDeserializer implements JsonDeserializer<SerializedItemStack> {
	@Override
	public SerializedItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();

		// 1. Extract required fields
		String item = obj.get("item").getAsString();

		// 2. Extract optional count (default to null so your secondary constructor
		// handles it)
		Integer count = obj.has("count") ? obj.get("count").getAsInt() : null;

		// 3. Handle the legacy "nbt" shorthand key mapping to payload
		if (obj.has("nbt") || (!obj.has("payload") && !obj.has("format"))) {
			String nbt = obj.has("nbt") ? obj.get("nbt").getAsString() : "{}";
			// This safely invokes your custom secondary constructor!
			return new SerializedItemStack(item, count, nbt);
		}

		// 4. Otherwise, handle standard explicit format / components payload
		ItemDataFormat format = obj.has("format")
				? ItemDataFormat.valueOf(obj.get("format").getAsString())
				: ItemDataFormat.LEGACY_NBT;

		String payload = obj.has("payload") ? obj.get("payload").getAsString() : "{}";
		Integer dataVersion = obj.has("dataVersion") ? obj.get("dataVersion").getAsInt() : null;

		return new SerializedItemStack(item, count != null ? count : 1, format, payload, dataVersion);
	}
}