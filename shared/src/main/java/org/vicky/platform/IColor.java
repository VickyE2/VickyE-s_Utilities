package org.vicky.platform;

public interface IColor {
    float getRed();
    float getGreen();
    float getBlue();
    default float getAlpha() {
        return 1f;
    }

    String toHex();
}