/* Licensed under Apache-2.0 2024. */
package org.v_utls.utilities;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ANSIColor {
  // Reset
  public static final String RESET = "\033[0m"; // Text Reset

  // Regular Colors
  public static final String BLACK = "\033[0;30m"; // BLACK
  public static final String RED = "\033[0;31m"; // RED
  public static final String GREEN = "\033[0;32m"; // GREEN
  public static final String YELLOW = "\033[0;33m"; // YELLOW
  public static final String BLUE = "\033[0;34m"; // BLUE
  public static final String PURPLE = "\033[0;35m"; // PURPLE
  public static final String CYAN = "\033[0;36m"; // CYAN
  public static final String WHITE = "\033[0;37m"; // WHITE

  // Bold Colors
  public static final String BLACK_BOLD = "\033[1;30m"; // BLACK
  public static final String RED_BOLD = "\033[1;31m"; // RED
  public static final String GREEN_BOLD = "\033[1;32m"; // GREEN
  public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
  public static final String BLUE_BOLD = "\033[1;34m"; // BLUE
  public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
  public static final String CYAN_BOLD = "\033[1;36m"; // CYAN
  public static final String WHITE_BOLD = "\033[1;37m"; // WHITE

  // High Intensity Colors (Bright)
  public static final String DARK_GRAY = "\033[0;90m"; // DARK GRAY
  public static final String LIGHT_RED = "\033[0;91m"; // LIGHT RED
  public static final String LIGHT_GREEN = "\033[0;92m"; // LIGHT GREEN
  public static final String LIGHT_YELLOW = "\033[0;93m"; // LIGHT YELLOW
  public static final String LIGHT_BLUE = "\033[0;94m"; // LIGHT BLUE
  public static final String LIGHT_PURPLE = "\033[0;95m"; // LIGHT PURPLE
  public static final String LIGHT_CYAN = "\033[0;96m"; // LIGHT CYAN
  public static final String LIGHT_GRAY = "\033[0;97m"; // LIGHT GRAY

  //STYLES
  public static final String BOLD = "\033[1m";
  public static final String ITALIC = "\033[3m";
  public static final String BOLD_ITALIC = "\033[1m\033[3m";
  public static final String UNDERLINE = "\033[4m";
  public static final String STRIKETHROUGH = "\033[9m" ;

  private static final Map<String, String> COLOR_MAP = new HashMap<>();

  static {
    COLOR_MAP.put("black", BLACK);
    COLOR_MAP.put("red", RED);
    COLOR_MAP.put("green", GREEN);
    COLOR_MAP.put("yellow", YELLOW);
    COLOR_MAP.put("blue", BLUE);
    COLOR_MAP.put("purple", PURPLE);
    COLOR_MAP.put("cyan", CYAN);
    COLOR_MAP.put("white", WHITE);
    COLOR_MAP.put("black_bold", BLACK_BOLD);
    COLOR_MAP.put("red_bold", RED_BOLD);
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
  // Regex pattern to match color markers like red[...] or purple[...]
  private static final Pattern COLOR_PATTERN = Pattern.compile("([a-zA-Z_]+)\\[([^\\]]+)\\]");


  /**
   * This is the list of default colors available:
   * <ul>
   * <li><b>Basic Colors:</b> black, red, green, yellow, blur, purple, cyan, white</li>
   * <li><b>Bold Colors:</b> add _bold to the end of the basic colors</li>
   * <li><b>Light Colors:</b> add bright_ to the start of the basic colors except black and white which become bright_grey</li>
   * <li><b>Dark Colors:</b> add dark_ to the start of the basic colors excluding white and black which become dark_ grey</li>
   * <li><b>Styles:</b> bold, bold_italic, italic, strikethrough, underline</li>
   * </ul>
   * <p></p>
   * @deprecated
   * This method is rather too rigid and dosent support multi colors on a single string without splitting it. I might remove this method later.....might...
   * <p>Use {@link #colorize(String)}</p>
   */
  @Deprecated(forRemoval = false)
  // Utility method to colorize strings
  public static String colorize(String message, String color) {
    return color + message + RESET;
  }

  /**
   * This is the list of default colors available:
   * <ul>
   * <li><b>Basic Colors:</b> black, red, green, yellow, blur, purple, cyan, white</li>
   * <li><b>Bold Colors:</b> add _bold to the end of the basic colors</li>
   * <li><b>Light Colors:</b> add bright_ to the start of the basic colors except black and white which become bright_grey</li>
   * <li><b>Dark Colors:</b> add dark_ to the start of the basic colors excluding white and black which become dark_ grey</li>
   * <li><b>Styles:</b> bold, bold_italic, italic, strikethrough, underline</li>
   * </ul>
   * To use this you could go:
   * <p style="padding-left: 20px;"><em><b>colorize('color[style[...message...]] color[style[...message...]] ...')</b></em></p>
   * <p>styles should come inside the color.</p>
   */
  public static String colorize(String message) {
    while (true) {
      Matcher matcher = COLOR_PATTERN.matcher(message);

      if (!matcher.find()) {
        // Break the loop if no matches are found
        break;
      }

      String color = matcher.group(1).toLowerCase(); // Extract color/style name
      String text = matcher.group(2);               // Extract text within the brackets

      // Recursively process the inner text
      String processedText = colorize(text);

      // Look up the ANSI code for the color/style, or default to RESET if unknown
      String colorCode = COLOR_MAP.getOrDefault(color, RESET);

      // Replace the matched segment with the processed content
      message = matcher.replaceFirst(Matcher.quoteReplacement(colorCode + processedText + RESET));
    }

    return message;
  }

  /**
   *
   * @param input The string to be cleaned of ansi codes
   * @return This returns the inputted string with no ansi codes for log saving functionalities
   */
  public static String removeAnsiCodes(String input) {
    return input.replaceAll("\u001B\\[[;\\d]*m", "");
  }

  /**
   * Adds a new color to the color map with the corresponding ANSI code.
   *
   * @param colorName The name of the new color (e.g., "orange").
   * @param ansiCode  The ANSI escape sequence for the color (e.g., "\u001B[38;5;214m").
   */
  public void addCustomColor(String colorName, String ansiCode) {
    COLOR_MAP.put(colorName.toLowerCase(), ansiCode);
  }

}
