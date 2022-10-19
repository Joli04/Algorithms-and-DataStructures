package models;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

public class TrafficTracker {
    private final String TRAFFIC_FILE_EXTENSION = ".txt";
    private final String TRAFFIC_FILE_PATTERN = ".+\\" + TRAFFIC_FILE_EXTENSION;

    private OrderedList<Car> cars;                  // the reference list of all known Cars registered by the RDW
    private OrderedList<Violation> violations;      // the accumulation of all offences by car and by city

    public TrafficTracker() {
        cars = new OrderedArrayList<>(Comparator.comparing(Car::getLicensePlate));
        violations = new OrderedArrayList<>(Violation::compareByLicensePlateAndCity);
    }

    //A method we created for our test in TrafficTrackerTest2
    public void add(Violation violation){
        violations.add(violation);
    }

    /**
     * imports all registered cars from a resource file that has been provided by the RDW
     *
     * @param resourceName
     */
    public void importCarsFromVault(String resourceName) {
        this.cars.clear();

        // load all cars from the text file
        int numberOfLines = importItemsFromFile(this.cars,
                createFileFromURL(TrafficTracker.class.getResource(resourceName)),
                Car::fromLine);

        // sort the cars for efficient later retrieval
        this.cars.sort();

      System.out.printf("Imported %d cars from %d lines in %s.\n", this.cars.size(), numberOfLines, resourceName);
    }

    /**
     * imports and merges all raw detection data of all entry gates of all cities from the hierarchical file structure of the vault
     * accumulates any offences against purple rules into this.violations
     *
     * @param resourceName
     */
    public void importDetectionsFromVault(String resourceName) {
        this.violations.clear();

        int totalNumberOfOffences =
                this.mergeDetectionsFromVaultRecursively(
                        createFileFromURL(TrafficTracker.class.getResource(resourceName)));

        System.out.printf("Found %d offences among detections imported from files in %s.\n",
                totalNumberOfOffences, resourceName);
    }

    /**
     * traverses the detections vault recursively and processes every data file that it finds
     *
     * @param file
     */
    private int mergeDetectionsFromVaultRecursively(File file) {
        int totalNumberOfOffences = 0;

        if (file.isDirectory()) {
            // the file is a folder (a.k.a. directory)
            //  retrieve a list of all files and sub folders in this directory
            File[] filesInDirectory = Objects.requireNonNullElse(file.listFiles(), new File[0]);
            int directoryLength = filesInDirectory.length;

            if (directoryLength > 0) {
                for (File f : filesInDirectory) {
                    totalNumberOfOffences += mergeDetectionsFromVaultRecursively(f);
                }
            }

        } else if (file.getName().matches(TRAFFIC_FILE_PATTERN)) {
            // the file is a regular file that matches the target pattern for raw detection files
            // process the content of this file and merge the offences found into this.violations
            totalNumberOfOffences += this.mergeDetectionsFromFile(file);
        }

        return totalNumberOfOffences;
    }

    /**
     * imports another batch detection data from the filePath text file
     * and merges the offences into the earlier imported and accumulated violations
     *
     * @param file
     */
    private int mergeDetectionsFromFile(File file) {

        // re-sort the accumulated violations for efficient searching and merging
        this.violations.sort();

        // use a regular ArrayList to load the raw detection info from the file
        List<Detection> newDetections = new ArrayList<>();

        importItemsFromFile(newDetections, file, s -> Detection.fromLine(s, this.cars));

        System.out.printf("Imported %d detections from %s.\n", newDetections.size(), file.getPath());

        int totalNumberOfOffences = 0; // tracks the number of offences that emerges from the data in this file

        for (Detection detection : newDetections) {
            Violation violation = detection.validatePurple();
            if (violation != null) {
                this.violations.merge(violation, Violation::combineOffencesCounts);
                totalNumberOfOffences++;
            }
        }

        return totalNumberOfOffences;
    }

    /**
     * calculates the total revenue of fines from all violations,
     * Trucks pay €25 per offence, Coaches €35 per offence
     *
     * @return the total amount of money recovered from all violations
     */
    public double calculateTotalFines() {
        return this.violations.aggregate( violation -> {
            final double truckOffence = 25;
            final double coachOffence = 35;
            boolean isCoach = violation.getCar().getCarType().equals(Car.CarType.Coach);
            boolean isTruck = violation.getCar().getCarType().equals(Car.CarType.Truck);

            if(isTruck){
                return violation.getOffencesCount() * truckOffence;
            } else if (isCoach) {
                return violation.getOffencesCount() * coachOffence;
            }
            return 0.0;
        });
    }

    /**
     * exception handler for topViolationsByCar() and topViolationsByCity()
     *
     * @param topNumber           int
     * @param list List
     * @return (sub)List
     */
    private List<Violation> exceptionHandler(int topNumber, OrderedArrayList<Violation> list) {
            //We call our selfmade method and pass the ordered arrayList
            comparingTopOffences(list);
            String exceptionMessage = String.format("%d is not valid (0-%d)", topNumber, list.size());
            if (topNumber > list.size()) {
                throw new IndexOutOfBoundsException(exceptionMessage);
            } else if (topNumber < 0) {
                throw new IllegalArgumentException(exceptionMessage);
            }
            return list.subList(0, topNumber);
        }

    /**
     * Prepares a list of topNumber of violations that show the highest offencesCount
     * when this.violations are aggregated by car across all cities.
     *
     * @param topNumber the requested top number of violations in the result list
     * @return a list of topNum items that provides the top aggregated violations
     */
    public List<Violation> topViolationsByCar(int topNumber) {

        //Make an List that holds violations
        OrderedArrayList<Violation> violationsByCarList = new OrderedArrayList<>((o1, o2) -> {
            //We compare the two Cars and if they are the same, we add the counts of both violations with each other.
            int resultComparing = o1.getCar().compareTo(o2.getCar());
            if (resultComparing == 0) {
                o1.combineOffencesCounts(o2);
            }
            return resultComparing;
        });
        //We call our function that merges and sorts the list.
        return exceptionHandler(topNumber, violationsByCarList);
    }

    /**
     * Prepares a list of topNumber of violations that show the highest offencesCount
     * when this.violations are aggregated by city across all cars.
     *
     * @param topNumber the requested top number of violations in the result list
     * @return a list of topNum items that provides the top aggregated violations
     */
    public List<Violation> topViolationsByCity(int topNumber) {

        OrderedArrayList<Violation> violationsByCityList = new OrderedArrayList<>((o1, o2) -> {
            int resultComparing = o1.getCity().compareTo(o2.getCity());
            if (resultComparing == 0)
                o1.combineOffencesCounts(o2);
            return resultComparing;
        });

        return exceptionHandler(topNumber, violationsByCityList);
    }

    public void comparingTopOffences(OrderedArrayList<Violation> list) {
        //We loop through all the violations in an violations list and merge them with the given List.
        for (Violation violation : this.violations) {
            list.merge(violation, Violation::combineOffencesCounts);
        }

        //We make a Comparator where we compare all the offences count.
        Comparator<Violation> violationOffencesComparator = Comparator.comparing(Violation::getOffencesCount);
        //We then make a reversed Comparator that reverses the previous comparator.
        Comparator<Violation> violationReversedComparator = violationOffencesComparator.reversed();
        // We sort our given list with the reverse Comparator that we made
        list.sort(violationReversedComparator);
    }

    /**
     * imports a collection of items from a text file which provides one line for each item
     *
     * @param items     the list to which imported items shall be added
     * @param file      the source text file
     * @param converter a function that can convert a text line into a new item instance
     * @param <E>       the (generic) type of each item
     */
    public static <E> int importItemsFromFile(List<E> items, File file, Function<String, E> converter) {
        int numberOfLines = 0;

        Scanner scanner = createFileScanner(file);

        // read all source lines from the scanner,
        // convert each line to an item of type E
        // and add each successfully converted item into the list
        while (scanner.hasNext()) {
            // input another line with author information
            String line = scanner.nextLine();
            numberOfLines++;
            items.add(converter.apply(line));
        }

        return numberOfLines;
    }

    /**
     * helper method to create a scanner on a file and handle the exception
     *
     * @param file
     * @return
     */
    private static Scanner createFileScanner(File file) {
        try {
            return new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFound exception on path: " + file.getPath());
        }
    }

    private static File createFileFromURL(URL url) {
        try {
            return new File(url.toURI().getPath());
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI syntax error found on URL: " + url.getPath());
        }
    }

    public OrderedList<Car> getCars() {
        return this.cars;
    }

    public OrderedList<Violation> getViolations() {
        return this.violations;
    }
}
