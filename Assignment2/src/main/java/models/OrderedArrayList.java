package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class OrderedArrayList<E>
        extends ArrayList<E>
        implements OrderedList<E> {

    protected Comparator<? super E> ordening;   // the comparator that has been used with the latest sort
    protected int nSorted;                      // the number of sorted items in the first section of the list
    // representation-invariant
    //      all items at index positions 0 <= index < nSorted have been ordered by the given ordening comparator
    //      other items at index position nSorted <= index < size() can be in any order amongst themselves
    //              and also relative to the sorted section

    public OrderedArrayList() {
        this(null);
    }

    public OrderedArrayList(Comparator<? super E> ordening) {
        super();
        this.ordening = ordening;
        this.nSorted = 0;
    }

    public Comparator<? super E> getOrdening() {
        return this.ordening;
    }

    @Override
    public void clear() {
        super.clear();
        this.nSorted = 0;
    }

    @Override
    public void sort(Comparator<? super E> c) {
        super.sort(c);
        this.ordening = c;
        this.nSorted = this.size();
    }

    @Override
    public void add(int index, E item) {
        super.add(index, item);
        if(index <= nSorted){
            nSorted = index;
        }
    }

    @Override
    public E remove(int index) {
        E removed = super.remove(index);

        if (index <= nSorted) {
            this.nSorted--;
        }
        return removed;
    }

    @Override
    public boolean remove(Object obj) {
        boolean removed = super.remove(obj);

        if (removed) {
            this.nSorted--;
        }
        return removed;
    }

    @Override
    public void sort() {
        if (this.nSorted < this.size()) {
            this.sort(this.ordening);
        }
    }

    @Override
    public int indexOf(Object item) {
        // efficient search can be done only if you have provided an ordening for the list
        if (this.getOrdening() != null) {
            return indexOfByIterativeBinarySearch((E) item);
        } else {
            return super.indexOf(item);
        }
    }

    @Override
    public int indexOfByBinarySearch(E searchItem) {
        if (searchItem != null) {
            // some arbitrary choice to use the iterative or the recursive version
            return indexOfByRecursiveBinarySearch(searchItem);
        } else {
            return -1;
        }
    }

    /**
     * finds the position of the searchItem by an iterative binary search algorithm in the
     * sorted section of the arrayList, using the this.ordening comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.ordening comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for ordening items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.ordening
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByIterativeBinarySearch(E searchItem) {
        int firstPositionIndex = 0;
        int lastPositionIndex = nSorted - 1;

        while (firstPositionIndex <= lastPositionIndex) {
            int middlePositionIndex = firstPositionIndex + (lastPositionIndex - firstPositionIndex) / 2;

            if (ordening.compare(this.get(middlePositionIndex), searchItem) == 0) {
                return middlePositionIndex;
            } else if (ordening.compare(this.get(middlePositionIndex), searchItem) < 0) {
                firstPositionIndex = middlePositionIndex + 1;
            } else {
                lastPositionIndex = middlePositionIndex - 1;
            }
        }
        for (int i = nSorted; i < this.size(); i++) {
            if (ordening.compare(this.get(i), searchItem) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * finds the position of the searchItem by a recursive binary search algorithm in the
     * sorted section of the arrayList, using the this.ordening comparator for comparison and equality test.
     * If the item is not found in the sorted section, the unsorted section of the arrayList shall be searched by linear search.
     * The found item shall yield a 0 result from the this.ordening comparator, and that need not to be in agreement with the .equals test.
     * Here we follow the comparator for ordening items and for deciding on equality.
     *
     * @param searchItem the item to be searched on the basis of comparison by this.ordening
     * @return the position index of the found item in the arrayList, or -1 if no item matches the search item.
     */
    public int indexOfByRecursiveBinarySearch(E searchItem) {
        int firstPositionIndex = 0;
        int lastPositionIndex = nSorted - 1;
        if (lastPositionIndex >= firstPositionIndex) {
            int middlePositionIndex = firstPositionIndex + (lastPositionIndex - firstPositionIndex) / 2;

            if (ordening.compare(this.get(middlePositionIndex), searchItem) == 0) {
                return middlePositionIndex;
            } else if (ordening.compare(this.get(middlePositionIndex), searchItem) > 0) {
                return recursiveHelperMethod(firstPositionIndex, middlePositionIndex - 1, searchItem);
            } else {
                return recursiveHelperMethod(middlePositionIndex + 1, lastPositionIndex, searchItem);
            }
        }
        for (int i = nSorted; i < this.size(); i++) {
            if (ordening.compare(this.get(i), searchItem) == 0) {
                return i;
            }
        }
        return -1;
    }


    /**
     * helper method
     * @param firstPositionIndex first index of the sequence
     * @param lastPositionIndex last index of the sequence
     * @param searchItem search for the item
     * @return index of the searchItem
     */
    public int recursiveHelperMethod(int firstPositionIndex, int lastPositionIndex, E searchItem) {
        if (lastPositionIndex >= firstPositionIndex) {
            int middlePositionIndex = firstPositionIndex + (lastPositionIndex - firstPositionIndex) / 2;
            // comparing the items to determine the index
            if (ordening.compare(this.get(middlePositionIndex), searchItem) == 0) {
                return middlePositionIndex;
            } else if (ordening.compare(this.get(middlePositionIndex), searchItem) > 0) {
                return recursiveHelperMethod(firstPositionIndex, middlePositionIndex - 1, searchItem);
            } else {
                return recursiveHelperMethod(middlePositionIndex + 1, lastPositionIndex, searchItem);
            }
        }
        for (int i = nSorted; i < this.size(); i++) {
            if (ordening.compare(this.get(i), searchItem) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * finds a match of newItem in the list and applies the merger operator with the newItem to that match
     * i.e. the found match is replaced by the outcome of the merge between the match and the newItem
     * If no match is found in the list, the newItem is added to the list.
     *
     * @param newItem
     * @param merger  a function that takes two items and returns an item that contains the merged content of
     *                the two items according to some merging rule.
     *                e.g. a merger could   add the value of attribute X of the second item
     *                to attribute X of the first item and then return the first item
     * @return whether a new item was added to the list or not
     */
    @Override
    public boolean merge(E newItem, BinaryOperator<E> merger) {
        if (newItem == null) return false;
        int matchedItemIndex = this.indexOfByRecursiveBinarySearch(newItem);

        if (matchedItemIndex < 0) {
            this.add(newItem);
            return true;
        } else {
            E merged = merger.apply(this.get(matchedItemIndex), newItem);
            this.set(matchedItemIndex, merged);
            return false;
        }
    }
    /**
     * calculates the total sum of contributions of all items in the list
     *
     * @param mapper a function that calculates the contribution of a single item
     * @return the total sum of all contributions
     */
    @Override
    public double aggregate(Function<E, Double> mapper) {
        double sum = 0.0;
        for (E item : this) {
            sum += mapper.apply(item);
        }
        return sum;
    }
}
