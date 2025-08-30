package org.vicky.forge.forgeplatform.forgeplatform;

import org.vicky.forge.forgeplatform.forgeplatform.useables.ForgeIColor;
import org.vicky.platform.PlatformChatFormatter;

public class ForgeChatFormatter implements PlatformChatFormatter {

    @Override
    public String hex(String hex) {
        StringBuilder out = new StringBuilder("§x");
        for (char c : hex.substring(1).toCharArray()) {
            out.append("§").append(c);
        }
        return out.toString();
    }

    @Override
    public String gradient(String text, String fromHex, String toHex) {
        StringBuilder gradientText = new StringBuilder();
        ForgeIColor start = ForgeIColor.decode(fromHex);
        ForgeIColor end = ForgeIColor.decode(toHex);

        int len = text.length();
        for (int i = 0; i < len; i++) {
            float ratio = (float) i / (len - 1);
            int r = (int) (start.getRed() + ratio * (end.getRed() - start.getRed()));
            int g = (int) (start.getGreen() + ratio * (end.getGreen() - start.getGreen()));
            int b = (int) (start.getBlue() + ratio * (end.getBlue() - start.getBlue()));

            String hex = String.format("#%02X%02X%02X", r, g, b);
            gradientText.append(this.hex(hex)).append(text.charAt(i));
        }

        return gradientText.toString();
    }
}
