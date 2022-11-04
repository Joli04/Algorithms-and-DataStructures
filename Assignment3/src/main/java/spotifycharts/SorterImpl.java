package spotifycharts;

import java.util.Comparator;
import java.util.List;

public class SorterImpl<E> implements Sorter<E> {

    /**
     * Sorts all items by selection or insertion sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     *
     * @param items
     * @param comparator
     * @return the items sorted in place
     */
    public List<E> selInsBubSort(List<E> items, Comparator<E> comparator) {
        // TODO implement selection sort or insertion sort or bubble sort

        for (int i = 0; i < items.size() - 1; i++) {
            for (int j = 0; j < items.size() - i - 1; j++) {
                if (comparator.compare(items.get(j), items.get(j + 1)) > 0) {
                    E temp = items.get(j);
                    items.set(j, items.get(j + 1));
                    items.set(j + 1, temp);
                }
            }
        }


        return items;   // replace as you find appropriate
    }

    /**
     * Sorts all items by quick sort using the provided comparator
     * for deciding relative ordening of two items
     * Items are sorted 'in place' without use of an auxiliary list or array
     *
     * @param items
     * @param comparator
     * @return the items sorted in place
     */
    public List<E> quickSort(List<E> items, Comparator<E> comparator) {
        // TODO provide a recursive quickSort implementation,
        //  that is different from the example given in the lecture

        quickSort(items, 0, items.size() - 1, comparator);

        return items;   // replace as you find appropriate
    }

    /**
     * Overloaded method
     * recursive purposes
     * choose a pivot
     * partition by putting the smaller items to the left of the pivot and
     * the greater items to the right of the pivot (using comparator)
     *
     * @param items list to quicksort
     * @param lowIndex use for range
     * @param highIndex use for range
     * @param comparator comparing items at certain pointers with the pivot
     */
    private void quickSort(List<E> items, int lowIndex, int highIndex, Comparator<E> comparator) {
        // just return, only one element left, no need to sort
        if (lowIndex >= highIndex) {
            return;
        }
        E pivot = items.get(highIndex);

        int leftPointer = lowIndex;
        int rightPointer = highIndex;

        // loop till both cross paths
        while (leftPointer < rightPointer) {
            while (comparator.compare(items.get(leftPointer), pivot) <= 0 && leftPointer < rightPointer) {
                leftPointer++;
            }
            while (comparator.compare(items.get(rightPointer), pivot) >= 0 && leftPointer < rightPointer) {
                rightPointer--;
            }

            // at this point leftPointer is pointing to a larger number than the pivot
            // And the right pointer the other way around, so we swap
            swap(items, leftPointer, rightPointer);
        }
        // then we want to swap the pointer with the pivot
        // we use highIndex because we chose the last element as our pivot
        swap(items, leftPointer, highIndex);

        // sort the left side of the pivot, so leftPointer - 1
        quickSort(items, lowIndex, leftPointer - 1, comparator);

        // sort the right side of the pivot, leftPointer + 1
        quickSort(items, leftPointer + 1, highIndex, comparator);
    }

    /**
     * Helper method to swap the pointers
     * @param items
     * @param index1
     * @param index2
     */
    private void swap(List<E> items, int index1, int index2) {
        E temp = items.get(index1);
        items.set(index1, items.get(index2));
        items.set(index2, temp);
    }

    /**
     * Identifies the lead collection of numTops items according to the ordening criteria of comparator
     * and organizes and sorts this lead collection into the first numTops positions of the list
     * with use of (zero-based) heapSwim and heapSink operations.
     * The remaining items are kept in the tail of the list, in arbitrary order.
     * Items are sorted 'in place' without use of an auxiliary list or array or other positions in items
     *
     * @param numTops    the size of the lead collection of items to be found and sorted
     * @param items
     * @param comparator
     * @return the items list with its first numTops items sorted according to comparator
     * all other items >= any item in the lead collection
     */
    public List<E> topsHeapSort(int numTops, List<E> items, Comparator<E> comparator) {

        // the lead collection of numTops items will be organised into a (zero-based) heap structure
        // in the first numTops list positions using the reverseComparator for the heap condition.
        // that way the root of the heap will contain the worst item of the lead collection
        // which can be compared easily against other candidates from the remainder of the list
        Comparator<E> reverseComparator = comparator.reversed();

        // initialise the lead collection with the first numTops items in the list
        for (int heapSize = 2; heapSize <= numTops; heapSize++) {
            // repair the heap condition of items[0..heapSize-2] to include new item items[heapSize-1]
            heapSwim(items, heapSize, reverseComparator);
        }

        // insert remaining items into the lead collection as appropriate
        for (int i = numTops; i < items.size(); i++) {
            // loop-invariant: items[0..numTops-1] represents the current lead collection in a heap data structure
            //  the root of the heap is the currently trailing item in the lead collection,
            //  which will lose its membership if a better item is found from position i onwards
            E item = items.get(i);
            E worstLeadItem = items.get(0);
            if (comparator.compare(item, worstLeadItem) < 0) {
                // item < worstLeadItem, so shall be included in the lead collection
                items.set(0, item);
                // demote worstLeadItem back to the tail collection, at the orginal position of item
                items.set(i, worstLeadItem);
                // repair the heap condition of the lead collection
                heapSink(items, numTops, reverseComparator);
            }
        }

        // the first numTops positions of the list now contain the lead collection
        // the reverseComparator heap condition applies to this lead collection
        // now use heapSort to realise full ordening of this collection
        for (int i = numTops - 1; i > 0; i--) {
            // loop-invariant: items[i+1..numTops-1] contains the tail part of the sorted lead collection
            // position 0 holds the root item of a heap of size i+1 organised by reverseComparator
            // this root item is the worst item of the remaining front part of the lead collection

            // TODO swap item[0] and item[i];
            //  this moves item[0] to its designated position


            // TODO the new root may have violated the heap condition
            //  repair the heap condition on the remaining heap of size i


        }

        return items;
    }

    /**
     * Repairs the zero-based heap condition for items[heapSize-1] on the basis of the comparator
     * all items[0..heapSize-2] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     * all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     *
     * @param items
     * @param heapSize
     * @param comparator
     */
    protected void heapSwim(List<E> items, int heapSize, Comparator<E> comparator) {
        // TODO swim items[heapSize-1] up the heap until
        //      i==0 || items[(i-1]/2] <= items[i]


    }

    /**
     * Repairs the zero-based heap condition for its root items[0] on the basis of the comparator
     * all items[1..heapSize-1] are assumed to satisfy the heap condition
     * The zero-bases heap condition says:
     * all items[i] <= items[2*i+1] and items[i] <= items[2*i+2], if any
     * or equivalently:     all items[i] >= items[(i-1)/2]
     *
     * @param items
     * @param heapSize
     * @param comparator
     */
    protected void heapSink(List<E> items, int heapSize, Comparator<E> comparator) {
        // TODO sink items[0] down the heap until
        //      2*i+1>=heapSize || (items[i] <= items[2*i+1] && items[i] <= items[2*i+2])


    }
}
