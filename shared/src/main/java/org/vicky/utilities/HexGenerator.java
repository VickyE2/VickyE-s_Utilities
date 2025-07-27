/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import org.vicky.platform.PlatformPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexGenerator {

  public static final char COLOR_CHAR = '§';

  public static String getHexGradient(String text, String startColor, String endColor) {
    return PlatformPlugin.chatFormatter().gradient(text, startColor, endColor);
  }

  // Interpolates between two colors based on the ratio (0.0 = startColor, 1.0 = endColor)
  static String interpolateColor(String startColor, String endColor, double ratio) {
    int startR = Integer.parseInt(startColor.substring(1, 3), 16);
    int startG = Integer.parseInt(startColor.substring(3, 5), 16);
    int startB = Integer.parseInt(startColor.substring(5, 7), 16);

    int endR = Integer.parseInt(endColor.substring(1, 3), 16);
    int endG = Integer.parseInt(endColor.substring(3, 5), 16);
    int endB = Integer.parseInt(endColor.substring(5, 7), 16);

    int newR = (int) (startR + ratio * (endR - startR));
    int newG = (int) (startG + ratio * (endG - startG));
    int newB = (int) (startB + ratio * (endB - startB));

    return String.format("#%02X%02X%02X", newR, newG, newB);
  }

  /**
   * Converts ANSI escape sequences for RGB colors (e.g. "\u001B[38;2;R;G;Bm")
   * to the Bukkit hex format ("§x§R§R§G§G§B§B").
   *
   * @param message The message containing ANSI escape sequences.
   * @return The message with ANSI escape sequences replaced by Bukkit hex color codes.
   */
  public static String convertAnsiEscapeToBukkitHex(String message) {
    // Pattern to match ANSI escape sequences of the form: \u001B[38;2;R;G;B m
    Pattern pattern = Pattern.compile("\\u001B\\[38;2;(\\d+);(\\d+);(\\d+)m");
    Matcher matcher = pattern.matcher(message);
    StringBuffer sb = new StringBuffer();

    while (matcher.find()) {
      int r = Integer.parseInt(matcher.group(1));
      int g = Integer.parseInt(matcher.group(2));
      int b = Integer.parseInt(matcher.group(3));
      String hex = String.format("#%02X%02X%02X", r, g, b);
      String bukkitHex =
          COLOR_CHAR
              + "x"
              + COLOR_CHAR
              + hex.charAt(1)
              + COLOR_CHAR
              + hex.charAt(2)
              + COLOR_CHAR
              + hex.charAt(3)
              + COLOR_CHAR
              + hex.charAt(4)
              + COLOR_CHAR
              + hex.charAt(5)
              + COLOR_CHAR
              + hex.charAt(6);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(bukkitHex));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  /**
   * Converts a hex color string (e.g. "#1E90FF") to the corresponding Bukkit hex color code.
   *
   * @param hex the hex color string (must start with "#")
   * @return the Bukkit hex color code (e.g. "§x§1§E§9§0§F§F")
   */
  public static String of(String hex) {
    return PlatformPlugin.chatFormatter().hex(hex);
  }

  public static String stripHex(String message) {
    return message.replaceAll("§x(§[0-9a-fA-F]){6}", ""); // strips Bukkit hex
  }

}
