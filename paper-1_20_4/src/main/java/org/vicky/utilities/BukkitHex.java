/* Licensed under Apache-2.0 2024-2025. */
package org.vicky.utilities;

import org.bukkit.ChatColor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is a helper class for making strings enriched with Bukkit color codes
 * (including hex support) that actually output ANSI escape sequences.
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
    // Regex patterns
    // COLOR_PATTERN: matches a simple color marker, e.g., red[Hello] or
    // #AA0000[Hello]
    private static final Pattern COLOR_PATTERN = Pattern.compile("((?!#)[A-Za-z_]+|#[A-Fa-f0-9]{6})\\[([^\\]]+)\\]");
    // RAINBOW_PATTERN: matches markers like
    // rainbow[-<length>]-color1-color2-...-colorN[Text]
    private static final Pattern RAINBOW_PATTERN = Pattern
            .compile("^rainbow(?:-(\\d+))?((?:-(?:[A-Za-z0-9_]+|#[A-Fa-f0-9]{6}))+)\\[([^\\]]+)\\]$");
    // GRADIENT_PATTERN: matches markers like
    // gradient[-<alignment>]-#RRGGBB-#RRGGBB[Text]
    // and also supports angle-based markers, e.g.,
    // gradient-45deg-top-#RRGGBB-#RRGGBB-...-#RRGGBB[Text]
    private static final Pattern GRADIENT_PATTERN = Pattern.compile(
            "^gradient-(?:(\\d+)deg-)?(?:([a-zA-Z]+)-)?" + "((?:#[A-Fa-f0-9]{6}(?:-#[A-Fa-f0-9]{6})+))\\[([^\\]]+)\\]");

    /**
     * Combined regex: matches either a rainbow marker, a gradient marker, or a
     * simple color marker.
     * <p>
     * - Rainbow: rainbow[-<length>]-color1-color2-...-colorN[Text] - Gradient:
     * gradient-([angle]deg-)?([a-zA-Z]+-)?#RRGGBB(-#RRGGBB)+[Text] - Simple:
     * red[Text] or #AA0000[Text]
     * </p>
     */
    private static final Pattern MIXED_COLOR_PATTERN = Pattern.compile(
            // Rainbow marker: group(1)=rainbow marker, group(2)=text inside brackets
            "(rainbow(?:-(?:[A-Za-z0-9_]+|#[A-Fa-f0-9]{6}))+)" + "\\[([^\\]]+)\\]" + "|" +
                    // Gradient marker: group(3)=gradient marker, group(4)=text inside brackets
                    "(gradient(?:-(?:\\d+deg))?(?:-[a-zA-Z]+)?-(?:#[A-Fa-f0-9]{6}(?:-#[A-Fa-f0-9]{6})+))"
                    + "\\[([^\\]]+)\\]" + "|" +
                    // Simple color marker: group(5)=simple marker, group(6)=text inside brackets
                    "((?!#)[A-Za-z_]+|#[A-Fa-f0-9]{6})" + "\\[([^\\]]+)\\]");

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
     * Processes a string containing simple color markers (e.g. red[Hello] or
     * #AA0000[Hello]) and replaces them with ANSI color codes.
     *
     * @param message The input string with markers.
     * @return The string with color codes applied.
     */
    public static String colorize(String message) {
        Matcher matcher = COLOR_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String marker = matcher.group(1);
            String text = matcher.group(2);
            String processedText = colorize(text); // recursive processing.
            String colorCode = marker.startsWith("#")
                    ? HexGenerator.of(marker)
                    : COLOR_MAP.getOrDefault(marker.toLowerCase(), RESET);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(colorCode + processedText + RESET));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Applies a rainbow effect to text contained in a rainbow marker. Expected
     * format: rainbow[-{length}]-color1-color2-...-colorN[Text]
     *
     * @param message The full rainbow marker string.
     * @return The string with a rainbow effect applied.
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
            String code = name.startsWith("#")
                    ? HexGenerator.of(name)
                    : COLOR_MAP.getOrDefault(name.toLowerCase(), RESET);
            colorCodes.add(code);
        }
        int segLength = lengthStr != null
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
     * Applies a gradient effect to text based on a gradient marker. Supported
     * formats: gradient-#FF0000-#00FF00[Text] (default alignment "left")
     * gradient-center-#FF0000-#00FF00[Text]
     * gradient-45deg-top-#FF0000-#00FF00-#0000FF[Line1\nLine2\nLine3]
     *
     * @param message The full gradient marker string.
     * @return The string with a gradient applied.
     */
    public static String gradientColorize(String message) {
        if (!message.startsWith("gradient-")) {
            return colorize(message);
        }
        Matcher matcher = GRADIENT_PATTERN.matcher(message);
        if (!matcher.matches()) {
            return message;
        }
        // Check if angle is provided.
        String angleStr = matcher.group(1);
        String align = matcher.group(2); // alignment, if provided.
        String colorsStr = matcher.group(3);
        String text = matcher.group(4);

        // If angle is provided, use the manual multi-line method.
        if (angleStr != null) {
            return gradientColorizeAngleMultiLine(message);
        } else {
            // Fallback: use a horizontal gradient.
            String alignment = (align != null) ? align.toLowerCase() : "left";
            // Split colors (only two expected in fallback)
            String[] parts = colorsStr.split("-");
            if (parts.length < 2)
                return message;
            String startColor = parts[0];
            String endColor = parts[1];
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
     * Applies a gradient effect to multi-line text based on a gradient marker with
     * angle and direction. Expected format:
     * gradient-[angle]deg-[direction]-#RRGGBB(-#RRGGBB)*[Text]
     *
     * @param marker The full gradient marker string.
     * @return The multi-line text with the gradient applied using ANSI escape
     * codes.
     */
    public static String gradientColorizeAngleMultiLine(String marker) {
        Pattern pattern = Pattern
                .compile("^gradient-(\\d+)deg-([a-zA-Z]+)-((?:#[A-Fa-f0-9]{6}(?:-#[A-Fa-f0-9]{6})*))\\[([^\\]]+)\\]$");
        Matcher matcher = pattern.matcher(marker);
        if (!matcher.matches()) {
            return marker;
        }
        int angle = Integer.parseInt(matcher.group(1));
        String direction = matcher.group(2).toLowerCase();
        String colorsStr = matcher.group(3);
        String text = matcher.group(4);

        List<String> colors = Arrays.asList(colorsStr.split("-"));
        if (colors.size() < 2)
            return marker;

        String[] lines = text.split("\n");
        int totalLines = lines.length;
        if (totalLines == 0)
            return "";

        double rawFactor = angle / 90.0;
        double factor = Math.max(0.5, Math.min(rawFactor, 2.0));

        // Branch based on direction:
        if (direction.equals("top") || direction.equals("bottom")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < totalLines; i++) {
                double ratio = (double) i / (totalLines - 1);
                double adjusted = direction.equals("top") ? Math.pow(ratio, factor) : 1 - Math.pow(1 - ratio, factor);
                int segments = colors.size() - 1;
                double segmentLength = 1.0 / segments;
                int segmentIndex = (int) Math.min(Math.floor(adjusted / segmentLength), segments - 1);
                double localRatio = (adjusted - (segmentIndex * segmentLength)) / segmentLength;
                String startColor = colors.get(segmentIndex);
                String endColor = colors.get(segmentIndex + 1);
                String interpHex = interpolateColor(startColor, endColor, localRatio);
                String ansiColor = HexGenerator.of(interpHex);
                sb.append(ansiColor).append(lines[i]).append(RESET);
                if (i < totalLines - 1)
                    sb.append("\n");
            }
            return sb.toString();
        } else if (direction.equals("left") || direction.equals("right")) {
            return Arrays.stream(lines).map(line -> gradientHorizontalMulti(line, direction, colors, factor))
                    .collect(Collectors.joining("\n"));
        } else {
            return text;
        }
    }

    /**
     * Applies a horizontal gradient effect to a single line of text based on
     * multiple color stops.
     *
     * @param text      The text line to colorize.
     * @param direction The direction: "left" means gradient from left (first color) to
     *                  right (last color), "right" means reversed.
     * @param colors    The list of hex color stops.
     * @param factor    The non-linear factor derived from the angle.
     * @return The line with an ANSI horizontal gradient applied.
     */
    private static String gradientHorizontalMulti(String text, String direction, List<String> colors, double factor) {
        int length = text.length();
        StringBuilder sb = new StringBuilder();
        int segments = colors.size() - 1;
        double segmentLength = 1.0 / segments;
        for (int i = 0; i < length; i++) {
            double baseRatio = (double) i / (length - 1);
            double ratio = switch (direction) {
                case "left" -> Math.pow(baseRatio, factor);
                case "right" -> 1 - Math.pow(1 - baseRatio, factor);
                default -> baseRatio;
            };
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
     * Applies a horizontal gradient effect to text (per-character interpolation).
     *
     * @param text       The text to gradient-colorize.
     * @param alignment  The alignment ("left", "right", "center") determining the
     *                   interpolation.
     * @param startColor The starting hex color (e.g. "#FF0000").
     * @param endColor   The ending hex color (e.g. "#00FF00").
     * @return The text with a horizontal gradient applied using ANSI escape codes.
     */
    private static String gradientHorizontal(String text, String alignment, String startColor, String endColor) {
        int length = text.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            double ratio;
            switch (alignment) {
                case "left":
                    ratio = (double) i / (length - 1);
                    break;
                case "right":
                    ratio = (double) (length - 1 - i) / (length - 1);
                    break;
                case "center":
                    ratio = (double) i / (length - 1);
                    break;
                default:
                    ratio = (double) i / (length - 1);
            }
            String hexColor = interpolateColor(startColor, endColor, ratio);
            String ansiColor = HexGenerator.of(hexColor);
            sb.append(ansiColor).append(text.charAt(i));
        }
        sb.append(RESET);
        return sb.toString();
    }

    /**
     * Processes a string containing mixed color markers and replaces them with ANSI
     * escape sequences. Supported marker formats include:
     * <ul>
     * <li>Rainbow: <code>rainbow-blue-green[Text]</code></li>
     * <li>Gradient: <code>gradient-center-#FF0000-#00FF00[Text]</code> or
     * <code>gradient-45deg-top-#FF0000-#00FF00-#0000FF[Multi\nLine]</code></li>
     * <li>Simple: <code>red[Text]</code> or <code>#AA0000[Text]</code></li>
     * </ul>
     *
     * @param message The input string with markers.
     * @return The string with ANSI escape sequences applied.
     */
    public static String colorizeMixed(String message) {
        Matcher matcher = MIXED_COLOR_PATTERN.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacement;
            // Check rainbow marker (group 1 is non-null)
            if (matcher.group(1) != null) {
                replacement = rainbowColorize(matcher.group(0));
            }
            // Else check gradient marker (group 3 is non-null)
            else if (matcher.group(3) != null) {
                replacement = gradientColorize(matcher.group(0));
            }
            // Else, it's a simple color marker (group 5)
            else if (matcher.group(5) != null) {
                String marker = matcher.group(5).toLowerCase();
                String text = matcher.group(6);
                String colorCode = marker.startsWith("#")
                        ? HexGenerator.of(marker)
                        : COLOR_MAP.getOrDefault(marker, RESET);
                replacement = colorCode + colorize(text) + RESET;
            } else {
                replacement = matcher.group(0);
            }
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Interpolates between two hex color codes based on the given ratio.
     *
     * @param startColor The starting hex color (e.g., "#FF0000").
     * @param endColor   The ending hex color (e.g., "#00FF00").
     * @param ratio      A value between 0.0 (startColor) and 1.0 (endColor).
     * @return The interpolated hex color string.
     */
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
