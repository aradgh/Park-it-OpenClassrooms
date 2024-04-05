package com.parkit.parkingsystem.util;

public class FareUtil {

    private FareUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static double roundToTwoDecimals(final double value) {
        return Math.ceil(value * 100.0) / 100.0;
    }
}
