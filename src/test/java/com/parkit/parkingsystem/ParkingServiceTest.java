package com.parkit.parkingsystem;

import com.parkit.parkingsystem.config.DataBaseConfig;
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

import java.sql.SQLException;
import java.util.Date;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {

    private ParkingService parkingService;

    @Mock
    private InputReaderUtil inputReaderUtil;
    @Mock
    private  ParkingSpotDAO parkingSpotDAO;
    @Mock
    private TicketDAO ticketDAO;

    @BeforeEach
    void setUpPerTest() {
        try {


            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");




            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        } catch (Exception e) {

            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    void processExitingVehicleReccuringUserTest() throws Exception {

        //Given a recurring user with more than 1 ticket
        String regNumber = inputReaderUtil.readVehicleRegistrationNumber();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(regNumber);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date(System.currentTimeMillis() - 3600000)); // 1h avant
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(2);
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);

        //When processing exiting vehicle
        parkingService.processExitingVehicle();

        //Then verify parking spot is updated and out time is set
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        assertNotNull(ticket.getOutTime());

    }
    @Test
    void processExitingVehicleTest() throws Exception {

        //Given a non-recurring user
        String regNumber = inputReaderUtil.readVehicleRegistrationNumber();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(regNumber);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date(System.currentTimeMillis() - 3600000)); // 1h avant
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(1);
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);

        //When processing exiting vehicle
        parkingService.processExitingVehicle();

        //Then verify parking spot is updated and out time is set
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        assertNotNull(ticket.getOutTime());
    }



    @Test
    void processIncomingVehicleTest() throws Exception {


        //Given a car entering the parking
        when (inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when (parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        //When processing incoming vehicle
        parkingService.processIncomingVehicle();



        //Then verify parking spot is updated and ticket is saved
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));

    }
    @Test
    void processIncomingVehicleReccuringUserTest() throws Exception {
        //Given a recurring user entering the parking
        when (inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when (parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        //When processing incoming vehicle
        parkingService.processIncomingVehicle();

        //Then verify parking spot is updated and ticket is saved
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    }


    @Test
    void ExitingVehicleTestUnableUpdate() throws Exception {

        //Given a vehicle exiting but unable to update ticket
        String regNumber = inputReaderUtil.readVehicleRegistrationNumber();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(regNumber);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date(System.currentTimeMillis() - 3600000)); // 1h avant
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(2);
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
        when(ticketDAO.updateTicket(ticket)).thenReturn(false);

        //When processing exiting vehicle
        parkingService.processExitingVehicle();

        //Then verify ticket update was attempted but parking spot update was not
        verify(ticketDAO,Mockito.times(1)).updateTicket(any(Ticket.class));
        verify(parkingSpotDAO, never()).updateParking(any());


        }


    @Test
    void testGetNextParkingNumberIfAvailable( ){
        //Given a car parking spot available
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when (parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);

        //When getting next parking number
        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();

        //Then verify the parking spot details
        assertNotNull(spot);
        assertEquals(1, spot.getId());
        assertEquals(ParkingType.CAR, spot.getParkingType());
        assertTrue(spot.isAvailable());






    }
    @Test
    void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {

        //Given no car parking spot available
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);

        //When getting next parking number
        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();

        //Then verify null is returned
        assertNull(spot);



    }

    @Test
    void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {

        //Given an invalid parking type selection
        when(inputReaderUtil.readSelection()).thenReturn(3);

        //When getting next parking number
        ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();

        //Then verify null is returned
        assertNull(spot);


}
    @Test
    void testSaveTicketFailure() {
        //Given a failure when saving ticket
        when(ticketDAO.saveTicket(any(Ticket.class))).thenThrow(new RuntimeException("DB Error"));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date(System.currentTimeMillis() - 3600000));


        //Then verify exception is thrown
        assertThrows(RuntimeException.class, () -> {
            ticketDAO.saveTicket(ticket);
        });


    }
    @Test
    void testGetTicketNotFound() {
        //Given a failure when getting ticket
        when(ticketDAO.getTicket("NONEXISTENT")).thenThrow(new RuntimeException("DB Error"));



        //Then verify exception is thrown
        assertThrows(RuntimeException.class, () -> {
            ticketDAO.getTicket("NONEXISTENT");


        });
    }


}
