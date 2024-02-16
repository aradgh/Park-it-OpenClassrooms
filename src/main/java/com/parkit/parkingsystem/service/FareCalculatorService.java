package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
//        Créer une classe TimeUtils avec une méthode static final claculateDuration() scope par défaut dans le package service
//        Créer un test pour calculateDuration
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();

        double durationInHour = (double) (outTime - inTime) /1000/60/60;


        final double minPaidParkingTimeInHour = 0.5;
//        Simplifier le switch
        if(durationInHour > minPaidParkingTimeInHour) {
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    ticket.setPrice(durationInHour * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(durationInHour * Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        } else {
            ticket.setPrice(0);
        }

//        double vehicleTypeRate = 0;
//        switch (ticket.getParkingSpot().getParkingType()){
//            case CAR: {
//                vehicleTypeRate = Fare.CAR_RATE_PER_HOUR;
//                break;
//            }
//            case BIKE: {
//                vehicleTypeRate = Fare.BIKE_RATE_PER_HOUR;
//                break;
//            }
//            default: throw new IllegalArgumentException("Unkown Parking Type");
//        }
//        ticket.setPrice(duration * vehicleTypeRate);
    }
}