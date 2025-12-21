/* Licensed under Apache-2.0 2024. */
package org.vicky.forge.forgeplatform.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class AdventureComponentConverter {
	public static net.minecraft.network.chat.Component toNative(Component adventureComponent) {
		String json = GsonComponentSerializer.gson().serialize(adventureComponent);
		return net.minecraft.network.chat.Component.Serializer.fromJson(json);
	}

	public static Component fromNative(net.minecraft.network.chat.Component mcComponent) {
		String json = net.minecraft.network.chat.Component.Serializer.toJson(mcComponent);
		return GsonComponentSerializer.gson().deserialize(json);
	}
}