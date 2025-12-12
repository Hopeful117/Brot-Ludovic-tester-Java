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

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private FareCalculatorService fareCalculatorService;

    @BeforeEach
    private void setUpPerTest() {
        try {
            lenient().when(inputReaderUtil.readSelection()).thenReturn(1);
            lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");



            lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest(){


        try{
            String regNumber = inputReaderUtil.readVehicleRegistrationNumber();
            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
            Ticket ticket = new Ticket();
            ticket.setVehicleRegNumber(regNumber);
            ticket.setParkingSpot(parkingSpot);
            ticket.setInTime(new Date(System.currentTimeMillis() - 3600000)); // 1h avant
            when(ticketDAO.getNbTicket(regNumber)).thenReturn(2);
            when(ticketDAO.getTicket(regNumber)).thenReturn(ticket);

            parkingService.processExitingVehicle();
            verify(ticketDAO, Mockito.times(1)).updateTicket(ticket);


        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Failed to verify method calls");
        }
    }

    @Test
    public void processIncomingVehicleTest(){

        try {

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
            when (parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
            when(ticketDAO.saveTicket(any(Ticket.class))).thenReturn(true);

            parkingService.processIncomingVehicle();




            verify(parkingSpotDAO, Mockito.times(1)).updateParking(parkingSpot);
            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to verify method calls");
        }
    }


    @Test
    public void ExitingVehicleTestUnableUpdate(){

        try{
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





        }
        catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Failed to verify method calls");
        }
    }
    @Test
    public void testGetNextParkingNumberIfAvailable( ){
        ParkingSpot parkingSpot = null;
        try {



            parkingSpot = new ParkingSpot (1,ParkingType.CAR,true);
            parkingService.getNextParkingNumberIfAvailable();
            assert(parkingSpot != null);
            assert(parkingSpot.getId() == 1);
            assert(parkingSpot.getParkingType() == ParkingType.CAR);
            assert(parkingSpot.isAvailable());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to verify method calls");
        }
    }
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {


            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(0);
            assertNull(parkingService.getNextParkingNumberIfAvailable());

    }
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {


            when(inputReaderUtil.readSelection()).thenReturn(3);
            assertNull(parkingService.getNextParkingNumberIfAvailable());


}
}
