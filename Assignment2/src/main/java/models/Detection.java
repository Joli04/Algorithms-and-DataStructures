package models;

import java.time.LocalDateTime;
import java.util.List;

import static models.Car.CarType;
import static models.Car.FuelType;

public class Detection {
    private final Car car;                  // the car that was detected
    private final String city;              // the name of the city where the detector was located
    private final LocalDateTime dateTime;   // date and time of the detection event

    /* Representation Invariant:
     *      every Detection shall be associated with a valid Car
     */

    public Detection(Car car, String city, LocalDateTime dateTime) {
        this.car = car;
        this.city = city;
        this.dateTime = dateTime;
    }

    /**
     * Parses detection information from a line of text about a car that has entered an environmentally controlled zone
     * of a specified city.
     * the format of the text line is: lisensePlate, city, dateTime
     * The licensePlate shall be matched with a car from the provided list.
     * If no matching car can be found, a new Car shall be instantiated with the given lisensePlate and added to the list
     * (besides the license plate number there will be no other information available about this car)
     *
     * @param textLine
     * @param cars     a list of known cars, ordered and searchable by licensePlate
     *                 (i.e. the indexOf method of the list shall only consider the lisensePlate when comparing cars)
     * @return a new Detection instance with the provided information
     * or null if the textLine is corrupt or incomplete
     */
    public static Detection fromLine(String textLine, List<Car> cars) {
        Detection detection;

        String text = textLine.replaceAll("\\s+", "");
        String[] splitText = text.split(",");

        String licensePlate = splitText[0];
        String city = splitText[1];
        String dateTime = splitText[2];

        int indexCar = cars.indexOf(new Car(licensePlate));
        if (indexCar != -1) {
            detection = new Detection(cars.get(indexCar), city, LocalDateTime.parse(dateTime));
        } else {
            Car car = new Car(licensePlate);
            cars.add(car);
            detection = new Detection(car, city, LocalDateTime.parse(dateTime));
        }

        return detection;
    }

    /**
     * Validates a detection against the purple conditions for entering an environmentally restricted zone
     * I.e.:
     * Diesel trucks and diesel coaches with an emission category of below 6 may not enter a purple zone
     *
     * @return a Violation instance if the detection saw an offence against the purple zone rule/
     * null if no offence was found.
     */
    public Violation validatePurple() {
        // TODO validate that diesel trucks and diesel coaches have an emission category of 6 or above
        final int minimumEmission = 6;
        // check if the car is a coach or truck
        // then if it's a diesel and below the specific emission category
        if (car.getCarType() == CarType.Coach || car.getCarType() == CarType.Truck) {
            if (car.getFuelType() == FuelType.Diesel && car.getEmissionCategory() < minimumEmission) {
                return new Violation(this.car, this.city);
            }
        }
        return null;
    }

    public Car getCar() {
        return car;
    }

    public String getCity() {
        return city;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }


    @Override
    public String toString() {
        return String.format("%s/%s/%s", car.getLicensePlate(), city, dateTime);
    }
}
