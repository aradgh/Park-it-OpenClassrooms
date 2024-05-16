package com.parkit.parkingsystem;

import com.parkit.parkingsystem.util.TimeUtil;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimeUtilTest {

    @Test
    void testCalculateDurationInHour() {
        // Given
        final Date inTime = new Date(System.currentTimeMillis() - (2 * 60 * 60 * 1000)); // 2 hours ago
        final Date outTime = new Date(System.currentTimeMillis());

        // When
        final double durationInHour = TimeUtil.calculateDurationInHour(inTime, outTime);

        // Then
        assertEquals(2.0, durationInHour, 0.01); // Using delta to handle potential precision issues
    }
}
