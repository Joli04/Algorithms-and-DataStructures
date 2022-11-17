package spotifycharts;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class EfficiencyTest {

    private SongSorter songSorter;
    private SorterImpl<Song> test;
    private List<Song> fewSongs;
    private List<Song> manySongs;
    private Comparator<Song> rankingScheme = Song::compareByHighestStreamsCountTotal;
    private List<Song> heap;
    private Comparator<Song> heapComparator = Comparator.comparing(Song::getTitle);
    private List<Song> hundred;


    @BeforeEach
    public void setup() {
        ChartsCalculator chartsCalculator = new ChartsCalculator(1L);
        this.songSorter = new SongSorter();
        test = new SorterImpl<>();
        fewSongs = new ArrayList<>(chartsCalculator.registerStreamedSongs(100));
        hundred = new ArrayList<>(fewSongs);
//        manySongs = new ArrayList<>(chartsCalculator.registerStreamedSongs(2));
    }


//    public void quicksortEfficiency() {
//        System.gc();
//        test.quickSort(hundred, Comparator.comparing(Song::getStreamsCountTotal));
//    }

    public void bubbleEfficiency() {
        System.gc();
        test.selInsBubSort(hundred, Comparator.comparing(Song::getStreamsCountTotal));
    }

//    @Test
//    public void test() {
//        long start = System.nanoTime();
//        quicksortEfficiency();
//        long end = System.nanoTime();
//        long duration = end - start;
//        System.out.println((double)duration*Math.pow(10,-9));
//
//    }
    @Test
    public void testBubble() {
        long start = System.nanoTime();
        bubbleEfficiency();
        long end = System.nanoTime();
        long duration = end - start;
        System.out.println((double)duration*Math.pow(10,-9));

    }
}
