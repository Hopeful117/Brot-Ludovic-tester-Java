package com.parkit.parkingsystem.integration;

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

import static junit.framework.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
 class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    static void setUp() {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    static void tearDown() {
        /** Method to clean up resources after all tests have run. */

    }

    @Test
    void testParkingACar() {
        // GIVEN a parking service with a car to park
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // WHEN parking the car
        parkingService.processIncomingVehicle();

        // THEN a ticket should be generated and the parking spot marked as not available
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(ticket);
        ParkingSpot parkingSpot = ticket.getParkingSpot();

        assertNotNull(parkingSpot);
        assertFalse(parkingSpot.isAvailable());

    }

    @Test
    public void testParkingLotExit(){
        // GIVEN a parking service with a car already parked
        testParkingACar();
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket ticket = ticketDAO.getTicket("ABCDEF");
        int inTime = 3_600_000 ; // 1 hour in milliseconds
        ticket.setInTime(new Date(System.currentTimeMillis() - inTime));
        ticketDAO.saveTicket(ticket);

        // WHEN exiting the parking lot
        parkingService.processExitingVehicle();

        // THEN the ticket should be updated with out time and price, and the parking spot marked as available
        Ticket exitingticket = ticketDAO.getTicket("ABCDEF");
        assertNotNull(exitingticket);
        assertNotNull(exitingticket.getOutTime());
        assertTrue(exitingticket.getPrice() >= 0);
        ParkingSpot spot = exitingticket.getParkingSpot();
        assertTrue(parkingSpotDAO.updateParking(spot));
    }


    @Test
    void testParkingLotExitRecurringUser() {
        // GIVEN a parking service with a car already parked twice
        int inTime = 3_600_000 ; // 1 hour in milliseconds
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - inTime));
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setParkingSpot(new ParkingSpot(1, ParkingType.CAR, false));
        ticketDAO.saveTicket(ticket);
        parkingService.processExitingVehicle();
        double firstPrice = ticketDAO.getTicket("ABCDEF").getPrice();





            // Second parking
        parkingService.processIncomingVehicle();
        Ticket secondTicket = ticketDAO.getTicket("ABCDEF");
        secondTicket.setInTime(new Date(System.currentTimeMillis() - inTime));
        secondTicket.setOutTime(new Date());
        ticketDAO.updateTicket(secondTicket);
        //WHEN exiting the parking lot again

        parkingService.processExitingVehicle();
        double secondPrice = ticketDAO.getTicket("ABCDEF").getPrice();

        //THEN the second ticket price should be less than the first one due to the discount for recurring users
        assertNotNull(secondTicket.getOutTime());
        assertTrue(ticketDAO.getNbTicket("ABCDEF")>1);
        assertTrue(secondPrice < firstPrice);











    }


}