package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    @Mock
    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    @BeforeEach
    private void setUpPerTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);

            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest(){

        parkingService.processExitingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
        when(ticketDAO.getNbTicket(anyString())).thenReturn(0);
        verify(fareCalculatorService, Mockito.times(1)).calculateFare(any(Ticket.class), anyBoolean());
    }

    @Test
    public void testProcessIncomingVehicle() {
        // Act
        parkingService.processIncomingVehicle();

        // Assert
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).saveTicket(any(Ticket.class));
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() {
        when(ticketDAO.getNbTicket(anyString())).thenReturn(1);

        // Act
        parkingService.processExitingVehicle();

        // Assert
        verify(parkingSpotDAO, never()).updateParking(any(ParkingSpot.class));
        verify(fareCalculatorService, never()).calculateFare(any(Ticket.class), anyBoolean());
        // Other verifications as needed
    }

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        // Arrange
        when(inputReaderUtil.readSelection()).thenReturn(1); // Assuming CAR is selected
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);

        // Act
        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(ParkingType.CAR, result.getParkingType());
        assertTrue(result.isAvailable());
    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        // Arrange
        when(inputReaderUtil.readSelection()).thenReturn(1); // Assuming CAR is selected
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(0);

        // Act
        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        // Assert
        assertNull(result);
    }

//    @Test
//    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
//        // Arrange
//        when(inputReaderUtil.readSelection()).thenReturn(3); // Invalid selection
//
//        // Act
//        ParkingTypeException exception = assertThrows(ParkingTypeException.class,
//            () -> parkingService.getNextParkingNumberIfAvailable());
//
//        // Assert
//        assertEquals("Entered input is invalid", exception.getMessage());
//    }

}
