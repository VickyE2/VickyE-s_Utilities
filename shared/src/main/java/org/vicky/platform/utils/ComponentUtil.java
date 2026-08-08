/* Licensed under Apache-2.0 2026. */
package org.vicky.platform.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComponentUtil {
	public static List<String> convertToFormatted(List<Component> components) {
		List<String> strings = new ArrayList<>();
		for (Component t : components) {
			strings.add(PlainTextComponentSerializer.plainText().serialize(t));
		}
		return strings;
	}

	public static Component create() {
		return createStr("");
	}

	public static Component createStr(String str) {
		return Component.text(str);
	}
	public static Component colorize(String text) {
		// Simple version: replace & or § codes with MC format codes and parse
		// You might use TextComponent, but better to use Component.literal with
		// formatting
		// For advanced: parse hex colors manually, or use adventure-text if you have it
		return ComponentUtil.createTranslated(applyMinecraftColorCodes(text));
	}

	private static String applyMinecraftColorCodes(String text) {
		return text.replace('&', '§'); // if you use & in config instead of §
	}

	public static Component create(Component... all) {
		Component base = create();
		for (Component tc : all) {
			base.append(tc);
		}
		return base;
	}

	public static Component createTranslated(String unlocalized, Object... params) {
		var translatable = Component.translatable().key(unlocalized);
		if (params != null && params.length > 0) {
			translatable.args(Arrays.stream(params).map(Object::toString).map(ComponentUtil::createStr).toList());
		}
		return translatable.build();
	}
}