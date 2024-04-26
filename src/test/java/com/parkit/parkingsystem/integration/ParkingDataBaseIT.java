package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @AfterAll
    public static void tearDown() {

    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    void testParkingACar() {
        final ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability
        final Ticket ticket = ticketDAO.getTicket("ABCDEF");
        final ParkingSpot parkingSpot = ticket.getParkingSpot();
        assertNotNull(ticket);
        assertNotNull(ticket.getInTime());
        assertNull(ticket.getOutTime());
        assertFalse(parkingSpot.isAvailable());
    }

    @Test
    void testParkingLotExit() {
        final ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        try {
            Thread.sleep(2000); // Pause de 2 secondes (2000 millisecondes)
        } catch (InterruptedException e) {
            // Gérer l'exception si la pause est interrompue
        }
        final Ticket ticketEntry = ticketDAO.getTicket("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
        final Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        ticketEntry.setInTime(inTime);
        ticketDAO.saveTicket(ticketEntry);

        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
        final Ticket ticketExit = ticketDAO.getTicket("ABCDEF");

        assertNotNull(ticketExit.getOutTime());
        assertTrue(ticketExit.getOutTime().getTime() >= ticketExit.getInTime().getTime());
//        Mettre le intime il y a 3 heures
        assertEquals(Fare.CAR_RATE_PER_HOUR, ticketExit.getPrice());
    }
}
