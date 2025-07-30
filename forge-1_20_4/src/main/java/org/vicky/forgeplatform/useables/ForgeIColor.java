package org.vicky.forgeplatform.useables;

import org.vicky.platform.IColor;

public class ForgeIColor implements IColor {

    private final float red;
    private final float green;
    private final float blue;

    public ForgeIColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public float getRed() {
        return red;
    }

    @Override
    public float getGreen() {
        return green;
    }

    @Override
    public float getBlue() {
        return blue;
    }

    public static ForgeIColor decode(String hex) {
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

            return new ForgeIColor(r / 255.0f, g / 255.0f, b / 255.0f);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex color format: " + hex, e);
        }
    }

    public int toNativeInt() {
        int r = (int) (red * 255) & 0xFF;
        int g = (int) (green * 255) & 0xFF;
        int b = (int) (blue * 255) & 0xFF;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}