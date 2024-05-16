package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
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

import static com.parkit.parkingsystem.util.FareUtil.roundToTwoDecimals;
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
//        when(inputReaderUtil.readSelection()).thenReturn(1);
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

        final Ticket ticketEntry = new Ticket();
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        ticketEntry.setVehicleRegNumber("ABCDEF");
        ticketEntry.setParkingSpot(parkingSpot);

        final Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        ticketEntry.setInTime(inTime);
        ticketDAO.saveTicket(ticketEntry);

        parkingService.processExitingVehicle();

        final Ticket ticketExit = ticketDAO.getTicket("ABCDEF");

        assertNotNull(ticketExit.getOutTime());
        assertTrue(ticketExit.getOutTime().getTime() >= ticketExit.getInTime().getTime());
        assertTrue(Fare.CAR_RATE_PER_HOUR <= ticketExit.getPrice());
    }

    @Test
    void testParkingLotExitRecurringUser() {
        final ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        final Ticket ticketEntryFirst = new Ticket();
        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        ticketEntryFirst.setVehicleRegNumber("ABCDEF");
        ticketEntryFirst.setParkingSpot(parkingSpot);

        final Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (3 * 60 * 60 * 1000));
        ticketEntryFirst.setInTime(inTime);
        final Date outTime = new Date();
        outTime.setTime(System.currentTimeMillis() - (2 * 60 * 60 * 1000));
        ticketEntryFirst.setOutTime(outTime);
        ticketDAO.saveTicket(ticketEntryFirst);

        final Ticket ticketEntrySecond = new Ticket();
        ticketEntrySecond.setVehicleRegNumber("ABCDEF");
        ticketEntrySecond.setParkingSpot(parkingSpot);
        final Date inTimeSecond = new Date();
        inTimeSecond.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        ticketEntrySecond.setInTime(inTimeSecond);
        ticketDAO.saveTicket(ticketEntrySecond);

        parkingService.processExitingVehicle();

        final Ticket ticketExit = ticketDAO.getTicket("ABCDEF");
        final double expectedPrice = roundToTwoDecimals(0.95 * Fare.CAR_RATE_PER_HOUR);

        assertNotNull(ticketExit.getOutTime());
        assertTrue(ticketExit.getOutTime().getTime() >= ticketExit.getInTime().getTime());
        assertEquals(expectedPrice, ticketExit.getPrice());
    }
}
