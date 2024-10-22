/* Licensed under Apache-2.0 2024. */
package org.v_utls.utilities;

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

  // Background Colors
  public static final String BLACK_BG = "\033[40m"; // BLACK
  public static final String RED_BG = "\033[41m"; // RED
  public static final String GREEN_BG = "\033[42m"; // GREEN
  public static final String YELLOW_BG = "\033[43m"; // YELLOW
  public static final String BLUE_BG = "\033[44m"; // BLUE
  public static final String PURPLE_BG = "\033[45m"; // PURPLE
  public static final String CYAN_BG = "\033[46m"; // CYAN
  public static final String WHITE_BG = "\033[47m"; // WHITE

  // High Intensity Colors (Bright)
  public static final String DARK_GRAY = "\033[0;90m"; // DARK GRAY
  public static final String LIGHT_RED = "\033[0;91m"; // LIGHT RED
  public static final String LIGHT_GREEN = "\033[0;92m"; // LIGHT GREEN
  public static final String LIGHT_YELLOW = "\033[0;93m"; // LIGHT YELLOW
  public static final String LIGHT_BLUE = "\033[0;94m"; // LIGHT BLUE
  public static final String LIGHT_PURPLE = "\033[0;95m"; // LIGHT PURPLE
  public static final String LIGHT_CYAN = "\033[0;96m"; // LIGHT CYAN
  public static final String LIGHT_GRAY = "\033[0;97m"; // LIGHT GRAY

  // High Intensity Background Colors
  public static final String DARK_GRAY_BG = "\033[0;100m"; // DARK GRAY Background
  public static final String LIGHT_RED_BG = "\033[0;101m"; // LIGHT RED Background
  public static final String LIGHT_GREEN_BG = "\033[0;102m"; // LIGHT GREEN Background
  public static final String LIGHT_YELLOW_BG = "\033[0;103m"; // LIGHT YELLOW Background
  public static final String LIGHT_BLUE_BG = "\033[0;104m"; // LIGHT BLUE Background
  public static final String LIGHT_PURPLE_BG = "\033[0;105m"; // LIGHT PURPLE Background
  public static final String LIGHT_CYAN_BG = "\033[0;106m"; // LIGHT CYAN Background
  public static final String LIGHT_GRAY_BG = "\033[0;107m"; // LIGHT GRAY Background

  // Utility method to colorize strings
  public static String colorize(String message, String color) {
    return color + message + RESET;
  }
}
