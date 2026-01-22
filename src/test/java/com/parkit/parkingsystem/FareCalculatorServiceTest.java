package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.stream.Stream;

/**
 * Unit tests for FareCalculatorService class.
 * Tests cover fare calculation for different vehicle types and parking durations.
 * Includes tests for edge cases such as future in-time and unknown parking types.
 * @see FareCalculatorService
 */
class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    void setUpPerTest() {
        ticket = new Ticket();
    }

/**
    private static Stream<Arguments> fareArgumentsProvider() {
        return Stream.of(
                Arguments.of(30, ParkingType.CAR, true, 0.0),
                Arguments.of(30, ParkingType.CAR, false, 0.0),
                Arguments.of(30, ParkingType.BIKE, true, 0.0),
                Arguments.of(30, ParkingType.BIKE, false, 0.0),
                Arguments.of(45, ParkingType.CAR, true, 0.75 * Fare.CAR_RATE_PER_HOUR * 0.95),
                Arguments.of(45, ParkingType.CAR, false, 0.75 * Fare.CAR_RATE_PER_HOUR),
                Arguments.of(45, ParkingType.BIKE, true, 0.75 * Fare.BIKE_RATE_PER_HOUR * 0.95),
                Arguments.of(45, ParkingType.BIKE, false, 0.75 * Fare.BIKE_RATE_PER_HOUR),
                Arguments.of(60, ParkingType.CAR, true, Fare.CAR_RATE_PER_HOUR * 0.95),
                Arguments.of(60, ParkingType.CAR, false, Fare.CAR_RATE_PER_HOUR),
                Arguments.of(60, ParkingType.BIKE, true, Fare.BIKE_RATE_PER_HOUR * 0.95),
                Arguments.of(60, ParkingType.BIKE, false, Fare.BIKE_RATE_PER_HOUR),
                Arguments.of(1440, ParkingType.CAR, true, 24 * Fare.CAR_RATE_PER_HOUR * 0.95),
                Arguments.of(1440, ParkingType.CAR, false, 24 * Fare.CAR_RATE_PER_HOUR),
                Arguments.of(1440, ParkingType.BIKE, true, 24 * Fare.BIKE_RATE_PER_HOUR * 0.95),
                Arguments.of(1440, ParkingType.BIKE, false, 24 * Fare.BIKE_RATE_PER_HOUR)
        );
    }

    @ParameterizedTest
    @MethodSource("fareArgumentsProvider")
    void calculateFare(int duration, ParkingType parkingType, boolean discount, double expectedPrice){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  duration * 60_000L) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, parkingType,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket, discount);
        assertEquals(expectedPrice, ticket.getPrice(),0.01);
    }

**/

    @Test
    void calculateFareCar(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        BigDecimal rawExpectedFare=new BigDecimal(Fare.CAR_RATE_PER_HOUR).setScale(2, RoundingMode.HALF_UP);
        double expectedFare=rawExpectedFare.doubleValue();
        assertEquals(expectedFare, ticket.getPrice());

    }

    @Test
    void calculateFareBike(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        BigDecimal rawExpectedFare=new BigDecimal(Fare.BIKE_RATE_PER_HOUR).setScale(2, RoundingMode.HALF_UP);
        double expectedFare=rawExpectedFare.doubleValue();
        assertEquals(expectedFare, ticket.getPrice());
    }

    @Test
    void calculateFareUnknownType(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        BigDecimal rawExpectedFare=new BigDecimal(Fare.BIKE_RATE_PER_HOUR).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedFareBD=rawExpectedFare.multiply(new BigDecimal("0.75")).setScale(2, RoundingMode.HALF_UP);
        double expectedFare=expectedFareBD.doubleValue();
        assertEquals(expectedFare, ticket.getPrice());


    }

    @Test
    void calculateFareCarWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        BigDecimal rawExpectedFare=new BigDecimal(Fare.CAR_RATE_PER_HOUR).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedFareBD=rawExpectedFare.multiply(new BigDecimal("0.75")).setScale(2, RoundingMode.HALF_UP);
        double expectedFare=expectedFareBD.doubleValue();
        assertEquals(expectedFare, ticket.getPrice());


    }

    @Test
    void calculateFareCarWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( (24 * Fare.CAR_RATE_PER_HOUR) , ticket.getPrice());
    }
    @Test
    void calculateFareCarWithLessThan30MinutesParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  30 * 60 * 1000) );//30 minutes parking time should be free
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( 0 , ticket.getPrice());
    }
    @Test
    void calculateFairBikeWithLessThan30MinutesParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  30 * 60 * 1000) );//30 minutes parking time should be free
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals( 0 , ticket.getPrice());

    }
    @Test
    void calculateFairCarWithDiscount(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket,true);
        BigDecimal rawExpectedFare=new BigDecimal((Fare.CAR_RATE_PER_HOUR)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discountedFare=rawExpectedFare.multiply(new BigDecimal("0.95")).setScale(2, RoundingMode.HALF_UP);
        double expectedFare=discountedFare.doubleValue();
        assertEquals(expectedFare, ticket.getPrice());



    }
    @Test
    void calculateFairBikeWithDiscount(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket,true);
        assertEquals( Fare.BIKE_RATE_PER_HOUR * 0.95 , ticket.getPrice());


    }

}
