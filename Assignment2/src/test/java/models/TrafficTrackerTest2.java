package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.time.LocalDate;
import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class TrafficTrackerTest2 {

    private TrafficTracker trafficTracker;
    private final static String VAULT_NAME = "/2022-09";

    private OrderedList<Violation> violations;
    private OrderedList<Car> cars;

    @BeforeEach
    public void setup() {
        trafficTracker = new TrafficTracker();

        trafficTracker.importCarsFromVault(VAULT_NAME + "/cars.txt");

        trafficTracker.importDetectionsFromVault(VAULT_NAME + "/detections");
        violations = new OrderedArrayList<>(Violation::compareByLicensePlateAndCity);

        cars =  new OrderedArrayList<>(Comparator.comparing(Car::getLicensePlate));
    }

    @DisplayName("Total fines")
    @Test
    public void calculateFines() {
        System.out.println(trafficTracker.calculateTotalFines());
        assertEquals(207540, trafficTracker.calculateTotalFines());

        Car carTruck = new Car("DCI-45-J",4, Car.CarType.Truck, Car.FuelType.Diesel,
                LocalDate.of(2010,9,12));

        Car carCoach = new Car("SUS-69-W",3, Car.CarType.Coach, Car.FuelType.Diesel,
                LocalDate.of(2011,12,12));

        Violation violation = new Violation(carTruck,"Amsterdam");
        TrafficTracker trafficTracker = new TrafficTracker();
        trafficTracker.add(violation);

        violation = new Violation(carCoach, "Rotterdam");
        TrafficTracker tracker = new TrafficTracker();
        tracker.add(violation);

        System.out.println(trafficTracker.calculateTotalFines());
        System.out.println(tracker.calculateTotalFines());
        // no fines
        assertEquals(25.0, trafficTracker.calculateTotalFines());
        assertEquals(35.0, tracker.calculateTotalFines());
//
//        double totalfines = 207540;
//        double truckOffence = 25;
//        double coachOffence = 35;
//        System.out.println(trafficTracker.calculateTotalFines());
//        assertEquals(totalfines, trafficTracker.calculateTotalFines());
//
//        Car carTruck = new Car("DCI-45-J",4, Car.CarType.Truck, Car.FuelType.Diesel,
//                LocalDate.of(2010,9,12));
//
//        Car ca = new Car("DCI-45-J",4, Car.CarType.Truck, Car.FuelType.Diesel,
//                LocalDate.of(2010,9,12));
//
//        Violation violation = new Violation(carTruck,"Amsterdam");
//        trafficTracker.add(violation);
//
//
//        System.out.println(trafficTracker.calculateTotalFines());
//
//
//        assertEquals(totalfines + truckOffence, trafficTracker.calculateTotalFines());
//
//
//        TrafficTracker tracker = new TrafficTracker();
//
//        Violation violationscootie;
//
//        for (int i = 0; i < 100; i++) {
//            Car scootie = new Car("SUS-41-A",3, Car.CarType.Coach, Car.FuelType.Diesel,
//                    LocalDate.of(2011,8,22));
//            scootie.setCarType();
//            cars.add(scootie);
//            violationscootie = new Violation(cars.get(i),"Rotterdam");
//            tracker.add(violationscootie);
//        }
//
//        assertEquals(coachOffence*100, tracker.calculateTotalFines());
//        System.out.println(tracker.calculateTotalFines());
    }

    public void exampleCar(){

    }
    @DisplayName("Top violations by city")
    @Test
    public void violationsCity() {
        // Number one
        assertEquals("[null/Amsterdam/1440]", trafficTracker.topViolationsByCity(1).toString());

        // if variable topNumber is larger than the size of the list
        Throwable exception = assertThrows(IndexOutOfBoundsException.class, () ->
                trafficTracker.topViolationsByCity(9)
        );
        // show exception message
        assertEquals("9 is not valid (0-6)", exception.getMessage());

        // if variable topNumber is smaller than 0
        exception = assertThrows(IllegalArgumentException.class, () ->
                trafficTracker.topViolationsByCity(-1)
        );
        // show exception message
        assertEquals("-1 is not valid (0-6)", exception.getMessage());

    }

    @DisplayName("Top violations by car")
    @Test
    public void violationsCar() {
        // Number one
        assertEquals("[69-WV-VS/null/225]", trafficTracker.topViolationsByCar(1).toString());

        // if variable topNumber is larger than the size of the list
        Throwable exception = assertThrows(IndexOutOfBoundsException.class, () ->
                trafficTracker.topViolationsByCar(100));

        // show exception message
        assertEquals("100 is not valid (0-38)", exception.getMessage());

        // if variable topNumber is smaller than 0
        exception = assertThrows(IllegalArgumentException.class, () ->
                trafficTracker.topViolationsByCar(-1));

        // show exception message
        assertEquals("-1 is not valid (0-38)", exception.getMessage());

    }
}
