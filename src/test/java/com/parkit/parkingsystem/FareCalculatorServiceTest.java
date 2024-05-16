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

    @Test
    void calculateFareCar() {
        //TODO : Factoriser ce bout de code qui se répète dans plusieurs tests
        final Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        final Date outTime = new Date();
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    void calculateFareBike() {
        final Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        final Date outTime = new Date();
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(Fare.BIKE_RATE_PER_HOUR, ticket.getPrice());
    }

    @Test
    void calculateFareUnkownType() {
        final Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        final Date outTime = new Date();
        final ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareBikeWithFutureInTime() {
        final Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        final Date outTime = new Date();
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareBikeWithLessThanOneHourParkingTime() {
        final Date inTime = new Date();
        inTime.setTime(
            System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        final Date outTime = new Date();
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    void calculateFareBikeWithLessThan30minutesParkingTime() {
//        Given
        final Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000));
        final Date outTime = new Date();
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
//        When
        fareCalculatorService.calculateFare(ticket);
//        Then
        assertEquals(0, ticket.getPrice());
    }

    @Test
    void calculateFareCarWithLessThan30minutesParkingTime() {
//        Given
        final Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (29 * 60 * 1000));
        final Date outTime = new Date();
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
//        When
        fareCalculatorService.calculateFare(ticket);
//        Then
        assertEquals(0, ticket.getPrice());
    }

    @Test
    void calculateFareCarWithLessThanOneHourParkingTime() {
        final Date inTime = new Date();
        inTime.setTime(
            System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        final Date outTime = new Date();
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);

        final double expectedPrice = roundToTwoDecimals(0.75 * Fare.CAR_RATE_PER_HOUR);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    void calculateFareCarWithMoreThanADayParkingTime() {
        final Date inTime = new Date();
        inTime.setTime(
            System.currentTimeMillis() - (24 * 60 * 60 * 1000));//24 hours parking time should give 24 * parking fare per hour
        final Date outTime = new Date();
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    void calculateFareCarWithDiscount() {
        final Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        final Date outTime = new Date();

        final double durationInHour = (double) (outTime.getTime() - inTime.getTime()) / 1000 / 60 / 60;
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);

        final double expectedPrice = roundToTwoDecimals(durationInHour * Fare.CAR_RATE_PER_HOUR * 0.95);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    void calculateFareBikeWithDiscount() {
        final Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        final Date outTime = new Date();

        final double durationInHour = (double) (outTime.getTime() - inTime.getTime()) / 1000 / 60 / 60;
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, true);
        assertEquals(durationInHour * Fare.BIKE_RATE_PER_HOUR * 0.95, ticket.getPrice());
    }

}
