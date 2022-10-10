package models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DetectionTest2 {

    Car ferrari, audi, bmw, mercedes, lambo, hyundai;
    List<Car> cars;

    @BeforeEach
    private void setup() {
        Locale.setDefault(Locale.ENGLISH);
        ferrari = new Car("32-DF-QR", 6, Car.CarType.Car, Car.FuelType.Gasoline, LocalDate.of(2014,1,31));
        audi = new Car("11-SS-AA", 4, Car.CarType.Truck, Car.FuelType.Diesel, LocalDate.of(1998,1,31));
        mercedes = new Car("VV-11-BB", 4, Car.CarType.Van, Car.FuelType.Diesel, LocalDate.of(1998,1,31));
        bmw = new Car("A-123-BB", 4, Car.CarType.Car, Car.FuelType.Gasoline, LocalDate.of(2019,1,31));
        lambo = new Car("1-TTT-99", 5, Car.CarType.Truck, Car.FuelType.Lpg, LocalDate.of(2011,1,31));
        hyundai = new Car("1-AAAA-0000");
        cars = new ArrayList<>(List.of(ferrari, audi, mercedes, bmw, lambo, hyundai));
    }


    @DisplayName("Validate Purple")
    @Test
    public void testForPurple(){
        Detection detection1 = Detection.fromLine(" 32-DF-QR , Amsterdam , 2022-10-01T12:11:10", cars);
        Detection detection2 = Detection.fromLine("11-SS-AA,Amsterdam,2022-10-01T12:11:10", cars);

        // Not detected return null
        assertNull(detection1.validatePurple());

        // Detected so not null
        assertNotNull(detection2.validatePurple());
    }
}
