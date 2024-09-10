package org.v_utls.utilities;

public class TimeFormatter {

    // Static method to format time from seconds to "min:second"
    public static String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        // Format minutes and seconds to always show two digits
        return String.format("%02d:%02d", minutes, seconds);
    }
}
