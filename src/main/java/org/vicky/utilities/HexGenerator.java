/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class HexGenerator {

  public static final char COLOR_CHAR = '§';

  public static String getHexGradient(String text, String startColor, String endColor) {
    // First, automatically translate any hex codes in the text
    text = translateHexColorCodes("<#", ">", text);

    StringBuilder gradientText = new StringBuilder();
    boolean inBrackets = false;
    int gradientLength = 0;

    // Count the number of characters excluding the brackets themselves
    for (char c : text.toCharArray()) {
      if (c != '[' && c != ']') {
        gradientLength++;
      }
    }

    int currentGradientIndex = 0;

    for (int i = 0; i < text.length(); i++) {
      char currentChar = text.charAt(i);

      if (currentChar == '[') {
        inBrackets = true;
        gradientText.append(ChatColor.RESET);
        gradientText.append(currentChar); // Keep the brackets in the final string
      } else if (currentChar == ']') {
        inBrackets = false;
        gradientText.append(ChatColor.RESET);
        gradientText.append(currentChar); // Keep the brackets in the final string
      } else {
        // Apply the gradient to all characters, including those inside the brackets
        // but skip the brackets themselves
        double ratio = (double) currentGradientIndex / (gradientLength - 1);
        String colorHex = interpolateColor(startColor, endColor, ratio);
        gradientText.append(ChatColor.of(colorHex));
        gradientText.append(currentChar);

        // Only increment the gradient index for characters outside of brackets
        if (!inBrackets) {
          currentGradientIndex++;
        }
      }
    }

    return gradientText.toString();
  }

  // Interpolates between two colors based on the ratio (0.0 = startColor, 1.0 = endColor)
  private static String interpolateColor(String startColor, String endColor, double ratio) {
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

  // Automatically translates hex color codes like <#xxxxxx> into Minecraft formatting
  public static String translateHexColorCodes(String startTag, String endTag, String message) {
    final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
    Matcher matcher = hexPattern.matcher(message);
    StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);

    while (matcher.find()) {
      String group = matcher.group(1);
      matcher.appendReplacement(
          buffer,
          COLOR_CHAR
              + "x"
              + COLOR_CHAR
              + group.charAt(0)
              + COLOR_CHAR
              + group.charAt(1)
              + COLOR_CHAR
              + group.charAt(2)
              + COLOR_CHAR
              + group.charAt(3)
              + COLOR_CHAR
              + group.charAt(4)
              + COLOR_CHAR
              + group.charAt(5));
    }
    return matcher.appendTail(buffer).toString();
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
    if (hex == null || !hex.matches("^#([A-Fa-f0-9]{6})$")) {
      throw new IllegalArgumentException("Invalid hex color: " + hex);
    }
    return COLOR_CHAR
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
  }
}
