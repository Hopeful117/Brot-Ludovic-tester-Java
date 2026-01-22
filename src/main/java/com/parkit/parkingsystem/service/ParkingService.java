package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

/**
 * Service class to process incoming and exiting vehicles.
 */
public class ParkingService {

    private static final Logger logger = LogManager.getLogger("ParkingService");

    private static FareCalculatorService fareCalculatorService = new FareCalculatorService();

    private InputReaderUtil inputReaderUtil;
    private ParkingSpotDAO parkingSpotDAO;
    private  TicketDAO ticketDAO;

    public ParkingService(InputReaderUtil inputReaderUtil, ParkingSpotDAO parkingSpotDAO, TicketDAO ticketDAO){
        this.inputReaderUtil = inputReaderUtil;
        this.parkingSpotDAO = parkingSpotDAO;
        this.ticketDAO = ticketDAO;
    }

    /**
     * Processes an incoming vehicle by allocating a parking spot and generating a ticket.
     */
    public void processIncomingVehicle() {
        try{
            ParkingSpot parkingSpot = getNextParkingNumberIfAvailable();
            if(parkingSpot !=null && parkingSpot.getId() > 0){
                String vehicleRegNumber = getVehicleRegNumber();
                parkingSpot.setAvailable(false);
                parkingSpotDAO.updateParking(parkingSpot);//allot this parking space and mark it's availability as false
                boolean recurrentCustomer =ticketDAO.getNbTicket(vehicleRegNumber)>0;

                Date inTime = new Date();
                Ticket ticket = new Ticket();
                //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
                //ticket.setId(ticketID);
                ticket.setParkingSpot(parkingSpot);
                ticket.setVehicleRegNumber(vehicleRegNumber);
                ticket.setPrice(0);
                ticket.setInTime(inTime);
                ticket.setOutTime(null);
                ticketDAO.saveTicket(ticket);
                if (recurrentCustomer){
                    logger.info("Heureux de vous revoir ! En tant qu’utilisateur régulier de notre parking, vous allez obtenir une remise de 5 %");
                }
                else{
                    logger.info("Bienvenue !");
                }

                logger.info("Generated Ticket and saved in DB");
                logger.info("Please park your vehicle in spot number:"+parkingSpot.getId());
                logger.info("Recorded in-time for vehicle number:"+vehicleRegNumber+" is:"+inTime);
            }
        }catch(Exception e){
            logger.error("Unable to process incoming vehicle",e);
        }
    }

    /**
     * Gets the vehicle registration number from user input.
     * @return
     * @throws Exception
     */
    private String getVehicleRegNumber() throws Exception {
        logger.info("Please type the vehicle registration number and press enter key");
        return inputReaderUtil.readVehicleRegistrationNumber();
    }
/**
     * Retrieves the next available parking spot based on vehicle type.
     * @return ParkingSpot object if available, null otherwise.
     */
    public ParkingSpot getNextParkingNumberIfAvailable(){
        int parkingNumber=0;
        ParkingSpot parkingSpot = null;
        try{
            ParkingType parkingType = getVehicleType();
            parkingNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);
            if(parkingNumber > 0){
                parkingSpot = new ParkingSpot(parkingNumber,parkingType, true);
            }else{
                logger.error("Error fetching parking number from DB. Parking slots might be full");


            }
        }catch(IllegalArgumentException ie){
            logger.error("Error parsing user input for type of vehicle", ie);

        }catch(Exception e){
            logger.error("Error fetching next available parking slot", e);
        }
        return parkingSpot;
    }
/**
     * Prompts the user to select a vehicle type and returns the corresponding ParkingType.
     * @return ParkingType selected by the user.
     */
    private ParkingType getVehicleType(){
        logger.info("Please select vehicle type from menu");
        logger.info("1 CAR");
        logger.info("2 BIKE");
        int input = inputReaderUtil.readSelection();
        switch(input){
            case 1: {
                return ParkingType.CAR;
            }
            case 2: {
                return ParkingType.BIKE;
            }
            default: {
               logger.error("Incorrect input provided");
               throw new IllegalArgumentException("Entered input is invalid");
            }
        }
    }

    /**
     * Processes an exiting vehicle by calculating the fare and updating the ticket.
     */
    public void processExitingVehicle() {
        try{
            String vehicleRegNumber = getVehicleRegNumber();
            boolean recurrentCustomer =ticketDAO.getNbTicket(vehicleRegNumber)>1;
            Ticket ticket = ticketDAO.getTicket(vehicleRegNumber);
            Date outTime = new Date();
            ticket.setOutTime(outTime);
            if (recurrentCustomer) {
                fareCalculatorService.calculateFare(ticket, true);
            }
            else {

                fareCalculatorService.calculateFare(ticket);
            }
            if(ticketDAO.updateTicket(ticket)) {
                ParkingSpot parkingSpot = ticket.getParkingSpot();
                parkingSpot.setAvailable(true);
                parkingSpotDAO.updateParking(parkingSpot);
                logger.info("Please pay the parking fare:" + String.format("%.2f",ticket.getPrice()));
                logger.info("Recorded out-time for vehicle number:" + ticket.getVehicleRegNumber() + " is:" + (outTime));
            }else{
                logger.info("Unable to update ticket information. Error occurred");
            }
        }catch(Exception e){
            logger.error("Unable to process exiting vehicle",e);
        }
    }
}
