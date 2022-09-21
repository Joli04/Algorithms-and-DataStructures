package models;

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

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
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
        int count = 0;
        Wagon wagon = firstWagon;

        while (wagon != null) {
            wagon = wagon.getNextWagon();
            count++;
        }
        return count;
    }

    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {
        Wagon wagon = firstWagon;
        if (firstWagon == null) {
            return null;
        }

        while (wagon != null) {
            if (wagon.getNextWagon() == null) {
                return wagon;
            }
            wagon = wagon.getNextWagon();
        }
        return null;
    }

    /**
     * @return the total number of seats on a passenger train
     * (return 0 for a freight train)
     */
    public int getTotalNumberOfSeats() {
        Wagon wagon = firstWagon;
        int total = 0;
        while (wagon != null) {
            if (isPassengerTrain()) {
                total += ((PassengerWagon) wagon).getNumberOfSeats();
            }
            wagon = wagon.getNextWagon();
        }
        return total;
    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        Wagon wagon = firstWagon;
        int weight = 0;
        while (wagon != null) {
            if (isFreightTrain()) {
                weight += ((FreightWagon) wagon).getMaxWeight();
            }
            wagon = wagon.getNextWagon();
        }
        return weight;
    }

    /**
     * Finds the wagon at the given position (starting at 1 for the first wagon of the train)
     *
     * @param position
     * @return the wagon found at the given position
     * (return null if the position is not valid for this train)
     */
    public Wagon findWagonAtPosition(int position) {
        Wagon wagon = firstWagon;
        int count = 1;
        while (wagon != null) {

            if (count == position) {
                return wagon;
            }
            count++;
            wagon = wagon.getNextWagon();
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
        Wagon wagon = firstWagon;
        while (wagon != null) {
            if (wagon.getId() == wagonId) {
                return wagon;
            }
            wagon = wagon.getNextWagon();
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

        int wagonsToAttach = 0;
        Wagon wagon1 = wagon;
        Wagon wagon2 = firstWagon;

        while (wagon2 != null) {
            if (wagon2 == wagon) {
                return false;
            }
            wagon2 = wagon2.getNextWagon();
        }

        while (wagon1 != null) {
            wagonsToAttach++;
            wagon1 = wagon1.getNextWagon();
        }

        if (isFreightTrain() && wagon instanceof FreightWagon ||
                isPassengerTrain() && wagon instanceof PassengerWagon) {
            return engine.getMaxWagons() >= (wagonsToAttach + getNumberOfWagons());
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

        if (canAttach(wagon)) {
            wagon.getLastWagonAttached().attachTail(wagon);
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

//        if(firstWagon == null){
//            setFirstWagon(wagon);
//        }

        if (canAttach(wagon)) {
            wagon.reAttachTo(firstWagon);
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

            Wagon wagon2 = findWagonAtPosition(position);
            wagon.setNextWagon(wagon2.getNextWagon());
            wagon2.setNextWagon(wagon);
            wagon2.setPreviousWagon(wagon2.getPreviousWagon());

            return true;
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
        // TODO

        Wagon wagon = firstWagon;

        while(wagon != null){
            if(wagon.getId() == wagonId){
                wagon.setNextWagon(wagon.getNextWagon());
            }
            wagon = wagon.getNextWagon();
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
        // TODO toTrain
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
        // TODO
        Wagon head = firstWagon;
        Wagon current = null;
        Wagon next = head.getNextWagon();
        while (head.getNextWagon() != null) {
            head = head.getNextWagon();
            next = head.getPreviousWagon();
        }

//        if(temp != null){
//            head = temp.getPreviousWagon();
//        }


    }

    // TODO string representation of a train

    @Override

    public String toString() {
        return engine + " with " + " wagons from " + origin + " to " + destination;
    }
}
