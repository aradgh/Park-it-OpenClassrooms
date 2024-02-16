package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public static void main(String[] args) {
        Ticket ticket = new Ticket();
        System.out.println(ticket.getPrice());
    }

    public void calculateFare(Ticket ticket, boolean isDiscount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        final long inTime = ticket.getInTime().getTime();
        final long outTime = ticket.getOutTime().getTime();

//        TODO:Créer une classe TimeUtils avec une méthode static final calculateDuration() scope par défaut dans le package service
//        TODO:Créer un test pour calculateDuration
//        Config intellij pour mettre automatiquement les final
//        (1000 * 60 * 60 ) là mettre dans une final private MILLI_TO_HOUR
        final double durationInHour = (double) (outTime - inTime) /1000/60/60;

        final double discount = isDiscount ? 0.95 : 1;
        final double minPaidParkingTimeInHour = 0.5;
        if(durationInHour >= minPaidParkingTimeInHour) {
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR: {
                    ticket.setPrice(durationInHour * discount * Fare.CAR_RATE_PER_HOUR);
                    break;
                }
                case BIKE: {
                    ticket.setPrice(durationInHour * discount*  Fare.BIKE_RATE_PER_HOUR);
                    break;
                }
                default: throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }
}