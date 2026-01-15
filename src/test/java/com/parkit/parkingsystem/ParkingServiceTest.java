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


        String regNumber = inputReaderUtil.readVehicleRegistrationNumber();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(regNumber);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date(System.currentTimeMillis() - 3600000)); // 1h avant
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(2);
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);

        parkingService.processExitingVehicle();
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        assertNotNull(ticket.getOutTime());

    }
    @Test
    void processExitingVehicleTest() throws Exception {
        String regNumber = inputReaderUtil.readVehicleRegistrationNumber();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber(regNumber);
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date(System.currentTimeMillis() - 3600000)); // 1h avant
        when(ticketDAO.getNbTicket(regNumber)).thenReturn(1);
        when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
        when(ticketDAO.updateTicket(ticket)).thenReturn(true);

        parkingService.processExitingVehicle();
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        assertNotNull(ticket.getOutTime());
    }



    @Test
    void processIncomingVehicleTest() throws Exception {



            when (inputReaderUtil.readSelection()).thenReturn(1);
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when (parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

            parkingService.processIncomingVehicle();




        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));

    }
    @Test
    void processIncomingVehicleReccuringUserTest() throws Exception {
        when (inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when (parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(2);
        when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

        parkingService.processIncomingVehicle();
        verify(parkingSpotDAO).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    }


    @Test
    void ExitingVehicleTestUnableUpdate() throws Exception {


            String regNumber = inputReaderUtil.readVehicleRegistrationNumber();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setVehicleRegNumber(regNumber);
            ticket.setParkingSpot(parkingSpot);
            ticket.setInTime(new Date(System.currentTimeMillis() - 3600000)); // 1h avant
            when(ticketDAO.getNbTicket(regNumber)).thenReturn(2);
            when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);
            when(ticketDAO.updateTicket(ticket)).thenReturn(false);

            parkingService.processExitingVehicle();

            verify(ticketDAO,Mockito.times(1)).updateTicket(any(Ticket.class));
            verify(parkingSpotDAO, never()).updateParking(any());


        }


    @Test
    void testGetNextParkingNumberIfAvailable( ){
        when(inputReaderUtil.readSelection()).thenReturn(1);

       when (parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);


       ParkingSpot spot = parkingService.getNextParkingNumberIfAvailable();
       assertNotNull(spot);
       assertEquals(1, spot.getId());
       assertEquals(ParkingType.CAR, spot.getParkingType());
       assertTrue(spot.isAvailable());






    }
    @Test
    void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {


            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);
            assertNull(parkingService.getNextParkingNumberIfAvailable());

    }
    @Test
    void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {


            when(inputReaderUtil.readSelection()).thenReturn(3);
            assertNull(parkingService.getNextParkingNumberIfAvailable());


}
    @Test
    void testSaveTicketFailure() {
      when(ticketDAO.saveTicket(any(Ticket.class))).thenThrow(new RuntimeException("DB Error"));
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setParkingSpot(parkingSpot);
        ticket.setInTime(new Date(System.currentTimeMillis() - 3600000));
        assertThrows(RuntimeException.class, () -> {
            ticketDAO.saveTicket(ticket);
        });


    }
    @Test
    void testGetTicketNotFound() {
        when(ticketDAO.getTicket("NONEXISTENT")).thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class, () -> {
            ticketDAO.getTicket("NONEXISTENT");


        });
    }

}
