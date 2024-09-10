package org.v_utls.utilities;

public class TimeDifference {
    private long timeDifferenceInSeconds;

    // Constructor that takes startTime and currentTime, both in milliseconds
    public TimeDifference(long startTime, long currentTime) {
        calculateTimeDifference(startTime, currentTime);
    }

    // Method to calculate and store the time difference in seconds
    private void calculateTimeDifference(long startTime, long currentTime) {
        this.timeDifferenceInSeconds = (currentTime - startTime) / 1000; // Convert to seconds
    }

    // Getter to access the time difference globally
    public long getTimeDifferenceInSeconds() {
        return this.timeDifferenceInSeconds;
    }
}

