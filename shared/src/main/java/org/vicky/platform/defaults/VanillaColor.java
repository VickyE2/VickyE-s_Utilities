package org.vicky.platform.defaults;

import org.vicky.platform.IColor;

public class VanillaColor implements IColor {
    private final int r, g, b;

    public VanillaColor(int r, int g, int b) { this.r = r; this.g = g; this.b = b; }

    public float getRed() { return r / 255f; }
    public float getGreen() { return g / 255f; }
    public float getBlue() { return b / 255f; }

    public static VanillaColor decode(String hex) {
        // Strip leading # if present
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }

        if (hex.length() != 6) {
            throw new IllegalArgumentException("Invalid hex color: " + hex);
        }

        try {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);

            return new VanillaColor(r, g, b);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex color format: " + hex, e);
        }
    }

    public int getRedInt() {
        return r;
    }

    public int getGreenInt() {
        return g;
    }

    public int getBlueInt() {
        return b;
    }

    public int toNativeInt() {
        int r = this.r & 0xFF;
        int g = this.g & 0xFF;
        int b = this.b & 0xFF;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public String toHex() {
        return String.format("#%02X%02X%02X", r, g, b);
    }
}

