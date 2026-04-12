package org.vicky.platform.defaults;

import org.vicky.platform.IColor;

public class VanillaColor implements IColor {

    public static final VanillaColor RED =
            new VanillaColor(255, 0, 0);
    public static final VanillaColor ORANGE =
            new VanillaColor(190, 145, 0);
    public static final VanillaColor YELLOW =
            new VanillaColor(255, 255, 0);
    public static final VanillaColor GREEN =
            new VanillaColor(0, 255, 0);
    public static final VanillaColor BLUE =
            new VanillaColor(0, 0, 255);
    public static final VanillaColor PURPLE =
            new VanillaColor(0, 255, 255);
    public static final VanillaColor BLACK =
            new VanillaColor(0, 0, 0);
    public static final VanillaColor WHITE =
            new VanillaColor(255, 255, 255);

    private final int r, g, b, a;

    public VanillaColor(int r, int g, int b, int a) { this.r = r; this.g = g; this.b = b; this.a = a; }
    public VanillaColor(int r, int g, int b) { this(r, g, b, 255); }

    public VanillaColor(float r, float g, float b, float a) { this.r = (int) (255 * r); this.g = (int) (255 * g); this.b = (int) (255 * b); this.a = (int) (255 * a); }
    public VanillaColor(float r, float g, float b) { this(r, g, b, 1f); }

    public float getRed() { return r / 255f; }
    public float getGreen() { return g / 255f; }
    public float getBlue() { return b / 255f; }
    public float getAlpha() { return a / 255f; }

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

    public int getAlphaInt() {
        return a;
    }

    public int toNativeInt() {
        int a = this.a & 0xFF;
        int r = this.r & 0xFF;
        int g = this.g & 0xFF;
        int b = this.b & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public String toHex() {
        return String.format("#%02X%02X%02X%02X", a, r, g, b);
    }
}

