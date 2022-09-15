package models;

import java.util.LinkedList;

public class Train {
    private final String origin;
    private final String destination;
    private final Locomotive engine;
    private Wagon firstWagon;
    private LinkedList<Wagon> wagons;

    /* Representation invariants:
        firstWagon == null || firstWagon.previousWagon == null
        engine != null
     */

    public Train(Locomotive engine, String origin, String destination) {
        this.engine = engine;
        this.destination = destination;
        this.origin = origin;
        wagons = new LinkedList<>();
    }

    public LinkedList<Wagon> getWagons() {
        return wagons;
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
//        if(wagons.size() >0 && (wagon instanceof PassengerWagon && isPassengerTrain()) ||
//                wagon instanceof FreightWagon && isFreightTrain()){
//            return;
//        }
        firstWagon = wagon;
        wagons.add(firstWagon);
    }

    /**
     * @return the number of Wagons connected to the train
     */
    public int getNumberOfWagons() {
        return wagons.size();
    }

    /**
     * @return the last wagon attached to the train
     */
    public Wagon getLastWagonAttached() {

        if(wagons.size() == 0){
            return null;
        }
        return wagons.getLast();
    }

    /**
     * @return the total number of seats on a passenger train
     * (return 0 for a freight train)
     */
    public int getTotalNumberOfSeats() {
        int totalSeats = 0;
        for (int i = 0; i < wagons.size(); i++) {
            if (wagons.get(i) instanceof PassengerWagon) {
                totalSeats += ((PassengerWagon) wagons.get(i)).getNumberOfSeats();
            }
        }

        return totalSeats;
    }

    /**
     * calculates the total maximum weight of a freight train
     *
     * @return the total maximum weight of a freight train
     * (return 0 for a passenger train)
     */
    public int getTotalMaxWeight() {
        int totalWeight = 0;
        for (int i = 0; i < wagons.size(); i++) {
            if (wagons.get(i) instanceof FreightWagon) {
                totalWeight += ((FreightWagon) wagons.get(i)).getMaxWeight();
            }
        }
        return totalWeight;
    }

    /**
     * Finds the wagon at the given position (starting at 1 for the first wagon of the train)
     *
     * @param position
     * @return the wagon found at the given position
     * (return null if the position is not valid for this train)
     */
    public Wagon findWagonAtPosition(int position) {

        if (position > wagons.size())
            return null;

        return wagons.get(position);
    }

    /**
     * Finds the wagon with a given wagonId
     *
     * @param wagonId
     * @return the wagon found
     * (return null if no wagon was found with the given wagonId)
     */
    public Wagon findWagonById(int wagonId) {
        for (int i = 0; i < wagons.size(); i++) {
            if (wagons.get(i).getId() == wagonId) {
                return wagons.get(i);
            }
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

        for (int i = 0; i < wagons.size(); i++) {
            if (wagon instanceof PassengerWagon || !(wagons.get(i) instanceof FreightWagon)) {
                if (wagons.size() <= engine.getMaxWagons()) {
                    if (!wagons.contains(wagon)) {
                        return true;
                    }
                }
            } else if (wagon instanceof FreightWagon || !(wagons.get(i) instanceof PassengerWagon)) {
                if (wagons.size() <= engine.getMaxWagons()) {
                    if (!wagons.contains(wagon)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
        if (wagons.size() > 0 && wagons.size() < engine.getMaxWagons()) {
            if (wagon instanceof PassengerWagon && isPassengerTrain()||
                    wagon instanceof FreightWagon && isFreightTrain()) {
                wagons.addLast(wagon);
                return true;
            }
        }
        if (wagons.size() == 0) {
            wagons.addLast(wagon);
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
        if (wagons.size() > 0 && wagons.size() < engine.getMaxWagons()) {
            if ((wagon instanceof PassengerWagon && isPassengerTrain()) ||
                    (wagon instanceof FreightWagon && isFreightTrain())) {
                wagons.addFirst(wagon);
                return true;
            }
        } else if (wagons.size() == 0) {
            wagons.addFirst(wagon);
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
        if (wagons.size() < engine.getMaxWagons() && wagons.size() > 0 ||
                wagon instanceof PassengerWagon && isPassengerTrain() ||
                wagon instanceof FreightWagon &&isFreightTrain()) {
            wagons.add(position, wagon);
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
        for (int i = position + 1; i < wagons.size(); i++) {
            wagons.removeLast();
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
        // TODO

    }

    // TODO string representation of a train

    @Override

    public String toString() {
        return engine + " with " + wagons.size() + " wagons from " + origin + " to " + destination;
    }
}
