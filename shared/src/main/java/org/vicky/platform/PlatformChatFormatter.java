package org.vicky.platform;

public interface PlatformChatFormatter {
    String hex(String hex);
    String gradient(String text, String fromHex, String toHex);
}
