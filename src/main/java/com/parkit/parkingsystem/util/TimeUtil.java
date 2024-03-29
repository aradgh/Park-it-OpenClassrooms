package com.parkit.parkingsystem.util;

import java.util.Date;

public class TimeUtil {
    private static final int MILLI_TO_HOUR = 1000 * 60 * 60;

    private TimeUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static double calculateDurationInHour(final Date inTime, final Date outTime) {
        return (double) (outTime.getTime() - inTime.getTime()) / MILLI_TO_HOUR;
    }
}
