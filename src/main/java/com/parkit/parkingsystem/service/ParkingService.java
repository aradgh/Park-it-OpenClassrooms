package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static final FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private final InputReaderUtil inputReaderUtil;
    private final ParkingSpotDAO parkingSpotDAO;
    private final TicketDAO ticketDAO;

    public ParkingService(
        final InputReaderUtil inputReaderUtil, final ParkingSpotDAO parkingSpotDAO, final TicketDAO ticketDAO
    ) {
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    public void processIncomingVehicle() {
        try {
            final ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if (parkingSpot != null && parkingSpot.getId() > 0) {
                final String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);//allot this parking space and mark its availability as false

                final Date inTime = new Date();
                final Ticket ticket = new Ticket();

                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticketDAO.saveTicket(ticket);
                System.out.println("Generated Ticket and saved in DB");
                System.out.println("Please park your vehicle in spot number:" + parkingSpot.getId());
                System.out.println("Recorded in-time for vehicle number:" + vehicleRegNumber + " is:" + inTime);
                if (ticketDAO.getNbTicket(vehicleRegNumber) > 1) System.out.println(
                    "Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5%");
            }
        } catch (Exception e) {
            logger.error("Unable to process incoming vehicle", e);
        }
    }

    public ParkingSpot getNextParkingNumberIfAvailable() {
        int parkingNumber = 0;
        ParkingSpot parkingSpot = null;
        try {
            ParkingType parkingType = getVehicleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if (parkingNumber > 0) {
                parkingSpot = new ParkingSpot(parkingNumber, parkingType, true);
            } else {
                throw new Exception("Error fetching parking number from DB. Parking slots might be full");
            }
        } catch (IllegalArgumentException ie) {
            logger.error("Error parsing user input for type of vehicle", ie);
        } catch (Exception e) {
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }

    private ParkingType getVehicleType() {
        System.out.println("Please select vehicle type from menu");
        System.out.println("1 CAR");
        System.out.println("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch (input) {
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
                System.out.println("Incorrect input provided");
                throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    public void processExitingVehicle() {
        try {
            final String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
            final Ticket ticket = getTicketAndUpdateOutTime(vehicleRegNumber);
            calculateAndSetFare(ticket);
            if (updateTicketAndFreeSpot(ticket)) {
                printExitDetails(ticket);
            } else {
                logger.error("Unable to update ticket information. Error occurred");
            }
        } catch (Exception e) {
            logger.error("Unable to process exiting vehicle", e);
        }
    }

    @NotNull
    private Ticket getTicketAndUpdateOutTime(final String vehicleRegNumber) {
        final Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
        final Date outTime = new Date();
        ticket.setOutTime(outTime);
        return ticket;
    }

    private void calculateAndSetFare(@NotNull final Ticket ticket) {
        final int nbTicket = ticketDAO.getNbTicket(ticket.getVehicleRegNumber());
        fareCalculatorService.calculateFare(ticket, nbTicket > 1);
    }

    private boolean updateTicketAndFreeSpot(final Ticket ticket) {
        if (ticketDAO.updateTicket(ticket)) {
            final ParkingSpot parkingSpot = ticket.getParkingSpot();
            parkingSpot.setAvailable(true);
            parkingSpotDAO.updateParking(parkingSpot);
            return true;
        }
        return false;
    }

    private void printExitDetails(@NotNull final Ticket ticket) {
        logger.info("Please pay the parking fare: {}", ticket.getPrice());
        logger.info(
            "Recorded out-time for vehicle number: {} is: {}", ticket.getVehicleRegNumber(), ticket.getOutTime());
    }
}
