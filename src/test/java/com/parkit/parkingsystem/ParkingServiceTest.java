package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static com.parkit.parkingsystem.util.FareUtil.roundToTwoDecimals;
import static junit.framework.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    @Mock
    private static ParkingSpot parkingSpot;
    @Mock
    private static ParkingService parkingService;
    @Mock
    private static Ticket ticket;

    @BeforeEach
    public void setUpPerTest() {
        try {
            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");
        }
    }


    private void setUpTicketAndVehicleRegNumber() {
        parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
    }

    @Test
    void processExitingVehicleTestAbleUpdate() {
        setUpTicketAndVehicleRegNumber();

        when(ticketDAO.updateTicket(ticket)).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket(anyString())).thenReturn(2);

        parkingService.processExitingVehicle();

        verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
//        Pourquoi mvn verify pete une erreur ?
        verify(ticketDAO, Mockito.times(1)).updateTicket(ticket);
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

        final double expectedPrice = roundToTwoDecimals(1.5 * 0.95);
//        Mettre le prix en dur, utiliser BigDecimal
        assertEquals(expectedPrice, ticket.getPrice());
    }

    @Test
    void testProcessIncomingVehicle() {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);
        // Act
        parkingService.processIncomingVehicle();

        // Assert
        verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
//        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
    }

    @Test
    void processExitingVehicleTestUnableUpdate() {
        setUpTicketAndVehicleRegNumber();
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        parkingService.processExitingVehicle();

        verify(inputReaderUtil, Mockito.times(1)).readVehicleRegistrationNumber();
        verify(ticketDAO, Mockito.times(1)).getTicket(anyString());
        verify(ticketDAO, Mockito.times(1)).getNbTicket(anyString());
        verify(ticketDAO, Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
    }

    @Test
    void testGetNextParkingNumberIfAvailable() {
        // Arrange
        when(inputReaderUtil.readSelection()).thenReturn(1); // Assuming CAR is selected
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);

        // Act
        final ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(ParkingType.CAR, result.getParkingType());
        assertTrue(result.isAvailable());
    }

    @Test
    void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        // Arrange
        when(inputReaderUtil.readSelection()).thenReturn(1); // Assuming CAR is selected
        when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(0);

        // Act
        final ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        // Assert
        assertNull(result);
    }

    @Test
    void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        // Arrange
        when(inputReaderUtil.readSelection()).thenReturn(3); // Invalid selection

        // Act
        final ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        // Assert
        assertNull(result);
    }

}
