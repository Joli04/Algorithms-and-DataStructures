package models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrafficTrackerTest2 {

    @DisplayName("Total fines")
    @Test
    public void calculateFines(){
        Car hummer = new Car("1-AAA-02", 3, Car.CarType.Truck, Car.FuelType.Gasoline, LocalDate.of(2014,1,31));
        List<Car> cars = new ArrayList<>();
        TrafficTracker trafficTracker = new TrafficTracker();
        // no fines
        assertEquals(0,trafficTracker.calculateTotalFines());

        Violation violation = new Violation(hummer, "Amsterdam");
        Detection detection1 = Detection.fromLine("1-AAA-02,Amsterdam,2022-10-01T12:11:10", cars);
        trafficTracker.importDetectionsFromVault(("/2022-09/cars.txt"));

        System.out.println(trafficTracker.calculateTotalFines());
    }

    @DisplayName("Top violations by city")
    @Test
    public void violationsCity(){

    }

    @DisplayName("Top violations by car")
    @Test
    public void violationsCar(){

    }
}
