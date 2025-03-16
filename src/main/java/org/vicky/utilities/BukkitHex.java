/* Licensed under Apache-2.0 2024. */
package org.vicky.utilities;

import static org.vicky.utilities.HexGenerator.interpolateColor;

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

  // Regular Colors (using Bukkit defaults or hex via HexGenerator)
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
  public static final String GREEN_BOLD = ChatColor.BOLD.toString() + ChatColor.GREEN;
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
  // COLOR_PATTERN: matches either a named color (first char not '#') or a hex color, followed by
  // text in brackets.
  private static final Pattern COLOR_PATTERN =
      Pattern.compile("((?!#)[A-Za-z_]+|#[A-Fa-f0-9]{6})\\[([^\\]]*)\\]");

  // RAINBOW_PATTERN: Format: rainbow[-<length>]-color1-color2-...-colorN[Text]
  private static final Pattern RAINBOW_PATTERN =
      Pattern.compile(
          "^rainbow(?:-(\\d+))?((?:-(?:[A-Za-z_]+|#[A-Fa-f0-9]{6}))+)" + "\\[([^\\]]*)\\]$");

  // GRADIENT marker: Format: gradient[-<alignment>]-#RRGGBB-#RRGGBB[Text]
  private static final Pattern GRADIENT_PATTERN =
      Pattern.compile(
          "^gradient(?:-([a-zA-Z]+))?-(#[A-Fa-f0-9]{6})-(#[A-Fa-f0-9]{6})\\[([^\\]]*)\\]$");

  /**
   * Combined regex: matches either a rainbow marker, a gradient marker, or a simple color marker.
   * <pre>
   * {@code
   * - Rainbow: rainbow[-<length>]-color1-color2-...-colorN[Text]
   * - Gradient: gradient[-<alignment>]-#RRGGBB-#RRGGBB[Text] | gradient-45deg-top-#FF0000-#00FF00-#0000FF[Line1\nLine2\nLine3]
   * - Simple: red[Text] or #AA0000[Text]
   * }
   * </pre>
   */
  private static final Pattern MIXED_COLOR_PATTERN =
      Pattern.compile(
          "((?:rainbow(?:-[a-zA-Z0-9_]+)+)|"
              + "(?:gradient(?:-(?:[a-zA-Z]+))?-(?:#[A-Fa-f0-9]{6})-(?:#[A-Fa-f0-9]{6})|"
              + "gradient-\\d+deg-[a-zA-Z]+-(?:#[A-Fa-f0-9]{6}(?:-#[A-Fa-f0-9]{6})+))|"
              + "(?:[a-zA-Z_]+|#[A-Fa-f0-9]{6}))\\[([^\\]]+)\\]");

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
   * Processes a string containing simple color markers (e.g. red[Hello] or #AA0000[Hello]) and replaces
   * them with Bukkit color codes.
   *
   * @param message The input string with markers.
   * @return The string with color codes applied.
   */
  public static String colorize(String message) {
    Matcher matcher = COLOR_PATTERN.matcher(message);
    while (matcher.find()) {
      String marker = matcher.group(1);
      String text = matcher.group(2);
      String processedText = colorize(text); // Recursive processing.
      String colorCode;
      if (marker.startsWith("#")) {
        // If it's a hex code, use our HexGenerator.
        colorCode = HexGenerator.convertAnsiEscapeToBukkitHex(marker);
      } else {
        colorCode = COLOR_MAP.getOrDefault(marker.toLowerCase(), RESET);
      }
      message = matcher.replaceFirst(Matcher.quoteReplacement(colorCode + processedText + RESET));
      matcher = COLOR_PATTERN.matcher(message);
    }
    return message;
  }

  /**
   * Applies a rainbow effect to the text in a rainbow marker.
   * Expected format: rainbow[-<length>]-color1-color2-...-colorN[Text]
   *
   * @param message The full rainbow marker string.
   * @return The string with a rainbow effect.
   */
  public static String rainbowColorize(String message) {
    Matcher matcher = RAINBOW_PATTERN.matcher(message);
    if (!matcher.matches()) {
      return message;
    }
    String lengthStr = matcher.group(1);
    String colorPart = matcher.group(2);
    String text = matcher.group(3);

    // Remove leading dash and split colors.
    String[] colorNames = colorPart.substring(1).split("-");
    List<String> colorCodes = new ArrayList<>();
    for (String name : colorNames) {
      String code =
          name.startsWith("#")
              ? HexGenerator.convertAnsiEscapeToBukkitHex(name)
              : COLOR_MAP.getOrDefault(name.toLowerCase(), RESET);
      colorCodes.add(code);
    }
    int segLength =
        lengthStr != null
            ? Integer.parseInt(lengthStr)
            : (int) Math.ceil((double) text.length() / colorCodes.size());

    StringBuilder sb = new StringBuilder();
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
   * Applies a gradient effect to text based on a gradient marker.
   * Supported formats:
   *   gradient-#FF0000-#00FF00[Text] (default alignment "left")
   *   gradient-center-#FF0000-#00FF00[Text]
   *   gradient-45deg-top-#FF0000-#00FF00-#0000FF[Line1\nLine2\nLine3]
   *
   * @param message The full gradient marker string.
   * @return The string with a gradient applied.
   */
  public static String gradientColorize(String message) {
    if (!message.startsWith("gradient-")) {
      return colorize(message);
    }
    // If the marker contains "deg", use the manual (angle-based) method.
    if (message.contains("deg")) {
      return gradientColorizeAngleMultiLine(message);
    } else {
      // Fallback: assume format "gradient-color1-color2[Text]"
      int openBracket = message.indexOf('[');
      if (openBracket < 0) return message;
      String marker = message.substring(0, openBracket);
      String text = message.substring(openBracket + 1, message.lastIndexOf(']'));
      String[] parts = marker.split("-");
      if (parts.length < 3) return message;
      String alignment = "left"; // default alignment
      String startColor = parts[1];
      String endColor = parts[2];
      if (text.contains("\n")) {
        return Arrays.stream(text.split("\n"))
            .map(line -> gradientHorizontal(line, alignment, startColor, endColor))
            .collect(Collectors.joining("\n"));
      } else {
        return gradientHorizontal(text, alignment, startColor, endColor);
      }
    }
  }

  /**
   * Applies a gradient effect to multi-line text based on a marker of the format:
   * <pre>
   * gradient-[angle]deg-[direction]-color1-color2-...-colorN[Text]
   * </pre>
   * The angle (in degrees) is used to adjust the interpolation non-linearly.
   * The direction can be:
   * <ul>
   *   <li>"top" (vertical gradient; top line uses first color, bottom uses last)</li>
   *   <li>"bottom" (vertical gradient reversed)</li>
   *   <li>"left" (horizontal gradient on each line; leftmost character uses first color)</li>
   *   <li>"right" (horizontal gradient reversed)</li>
   * </ul>
   *
   * @param marker The full gradient marker string.
   * @return The multi-line text with the gradient applied using ANSI escape codes.
   */
  public static String gradientColorizeAngleMultiLine(String marker) {
    // Expected marker format: gradient-[angle]deg-[direction]-#RRGGBB(-#RRGGBB)*[Text]
    Pattern pattern =
        Pattern.compile(
            "^gradient-(\\d+)deg-([a-zA-Z]+)-((?:#[A-Fa-f0-9]{6}(?:-#[A-Fa-f0-9]{6})*))\\[([^\\]]*)\\]$");
    Matcher matcher = pattern.matcher(marker);
    if (!matcher.matches()) {
      return marker; // Unchanged if it doesn't match expected format.
    }

    int angle = Integer.parseInt(matcher.group(1));
    String direction = matcher.group(2).toLowerCase(); // "top", "bottom", "left", or "right"
    String colorsStr = matcher.group(3); // e.g. "#FF0000-#00FF00-#0000FF"
    String text = matcher.group(4);

    // Split colors
    List<String> colors = Arrays.asList(colorsStr.split("-"));
    if (colors.size() < 2) {
      return marker; // Need at least two colors for a gradient.
    }

    // Split text into lines.
    String[] lines = text.split("\n");
    int totalLines = lines.length;
    if (totalLines == 0) return "";

    // Normalize the angle to a factor: 90° means linear (factor=1.0).
    double rawFactor = angle / 90.0;
    double factor = Math.max(0.5, Math.min(rawFactor, 2.0)); // Clamp factor between 0.5 and 2.0.

    // Branch based on direction:
    if (direction.equals("top") || direction.equals("bottom")) {
      // Vertical gradient: each line's color is interpolated based on its line index.
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < totalLines; i++) {
        double ratio = (double) i / (totalLines - 1);
        double adjusted =
            direction.equals("top") ? Math.pow(ratio, factor) : 1 - Math.pow(1 - ratio, factor);
        int segments = colors.size() - 1;
        double segmentLength = 1.0 / segments;
        int segmentIndex = (int) Math.min(Math.floor(adjusted / segmentLength), segments - 1);
        double localRatio = (adjusted - (segmentIndex * segmentLength)) / segmentLength;
        String startColor = colors.get(segmentIndex);
        String endColor = colors.get(segmentIndex + 1);
        String interpHex = interpolateColor(startColor, endColor, localRatio);
        String ansiColor = HexGenerator.of(interpHex);
        sb.append(ansiColor).append(lines[i]).append(RESET);
        if (i < totalLines - 1) {
          sb.append("\n");
        }
      }
      return sb.toString();
    } else if (direction.equals("left") || direction.equals("right")) {
      // Horizontal gradient: apply to each line separately.
      return Arrays.stream(lines)
          .map(line -> gradientHorizontalMulti(line, direction, colors, factor))
          .collect(Collectors.joining("\n"));
    } else {
      // Fallback: return unmodified text if direction is not recognized.
      return text;
    }
  }

  /**
   * Applies a horizontal gradient effect to a single line of text based on multiple color stops.
   *
   * @param text      The text line to colorize.
   * @param direction The direction: "left" means gradient from left (first color) to right (last color),
   *                  "right" means reversed.
   * @param colors    The list of hex color stops.
   * @param factor    The non-linear factor derived from the angle.
   * @return The line with an ANSI horizontal gradient applied.
   */
  private static String gradientHorizontalMulti(
      String text, String direction, List<String> colors, double factor) {
    int length = text.length();
    StringBuilder sb = new StringBuilder();
    int segments = colors.size() - 1;
    double segmentLength = 1.0 / segments;
    for (int i = 0; i < length; i++) {
      double baseRatio = (double) i / (length - 1);
      double ratio;
      switch (direction) {
        case "left":
          ratio = Math.pow(baseRatio, factor);
          break;
        case "right":
          ratio = 1 - Math.pow(1 - baseRatio, factor);
          break;
        default:
          ratio = baseRatio;
      }
      int segmentIndex = (int) Math.min(Math.floor(ratio / segmentLength), segments - 1);
      double localRatio = (ratio - (segmentIndex * segmentLength)) / segmentLength;
      String startColor = colors.get(segmentIndex);
      String endColor = colors.get(segmentIndex + 1);
      String interpHex = interpolateColor(startColor, endColor, localRatio);
      String ansiColor = HexGenerator.of(interpHex);
      sb.append(ansiColor).append(text.charAt(i));
    }
    sb.append(RESET);
    return sb.toString();
  }

  /**
   * Processes a string that may contain rainbow, gradient, or simple color markers.
   *
   * @param message The input string with markers.
   * @return The string with Bukkit color codes applied.
   */
  public static String colorizeMixed(String message) {
    Matcher matcher = MIXED_COLOR_PATTERN.matcher(message);
    while (matcher.find()) {
      String replacement;
      if (matcher.group(1) != null) { // Rainbow alternative (group 1 = full rainbow marker)
        replacement = rainbowColorize(matcher.group(0));
      } else if (matcher.group(5)
          != null) { // Gradient alternative (group 5 = full gradient marker)
        // Use the gradientColorize method (it expects the entire marker string)
        replacement = gradientColorize(matcher.group(0));
      } else if (matcher.group(10)
          != null) { // Simple color alternative (group 10 = color, group 11 = text)
        String marker = matcher.group(10);
        String text = matcher.group(11);
        String colorCode =
            marker.startsWith("#")
                ? HexGenerator.convertAnsiEscapeToBukkitHex(marker)
                : COLOR_MAP.getOrDefault(marker.toLowerCase(), RESET);
        replacement = colorCode + colorize(text) + RESET;
      } else {
        replacement = matcher.group(0);
      }
      message = matcher.replaceFirst(Matcher.quoteReplacement(replacement));
      matcher = MIXED_COLOR_PATTERN.matcher(message);
    }
    return message;
  }

  // Horizontal gradient: per-character interpolation.
  private static String gradientHorizontal(
      String text, String alignment, String startColor, String endColor) {
    int length = text.length();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      double ratio =
          switch (alignment) {
            case "left" -> (double) i / (length - 1);
            case "right" -> (double) (length - 1 - i) / (length - 1);
            case "center" -> (double) i / (length - 1);
            default -> (double) i / (length - 1);
          };
      String hexColor = interpolateColor(startColor, endColor, ratio);
      String bukkitColor = HexGenerator.of(hexColor);
      sb.append(bukkitColor).append(text.charAt(i));
    }
    sb.append(RESET);
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
      String bukkitColor = HexGenerator.of(hexColor);
      sb.append(bukkitColor).append(lines[i]).append(RESET);
      if (i < total - 1) {
        sb.append("\n");
      }
    }
    return sb.toString();
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
}
