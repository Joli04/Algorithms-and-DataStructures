package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class TrafficTrackerTest2 {

    private TrafficTracker trafficTracker;
    private final static String VAULT_NAME = "/2022-09";

    @BeforeEach
    public void setup() {
        trafficTracker = new TrafficTracker();

        trafficTracker.importCarsFromVault(VAULT_NAME + "/cars.txt");

        trafficTracker.importDetectionsFromVault(VAULT_NAME + "/detections");
    }

    @DisplayName("Total fines")
    @Test
    public void calculateFines() {
        System.out.println(trafficTracker.calculateTotalFines());
        TrafficTracker trafficTracker = new TrafficTracker();
        // no fines
        assertEquals(0, trafficTracker.calculateTotalFines());

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
