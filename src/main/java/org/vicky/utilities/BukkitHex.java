/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;

/**
 * This is a helper class for making strings enriched with Bukkit color codes (including hex support).
 */
@SuppressWarnings({"deprecation"})
public class BukkitHex {
  // Reset
  public static final String RESET = ChatColor.RESET.toString();

  // Regular Colors
  public static final String BLACK = ChatColor.BLACK.toString();
  public static final String RED = ChatColor.RED.toString();
  public static final String ORANGE = HexGenerator.of("#FFA500");
  public static final String PINK = HexGenerator.of("#FFC0CB");
  public static final String GREEN = ChatColor.GREEN.toString();
  public static final String YELLOW = ChatColor.YELLOW.toString();
  public static final String BLUE = ChatColor.BLUE.toString();
  public static final String PURPLE = ChatColor.DARK_PURPLE.toString();
  public static final String CYAN = ChatColor.AQUA.toString();
  public static final String WHITE = ChatColor.WHITE.toString();

  // Bold Colors
  public static final String BLACK_BOLD = ChatColor.BOLD.toString() + ChatColor.BLACK;
  public static final String RED_BOLD = ChatColor.BOLD.toString() + ChatColor.RED;
  public static final String ORANGE_BOLD = ChatColor.BOLD + HexGenerator.of("#FFA500");
  public static final String PINK_BOLD = ChatColor.BOLD + HexGenerator.of("#FFC0CB");
  public static final String GREEN_BOLD = ChatColor.BOLD + ChatColor.GREEN.toString();
  public static final String YELLOW_BOLD = ChatColor.BOLD.toString() + ChatColor.YELLOW;
  public static final String BLUE_BOLD = ChatColor.BOLD.toString() + ChatColor.BLUE;
  public static final String PURPLE_BOLD = ChatColor.BOLD.toString() + ChatColor.DARK_PURPLE;
  public static final String CYAN_BOLD = ChatColor.BOLD.toString() + ChatColor.AQUA;
  public static final String WHITE_BOLD = ChatColor.BOLD.toString() + ChatColor.WHITE;

  // High Intensity Colors (Bright)
  public static final String DARK_GRAY = ChatColor.DARK_GRAY.toString();
  public static final String LIGHT_RED = HexGenerator.of("#FF5555");
  public static final String LIGHT_GREEN = HexGenerator.of("#55FF55");
  public static final String LIGHT_YELLOW = HexGenerator.of("#FFFF55");
  public static final String LIGHT_BLUE = HexGenerator.of("#5555FF");
  public static final String LIGHT_PURPLE = HexGenerator.of("#FF55FF");
  public static final String LIGHT_CYAN = HexGenerator.of("#55FFFF");
  public static final String LIGHT_GRAY = ChatColor.GRAY.toString();

  // STYLES
  public static final String BOLD = ChatColor.BOLD.toString();
  public static final String ITALIC = ChatColor.ITALIC.toString();
  public static final String BOLD_ITALIC = ChatColor.BOLD.toString() + ChatColor.ITALIC;
  public static final String UNDERLINE = ChatColor.UNDERLINE.toString();
  public static final String STRIKETHROUGH = ChatColor.STRIKETHROUGH.toString();

  private static final Map<String, String> COLOR_MAP = new HashMap<>();
  // Regex pattern to match color markers like red[...] or purple[...]
  private static final Pattern COLOR_PATTERN = Pattern.compile("([a-zA-Z_]+)\\[([^\\]]+)\\]");
  // Updated pattern for rainbow effect:
  // Format: rainbow[-<length>]-color1-color2-...-colorN[Text]
  private static final Pattern RAINBOW_PATTERN =
      Pattern.compile("^rainbow(?:-(\\d+))?((?:-[a-zA-Z0-9_]+)+)\\[([^\\]]+)\\]$");
  // Combined regex: matches either a rainbow marker or a simple marker like "red[...]"
  private static final Pattern MIXED_COLOR_PATTERN =
      Pattern.compile(
          "((?:rainbow(?:-[a-zA-Z0-9_]+)+)|"
              + "(?:gradient(?:-(?:[a-zA-Z]+))?-(?:#[A-Fa-f0-9]{6})-(?:#[A-Fa-f0-9]{6}))|"
              + "[a-zA-Z_]+)\\[([^\\]]+)\\]");

  static {
    COLOR_MAP.put("black", BLACK);
    COLOR_MAP.put("red", RED);
    COLOR_MAP.put("orange", ORANGE);
    COLOR_MAP.put("pink", PINK);
    COLOR_MAP.put("green", GREEN);
    COLOR_MAP.put("yellow", YELLOW);
    COLOR_MAP.put("blue", BLUE);
    COLOR_MAP.put("purple", PURPLE);
    COLOR_MAP.put("cyan", CYAN);
    COLOR_MAP.put("white", WHITE);
    COLOR_MAP.put("black_bold", BLACK_BOLD);
    COLOR_MAP.put("red_bold", RED_BOLD);
    COLOR_MAP.put("orange_bold", ORANGE_BOLD);
    COLOR_MAP.put("pink_bold", PINK_BOLD);
    COLOR_MAP.put("green_bold", GREEN_BOLD);
    COLOR_MAP.put("yellow_bold", YELLOW_BOLD);
    COLOR_MAP.put("blue_bold", BLUE_BOLD);
    COLOR_MAP.put("purple_bold", PURPLE_BOLD);
    COLOR_MAP.put("cyan_bold", CYAN_BOLD);
    COLOR_MAP.put("white_bold", WHITE_BOLD);
    COLOR_MAP.put("bright_gray", LIGHT_GRAY);
    COLOR_MAP.put("bright_red", LIGHT_RED);
    COLOR_MAP.put("bright_green", LIGHT_GREEN);
    COLOR_MAP.put("bright_yellow", LIGHT_YELLOW);
    COLOR_MAP.put("bright_blue", LIGHT_BLUE);
    COLOR_MAP.put("bright_purple", LIGHT_PURPLE);
    COLOR_MAP.put("bright_cyan", LIGHT_CYAN);
    COLOR_MAP.put("dark_gray", DARK_GRAY);
    COLOR_MAP.put("bold", BOLD);
    COLOR_MAP.put("bold_italic", BOLD_ITALIC);
    COLOR_MAP.put("italic", ITALIC);
    COLOR_MAP.put("underline", UNDERLINE);
    COLOR_MAP.put("strikethrough", STRIKETHROUGH);
  }

  /**
   * @deprecated Use {@link #colorize(String)} instead.
   */
  @Deprecated
  public static String colorize(String message, String color) {
    String colorCode = COLOR_MAP.getOrDefault(color.toLowerCase(), RESET);
    return colorCode + message + RESET;
  }

  /**
   * Processes a string containing color markers (e.g. red[Hello]) and replaces them with Bukkit color codes.
   *
   * @param message The input string with markers.
   * @return The string with Bukkit color codes applied.
   */
  public static String colorize(String message) {
    while (true) {
      Matcher matcher = COLOR_PATTERN.matcher(message);
      if (!matcher.find()) {
        break;
      }
      String color = matcher.group(1).toLowerCase();
      String text = matcher.group(2);
      String processedText = colorize(text);
      String colorCode = COLOR_MAP.getOrDefault(color, RESET);
      message = matcher.replaceFirst(Matcher.quoteReplacement(colorCode + processedText + RESET));
    }
    return message;
  }

  /**
   * Applies a rainbow effect to the text inside a marker.
   * Format: rainbow[-<length>]-color1-color2-...-colorN[Text]
   *
   * @param message The input string in rainbow format.
   * @return The string with a rainbow effect using Bukkit color codes.
   */
  public static String rainbowColorize(String message) {
    Matcher matcher = RAINBOW_PATTERN.matcher(message);
    if (!matcher.matches()) {
      return message;
    }
    String lengthStr = matcher.group(1);
    String colorPart = matcher.group(2);
    String text = matcher.group(3);

    // Remove the leading dash and split colors.
    String[] colorNames = colorPart.substring(1).split("-");
    List<String> colorCodes = new ArrayList<>();
    for (String name : colorNames) {
      String code = COLOR_MAP.getOrDefault(name.toLowerCase(), RESET);
      colorCodes.add(code);
    }

    int segLength;
    if (lengthStr != null) {
      segLength = Integer.parseInt(lengthStr);
    } else {
      segLength = (int) Math.ceil((double) text.length() / colorCodes.size());
    }

    StringBuilder sb = new StringBuilder();
    // Process the text in segments of segLength characters.
    for (int i = 0; i < text.length(); i += segLength) {
      int end = Math.min(i + segLength, text.length());
      String segment = text.substring(i, end);
      String code = colorCodes.get((i / segLength) % colorCodes.size());
      sb.append(code).append(segment);
    }
    sb.append(RESET);
    return sb.toString();
  }

  /**
   * Processes a string that may contain standard color markers, rainbow markers, or gradient markers.
   *
   * Supports:
   *   - "red[Hello]"
   *   - "rainbow-blue-green[World]"
   *   - "gradient-#FF0000-#00FF00[Gradient Text]" or "gradient-center-#FF0000-#00FF00[Gradient Text]"
   *
   * @param message The input string with markers.
   * @return The ANSI-enriched string.
   */
  public static String colorizeMixed(String message) {
    while (true) {
      Matcher matcher = MIXED_COLOR_PATTERN.matcher(message);
      if (!matcher.find()) {
        break;
      }

      String marker =
          matcher.group(1).toLowerCase(); // e.g., "red", "rainbow-blue-green", "gradient-..."
      String text = matcher.group(2);
      String replacement;

      if (marker.startsWith("rainbow")) {
        replacement = rainbowColorize(matcher.group(0));
      } else if (marker.startsWith("gradient")) {
        // Marker formats supported:\n"
        // "gradient-#FF0000-#00FF00" (default alignment 'left')\n"
        // "gradient-center-#FF0000-#00FF00"\n
        String[] parts = marker.split("-");
        String alignment;
        String startColor;
        String endColor;
        if (parts.length == 3) {
          alignment = "left"; // default alignment\n"
          startColor = parts[1];
          endColor = parts[2];
        } else if (parts.length == 4) {
          alignment = parts[1];
          startColor = parts[2];
          endColor = parts[3];
        } else {
          replacement = text; // Fallback if format is invalid\n"
          continue;
        }
        replacement = gradientColorize(text, alignment, startColor, endColor);
      } else {
        String colorCode = COLOR_MAP.getOrDefault(marker, RESET);
        replacement = colorCode + colorize(text) + RESET;
      }

      message = matcher.replaceFirst(Matcher.quoteReplacement(replacement));
    }
    return message;
  }

  /**
   * Applies a gradient effect to a given text.
   * If the text is multi-line (contains newline characters) and the alignment is \"top\" or \"bottom\",\n
   * the gradient is applied vertically; otherwise, it is applied horizontally (line by line).
   *
   * @param text the text to gradient-colorize
   * @param alignment the alignment for the gradient (e.g. \"left\", \"right\", \"center\", \"top\", \"bottom\")
   * @param startColor the starting hex color (e.g. \"#FF0000\")
   * @param endColor the ending hex color (e.g. \"#00FF00\")
   * @return the text with a gradient applied using ANSI true-color escape sequences
   */
  private static String gradientColorize(
      String text, String alignment, String startColor, String endColor) {
    if (text.contains("\n")) {
      if (alignment.equals("top") || alignment.equals("bottom")) {
        return gradientVertical(text, alignment, startColor, endColor);
      } else {
        return Arrays.stream(text.split("\n"))
            .map(line -> gradientHorizontal(line, alignment, startColor, endColor))
            .collect(Collectors.joining("\n"));
      }
    } else {
      return gradientHorizontal(text, alignment, startColor, endColor);
    }
  }

  /**
   * Removes all Bukkit color codes from the input string.
   *
   * @param input The string to be cleaned.
   * @return The string without any Bukkit color codes.
   */
  public static String removeColorCodes(String input) {
    return ChatColor.stripColor(input);
  }

  /**
   * Adds a new color to the color map with the corresponding hex code.
   *
   * @param colorName The name of the new color (e.g., "customBlue").
   * @param hexCode   The hex code (e.g., "#1E90FF") for the color.
   */
  public void addCustomColor(String colorName, String hexCode) {
    COLOR_MAP.put(colorName.toLowerCase(), HexGenerator.of(hexCode));
  }

  /**
   * Applies a gradient effect to text based on a marker.
   * Expected format:
   *   gradient-{alignment}-{startColor}-{endColor}[Text to colorize]
   *
   * Alignment options:
   *   left, right, center (for horizontal gradient) and
   *   top, bottom (for vertical gradient on multi–line text)
   *
   * @param message the gradient marker and text
   * @return the Bukkit enriched string with a gradient effect
   */
  public static String gradientColorize(String message) {
    if (!message.startsWith("gradient-")) {
      return colorize(message);
    }
    int openBracket = message.indexOf('[');
    if (openBracket < 0) return message;
    String marker = message.substring(0, openBracket);
    String text = message.substring(openBracket + 1, message.lastIndexOf(']'));
    String[] parts = marker.split("-");
    if (parts.length < 4) return message;
    // parts[0] is "gradient"
    String alignment = parts[1].toLowerCase();
    String startColor = parts[2];
    String endColor = parts[3];

    if (text.contains("\n")) {
      if (alignment.equals("top") || alignment.equals("bottom")) {
        return gradientVertical(text, alignment, startColor, endColor);
      } else {
        // Horizontal gradient applied on each line using stream map/collect
        return Arrays.stream(text.split("\n"))
            .map(line -> gradientHorizontal(line, alignment, startColor, endColor))
            .collect(Collectors.joining("\n"));
      }
    } else {
      return gradientHorizontal(text, alignment, startColor, endColor);
    }
  }

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

  // Horizontal gradient: per-character interpolation.
  private static String gradientHorizontal(
      String text, String alignment, String startColor, String endColor) {
    Matcher matcher = COLOR_PATTERN.matcher(text);
    List<Pair<String, Boolean>> parts = new ArrayList<>();

    int lastIndex = 0;
    while (matcher.find()) {
      if (matcher.start() > lastIndex) {
        // Add normal text (to be gradient-colored)
        parts.add(new Pair<>(text.substring(lastIndex, matcher.start()), true));
      }
      // Add the color marker itself (not gradient-colored)
      parts.add(new Pair<>(matcher.group(), false));
      lastIndex = matcher.end();
    }
    if (lastIndex < text.length()) {
      parts.add(new Pair<>(text.substring(lastIndex), true));
    }

    // Apply gradient only to actual text
    int textLength = parts.stream().filter(Pair::getValue).mapToInt(p -> p.getKey().length()).sum();
    if (textLength == 0) return text; // If no visible text, return as-is

    StringBuilder sb = new StringBuilder();
    int charIndex = 0;

    for (Pair<String, Boolean> part : parts) {
      if (!part.getValue()) {
        sb.append(part.getKey()); // Directly append color markers
      } else {
        String segment = part.getKey();
        for (char c : segment.toCharArray()) {
          double ratio =
              switch (alignment) {
                case "left" -> (double) charIndex / (textLength - 1);
                case "right" -> (double) (textLength - 1 - charIndex) / (textLength - 1);
                case "center" -> (double) charIndex / (textLength - 1);
                default -> (double) charIndex / (textLength - 1);
              };
          String hexColor = interpolateColor(startColor, endColor, ratio);
          String bukkitColor = HexGenerator.of(hexColor);
          sb.append(bukkitColor).append(c);
          charIndex++;
        }
      }
    }

    sb.append(RESET); // Reset at the end
    return sb.toString();
  }

  // Vertical gradient: one color per line.
  private static String gradientVertical(
      String text, String alignment, String startColor, String endColor) {
    String[] lines = text.split("\n");
    int total = lines.length;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < total; i++) {
      double ratio = (double) i / (total - 1);
      if (alignment.equals("bottom")) {
        ratio = 1 - ratio;
      }
      String hexColor = interpolateColor(startColor, endColor, ratio);
      String bukkitColor = HexGenerator.of(hexColor).toString();
      sb.append(bukkitColor).append(lines[i]).append(RESET);
      if (i < total - 1) {
        sb.append("\n");
      }
    }
    return sb.toString();
  }
}
