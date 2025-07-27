package org.vicky.platform.defaults;

import net.kyori.adventure.text.format.TextColor;
import org.vicky.platform.IColor;

public enum BossBarColor {
    PINK(TextColor.color(0xFF55FF)),
    BLUE(TextColor.color(0x5555FF)),
    RED(TextColor.color(0xFF5555)),
    GREEN(TextColor.color(0x55FF55)),
    YELLOW(TextColor.color(0xFFFF55)),
    PURPLE(TextColor.color(0xAA00AA)),
    WHITE(TextColor.color(0xFFFFFF));

    final TextColor color;
    BossBarColor(TextColor color) {
        this.color = color;
    }
}