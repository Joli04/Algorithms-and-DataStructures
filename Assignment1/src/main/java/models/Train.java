package models;

import java.util.ArrayList;

/**
 * @author Joli-Coeur Weibolt and Vincent Ohr
 */
public class Train {
    private final String origin;
    private final String destination;
    private final Locomotive engine;
    private Wagon firstWagon;

    /* Representation invariants:
        firstWagon == null || firstWagon.previousWagon == null
        engine != null
     */

    public Train(Locomotive engine, String origin, String destination) {
        this.engine = engine;
        this.destination = destination;
        this.origin = origin;
    }

    /* three helper methods that are useful in other methods */
    public boolean hasWagons() {
        return firstWagon != null;
    }

    public boolean isPassengerTrain() {
        return firstWagon instanceof PassengerWagon;
    }

    public boolean isFreightTrain() {
        return firstWagon instanceof FreightWagon;
    }

    public Locomotive getEngine() {
        return engine;
    }

    public Wagon getFirstWagon() {
        return firstWagon;
    }

    /**
     * Replaces the current sequence of wagons (if any) in the train
     * by the given new sequence of wagons (if any)
     * (sustaining all representation invariants)
     *
     * @param wagon the first wagon of a sequence of wagons to be attached
     *              (can be null)
     */
    public void setFirstWagon(Wagon wagon) {
        firstWagon = wagon;
    }

    /**
     * @return the number of Wagons connected to the train
     */
    public int getNumberOfWagons() {
        int numberOfWagons = 0;
        Wagon wagon = firstWagon;

        while (wagon != null) {
            numberOfWagons++;
            wagon = wagon.getNextWagon();
        }
        return numberOfWagons;
    }

    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {
        Wagon currentWagon = firstWagon;
        if (firstWagon == null) {
            return null;
        }
        while (currentWagon != null) {
            if (currentWagon.getNextWagon() == null) {
                return currentWagon;
            }
            currentWagon = currentWagon.getNextWagon();
        }
        return null;
    }

    /**
     * @return the total number of seats on a passenger train
     * (return 0 for a freight train)
     */
    public int getTotalNumberOfSeats() {
        int totalNumberOfSeats = 0;
        Wagon currentWagon = firstWagon;

        while (currentWagon != null) {
            if (isPassengerTrain()) {
                totalNumberOfSeats += ((PassengerWagon) currentWagon).getNumberOfSeats();
            }
            currentWagon = currentWagon.getNextWagon();
        }
        return totalNumberOfSeats;
    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        int MaxWeight = 0;
        Wagon currentWagon = firstWagon;

        while (currentWagon != null) {
            if (isFreightTrain()) {
                MaxWeight += ((FreightWagon) currentWagon).getMaxWeight();
            }
            currentWagon = currentWagon.getNextWagon();
        }
        return MaxWeight;
    }

    /**
     * Finds the wagon at the given position (starting at 1 for the first wagon of the train)
     *
     * @param position
     * @return the wagon found at the given position
     * (return null if the position is not valid for this train)
     */
    public Wagon findWagonAtPosition(int position) {
        int wagonPosition = 1;
        Wagon currentWagon = firstWagon;

        while (currentWagon != null) {

            if (wagonPosition == position) {
                return currentWagon;
            }
            wagonPosition++;
            currentWagon = currentWagon.getNextWagon();
        }
        return null;
    }

    /**
     * Finds the wagon with a given wagonId
     *
     * @param wagonId
     * @return the wagon found
     * (return null if no wagon was found with the given wagonId)
     */
    public Wagon findWagonById(int wagonId) {
        Wagon currentWagon = firstWagon;

        while (currentWagon != null) {
            if (currentWagon.getId() == wagonId) {
                return currentWagon;
            }
            currentWagon = currentWagon.getNextWagon();
        }
        return null;
    }

    /**
     * Determines if the given sequence of wagons can be attached to this train
     * Verifies if the type of wagons match the type of train (Passenger or Freight)
     * Verifies that the capacity of the engine is sufficient to also pull the additional wagons
     * Verifies that the wagon is not part of the train already
     * Ignores the predecessors before the head wagon, if any
     *
     * @param wagon the head wagon of a sequence of wagons to consider for attachment
     * @return whether type and capacity of this train can accommodate attachment of the sequence
     */
    public boolean canAttach(Wagon wagon) {
        int amountOfWagons = 0;

        Wagon headOfAttachment = wagon;
        Wagon currentWagon = firstWagon;

        while (currentWagon != null) {
            if (currentWagon == wagon) {
                return false;
            }
            currentWagon = currentWagon.getNextWagon();
        }

        while (headOfAttachment != null) {
            amountOfWagons++;
            headOfAttachment = headOfAttachment.getNextWagon();
        }

        if (isFreightTrain() && wagon instanceof FreightWagon ||
                isPassengerTrain() && wagon instanceof PassengerWagon) {
            return engine.getMaxWagons() >= (amountOfWagons + getNumberOfWagons());
        } else {
            return firstWagon == null;
        }
    }

    /**
     * Tries to attach the given sequence of wagons to the rear of the train
     * No change is made if the attachment cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if attachment is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be attached
     * @return whether the attachment could be completed successfully
     */
    public boolean attachToRear(Wagon wagon) {

        if (firstWagon == null) {
            setFirstWagon(wagon);
            if (wagon.hasPreviousWagon()) {
                wagon.getPreviousWagon().setNextWagon(null);
            }
            return true;
        }
        if (canAttach(wagon)) {
            Wagon lastWagon = getLastWagonAttached();
            if (wagon.hasPreviousWagon()) {
                wagon.getPreviousWagon().setNextWagon(null);
            }
            wagon.setPreviousWagon(lastWagon);
            lastWagon.setNextWagon(wagon);

            return true;
        }
        return false;
    }

    /**
     * Tries to insert the given sequence of wagons at the front of the train
     * (the front is at position one, before the current first wagon, if any)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity)
     * if insertion is possible, the head wagon is first detached from its predecessors, if any
     *
     * @param wagon the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtFront(Wagon wagon) {
        Wagon currentWagon = firstWagon;
        Wagon lastWagon = wagon.getLastWagonAttached();

        if (firstWagon == null) {
            setFirstWagon(wagon);
            return true;
        }

        if (canAttach(wagon)) {
            if (wagon.getPreviousWagon() != null) {
                wagon.getPreviousWagon().setNextWagon(null);
                wagon.setPreviousWagon(null);
            }
            firstWagon = wagon;
            lastWagon.setNextWagon(currentWagon);
            currentWagon.setPreviousWagon(lastWagon);
            return true;
        }
        return false;
    }

    /**
     * Tries to insert the given sequence of wagons at/before the given position in the train.
     * (The current wagon at given position including all its successors shall then be reattached
     * after the last wagon of the given sequence.)
     * No change is made if the insertion cannot be made.
     * (when the sequence is not compatible or the engine has insufficient capacity
     * or the given position is not valid for insertion into this train)
     * if insertion is possible, the head wagon of the sequence is first detached from its predecessors, if any
     *
     * @param position the position where the head wagon and its successors shall be inserted
     *                 1 <= position <= numWagons + 1
     *                 (i.e. insertion immediately after the last wagon is also possible)
     * @param wagon    the head wagon of a sequence of wagons to be inserted
     * @return whether the insertion could be completed successfully
     */
    public boolean insertAtPosition(int position, Wagon wagon) {
        if (canAttach(wagon)) {
            wagon.detachFront();
            Wagon previousWagon;
            Wagon lastWagon = wagon.getLastWagonAttached();
            Wagon wagonAtPosition = findWagonAtPosition(position);

            if (!hasWagons() || position == 1) {
                setFirstWagon(wagon);
                if (wagonAtPosition != null) {
                    wagon.attachTail(wagonAtPosition);
                }
                return true;
            }

            if (wagonAtPosition == null) {
                getLastWagonAttached().attachTail(wagon);
                return true;
            }

            if (wagonAtPosition.hasPreviousWagon()) {
                previousWagon = wagonAtPosition.getPreviousWagon();
                wagonAtPosition.detachFront();
                previousWagon.attachTail(wagon);

                if (wagon.hasNextWagon()) {
                    lastWagon.attachTail(wagonAtPosition);
                } else {
                    wagon.attachTail(wagonAtPosition);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Tries to remove one Wagon with the given wagonId from this train
     * and attach it at the rear of the given toTrain
     * No change is made if the removal or attachment cannot be made
     * (when the wagon cannot be found, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param wagonId the id of the wagon to be removed
     * @param toTrain the train to which the wagon shall be attached
     *                toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean moveOneWagon(int wagonId, Train toTrain) {
        Wagon wagonById = findWagonById(wagonId);

        if (firstWagon == wagonById) {
            setFirstWagon(wagonById.getNextWagon());
        }

        if (wagonById != null) {
            Wagon nextWagon = wagonById.getNextWagon();
            Wagon previousWagon = wagonById.getPreviousWagon();

            if (toTrain.canAttach(wagonById)) {
                if (nextWagon != null) {
                    nextWagon.setPreviousWagon(previousWagon);
                }

                if (previousWagon != null) {
                    previousWagon.setNextWagon(nextWagon);
                }

                wagonById.setNextWagon(null);
                wagonById.setPreviousWagon(null);
                toTrain.attachToRear(wagonById);
                return true;
            }
        }
        return false;
    }

    /**
     * Tries to split this train before the wagon at given position and move the complete sequence
     * of wagons from the given position to the rear of toTrain.
     * No change is made if the split or re-attachment cannot be made
     * (when the position is not valid for this train, or the trains are not compatible
     * or the engine of toTrain has insufficient capacity)
     *
     * @param position 1 <= position <= numWagons
     * @param toTrain  the train to which the split sequence shall be attached
     *                 toTrain shall be different from this train
     * @return whether the move could be completed successfully
     */
    public boolean splitAtPosition(int position, Train toTrain) {
        Wagon wagonAtPosition = this.findWagonAtPosition(position);

        if (wagonAtPosition == null) {
            return false;
        }

        if (toTrain.canAttach(wagonAtPosition)) {
            if (position == 1) {
                this.firstWagon = null;
            }
            toTrain.attachToRear(wagonAtPosition);
            return true;
        }

        if (!toTrain.hasWagons()) {
            setFirstWagon(wagonAtPosition);
            wagonAtPosition.setPreviousWagon(null);
            return true;
        }
        return false;
    }

    /**
     * Reverses the sequence of wagons in this train (if any)
     * i.e. the last wagon becomes the first wagon
     * the previous wagon of the last wagon becomes the second wagon
     * etc.
     * (No change if the train has no wagons or only one wagon)
     */
    public void reverse() {
        Wagon firstWagon = this.firstWagon;
        Wagon lastWagon = this.getLastWagonAttached();

        if (!hasWagons() || getNumberOfWagons() <= 1) {
            return;
        }

        Wagon nextWagon = firstWagon.getNextWagon();
        while (!firstWagon.equals(lastWagon) && !nextWagon.equals(lastWagon)) {
            int previousWagonId = firstWagon.id;

            firstWagon.id = lastWagon.id;
            lastWagon.id = previousWagonId;
            firstWagon = firstWagon.getNextWagon();
            lastWagon = lastWagon.getPreviousWagon();
        }


    }
    @Override
    public String toString() {
        Wagon currentWagon = firstWagon;
        ArrayList<Wagon> wagons = new ArrayList<>();

        //loops through all wagons and add them tho the arraylist
        while (currentWagon != null) {
            wagons.add(currentWagon);
            currentWagon = currentWagon.getNextWagon();
        }
        //Makes the arrayList to string.
        String wagonsArrayToString = wagons.toString();
        //removes all whitespaces
        String removeAllWhiteSpace = wagonsArrayToString.replaceAll("\\s", "");
        //replaces all commas with whitespaces
        String replaceAllCommas = removeAllWhiteSpace.replace(',', ' ');
        //removes the first and last bracket from the string.
        String removeFirstAndLastBracket = replaceAllCommas.substring(
                replaceAllCommas.indexOf("[")+1, replaceAllCommas.lastIndexOf("]")
        );
        // prints the result
        String finalText = String.format(
                "%s %s with %d wagons from %s to %s", engine, removeFirstAndLastBracket,
                this.getNumberOfWagons(), origin, destination);

        return finalText;
    }
}
