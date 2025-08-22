/* Licensed under Apache-2.0 2025. */
package org.vicky.bukkitplatform.useables;

import org.bukkit.boss.BarColor;
import org.vicky.platform.IColor;

public final class ColorAdapters {

	private ColorAdapters() {
	}

	public static BarColor adaptColor(IColor color) {
		if (color == null)
			return BarColor.WHITE;

		// 1) Try hex string if available
		String hex = null;
		try {
			hex = color.toHex();
		} catch (Throwable ignored) {
		}
		if (hex != null) {
			hex = hex.trim();
			if (hex.startsWith("#"))
				hex = hex.substring(1);
			if (hex.length() == 6) {
				try {
					int rgb = Integer.parseInt(hex, 16);
					int r = (rgb >> 16) & 0xFF;
					int g = (rgb >> 8) & 0xFF;
					int b = rgb & 0xFF;
					return nearestBarColor(r, g, b);
				} catch (NumberFormatException ignored) {
				}
			}
		}

		// 2) Fall back to using floats
		float rf, gf, bf;
		try {
			rf = color.getRed();
			gf = color.getGreen();
			bf = color.getBlue();
		} catch (Throwable t) {
			return BarColor.WHITE;
		}

		int r = floatToByte(rf);
		int g = floatToByte(gf);
		int b = floatToByte(bf);

		return nearestBarColor(r, g, b);
	}

	// Convert a float channel to 0..255 int.
	// If value looks like 0..1 range we scale by 255, otherwise assume it's already
	// 0..255.
	private static int floatToByte(float v) {
		if (Float.isNaN(v) || Float.isInfinite(v))
			return 255;
		if (v <= 1.0f && v >= 0.0f) {
			return clampToByte(Math.round(v * 255f));
		} else {
			return clampToByte(Math.round(v));
		}
	}

	private static int clampToByte(int x) {
		if (x < 0)
			return 0;
		if (x > 255)
			return 255;
		return x;
	}

	private static BarColor nearestBarColor(int r, int g, int b) {
		BarColor best = BarColor.WHITE;
		double bestDist = Double.MAX_VALUE;
		for (BarColor bc : BarColor.values()) {
			int cand = approximateRgbForBarColor(bc);
			int cr = (cand >> 16) & 0xFF;
			int cg = (cand >> 8) & 0xFF;
			int cb = cand & 0xFF;
			double d = (r - cr) * (double) (r - cr) + (g - cg) * (double) (g - cg) + (b - cb) * (double) (b - cb);
			if (d < bestDist) {
				bestDist = d;
				best = bc;
			}
		}
		return best;
	}

	// Reasonable approximations for the Bukkit BarColor enum.
	private static int approximateRgbForBarColor(BarColor bc) {
		return switch (bc) {
			case PINK -> 0xFF55FF;
			case BLUE -> 0x5555FF; // slightly darker than earlier suggestion
			case RED -> 0xFF5555;
			case GREEN -> 0x55FF55;
			case YELLOW -> 0xFFFF55;
			case PURPLE -> 0xAA00FF;
			default -> 0xFFFFFF;
		};
	}
}