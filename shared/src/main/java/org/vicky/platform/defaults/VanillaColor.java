/* Licensed under Apache-2.0 2025. */
package org.vicky.platform.defaults;

import org.vicky.platform.IColor;

public class VanillaColor implements IColor {
	private final int r, g, b;

	public VanillaColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public float getRed() {
		return r / 255f;
	}

	public float getGreen() {
		return g / 255f;
	}

	public float getBlue() {
		return b / 255f;
	}
}
