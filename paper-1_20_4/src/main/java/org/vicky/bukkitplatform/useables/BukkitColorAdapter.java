/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import org.vicky.platform.IColor;

public class BukkitColorAdapter {
	public static @NotNull Color adapt(IColor from) {
		return Color.fromRGB((int) Math.max(0, Math.min(255, from.getRed())),
				(int) Math.max(0, Math.min(255, from.getGreen())), (int) Math.max(0, Math.min(255, from.getBlue())));
	}
}
