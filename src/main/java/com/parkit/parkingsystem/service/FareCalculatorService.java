package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

/**
 * Service to calculate the fare of a parking ticket.
 * The fare is calculated based on the parking type and duration.
 * The rates are defined in the Fare class.
 * The duration is calculated in hours.
 * The time is provided in milliseconds.
 * if the time is inferior to 30 min the prices should be 0
 */
public class FareCalculatorService {

    public void calculateFare(Ticket ticket ,boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();


        float duration = (outHour  - inHour) ;
        duration= duration / (1000 * 60 * 60); // convert milliseconds to hours

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if(duration > 0.5 && !discount) {
                    ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                }
                else if(duration > 0.5 && discount){
                    ticket.setPrice((duration * Fare.CAR_RATE_PER_HOUR)*0.95);
                }
                else{

                    ticket.setPrice(0);
                }
                break;
            }
            case BIKE: {
                if(duration > 0.5 && !discount) {
                    ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                }
                else if(duration > 0.5 && discount){
                    ticket.setPrice((duration * Fare.BIKE_RATE_PER_HOUR)*0.95);
                }
                else{
                    ticket.setPrice(0);
                }
                break;
            }
            default: throw new IllegalArgumentException("Unknown Parking Type");
        }
    }

    public void calculateFare(Ticket ticket){
        calculateFare(ticket,false);
    }
}