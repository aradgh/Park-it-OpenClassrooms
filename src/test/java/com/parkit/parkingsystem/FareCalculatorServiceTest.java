package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static com.parkit.parkingsystem.util.FareUtil.roundToTwoDecimals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    public static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    public void setUpPerTest() {
        ticket = new Ticket();
    }

    private void setTicket(final Date inTime, final Date outTime, final ParkingType parkingType) {
        final ParkingSpot parkingSpot = new ParkingSpot(1, parkingType, false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
    }

    private Date getDateWithOffset(final long offsetMillis) {
        final Date date = new Date();
        date.setTime(System.currentTimeMillis() + offsetMillis);
        return date;
    }

    @Test
    void calculateFareCar() {
        final Date inTime = getDateWithOffset(-(60 * 60 * 1000));
        final Date outTime = new Date();
        setTicket(inTime, outTime, ParkingType.CAR);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    void calculateFareBike() {
        final Date inTime = getDateWithOffset(-(60 * 60 * 1000));
        final Date outTime = new Date();
        setTicket(inTime, outTime, ParkingType.BIKE);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    void calculateFareUnknownType() {
        final Date inTime = getDateWithOffset(-(60 * 60 * 1000));
        final Date outTime = new Date();
        setTicket(inTime, outTime, null);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareBikeWithFutureInTime() {
        final Date inTime = getDateWithOffset(60 * 60 * 1000);
        final Date outTime = new Date();
        setTicket(inTime, outTime, ParkingType.BIKE);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareBikeWithLessThanOneHourParkingTime() {
        final Date inTime = getDateWithOffset(-(45 * 60 * 1000));
        final Date outTime = new Date();
        setTicket(inTime, outTime, ParkingType.BIKE);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(0.75 * Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    void calculateFareBikeWithLessThan30MinutesParkingTime() {
        final Date inTime = getDateWithOffset(-(29 * 60 * 1000));
        final Date outTime = new Date();
        setTicket(inTime, outTime, ParkingType.BIKE);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(0, ticket.getPrice());
    }

    @Test
    void calculateFareCarWithLessThan30MinutesParkingTime() {
        final Date inTime = getDateWithOffset(-(29 * 60 * 1000));
        final Date outTime = new Date();
        setTicket(inTime, outTime, ParkingType.CAR);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(0, ticket.getPrice());
    }

    @Test
    void calculateFareCarWithLessThanOneHourParkingTime() {
        final Date inTime = getDateWithOffset(-(45 * 60 * 1000));
        final Date outTime = new Date();
        setTicket(inTime, outTime, ParkingType.CAR);
        fareCalculatorService.calculateFare(ticket);
        double expectedPrice = roundToTwoDecimals(0.75 * Fare.CAR_RATE_PER_HOUR);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    void calculateFareCarWithMoreThanADayParkingTime() {
        final Date inTime = getDateWithOffset(-(24 * 60 * 60 * 1000));
        final Date outTime = new Date();
        setTicket(inTime, outTime, ParkingType.CAR);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(24 * Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    void calculateFareCarWithDiscount() {
        final Date inTime = getDateWithOffset(-(60 * 60 * 1000));
        final Date outTime = new Date();
        setTicket(inTime, outTime, ParkingType.CAR);
        fareCalculatorService.calculateFare(ticket, true);
        final double expectedPrice = roundToTwoDecimals(Fare.CAR_RATE_PER_HOUR * 0.95);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    void calculateFareBikeWithDiscount() {
        final Date inTime = getDateWithOffset(-(60 * 60 * 1000));
        final Date outTime = new Date();
        setTicket(inTime, outTime, ParkingType.BIKE);
        fareCalculatorService.calculateFare(ticket, true);
        final double expectedPrice = roundToTwoDecimals(Fare.BIKE_RATE_PER_HOUR * 0.95);
        assertEquals(expectedPrice, ticket.getPrice());
    }
}
