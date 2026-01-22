package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Service to calculate the fare of a parking ticket.
 * The fare is calculated based on the parking type and duration.
 * The rates are defined in the Fare class.
 * The duration is calculated in hours.
 * The time is provided in milliseconds.
 * if the time is inferior to 30 min the prices should be 0
 */
public class FareCalculatorService {
    /**
     * Calculate the fare for a given ticket.
     *
     * @param ticket
     * @param discount
     */
    public void calculateFare(Ticket ticket, boolean discount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();


        float duration = (outHour - inHour);
        duration = duration / (1000 * 60 * 60); // convert milliseconds to hours

        double fare;

        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                fare = Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                fare = Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }



        BigDecimal rawPrice = new BigDecimal (0);

        if (duration > 0.5) {
            rawPrice = BigDecimal.valueOf(duration * fare).setScale(2, RoundingMode.HALF_UP);

            if (discount) {
                rawPrice = rawPrice.multiply(BigDecimal.valueOf(0.95)).setScale(2, RoundingMode.HALF_UP);
            }
        }

        double price = rawPrice.doubleValue();
        ticket.setPrice(price);


    }

    /**
     * Calculate the fare for a given ticket without discount.
     *
     * @param ticket
     */
    public void calculateFare(Ticket ticket) {
        calculateFare(ticket, false);
    }
}