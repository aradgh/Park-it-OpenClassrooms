package com.parkit.parkingsystem.util;

import java.util.Date;

public class TimeUtil {
    private static final int MILLI_TO_HOUR = 1000 * 60 * 60;
    public static double calculateDurationInHour(Date inTime, Date outTime) {
        return (double) (outTime.getTime() - inTime.getTime()) / MILLI_TO_HOUR;
    }
}
