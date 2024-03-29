package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.TimeUtil;

public class FareCalculatorService {
    public void calculateFare(final Ticket ticket, final boolean isDiscount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        final double durationInHour = TimeUtil.calculateDurationInHour(ticket.getInTime(), ticket.getOutTime());
        final double discount = isDiscount ? 0.95 : 1;
        final double minPaidParkingTimeInHour = 0.5;

        if (durationInHour >= minPaidParkingTimeInHour) {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(durationInHour * discount * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(durationInHour * discount * Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }

    public void calculateFare(final Ticket ticket) {
        calculateFare(ticket, false);
    }
}