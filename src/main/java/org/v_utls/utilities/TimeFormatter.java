/* Licensed under Apache-2.0 2024. */
package org.v_utls.utilities;

public class TimeFormatter {

  // Static method to format time from seconds to "min:second"
  public static String formatTimeLong(long totalSeconds) {
    long minutes = totalSeconds / 60;
    long seconds = totalSeconds % 60;

    // Format minutes and seconds to always show two digits
    return String.format("%02d:%02d", minutes, seconds);
  }

  public static String formatTimeInt(int totalSeconds) {
    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;

    // Format minutes and seconds to always show two digits
    return String.format("%02d:%02d", minutes, seconds);
  }
}
