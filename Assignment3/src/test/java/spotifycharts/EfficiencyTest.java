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


    @BeforeEach
    public void setup(){
        ChartsCalculator chartsCalculator = new ChartsCalculator(2L);
        this.songSorter = new SongSorter();
        test = new SorterImpl<>();
        fewSongs = new ArrayList<>(chartsCalculator.registerStreamedSongs(10));
        manySongs = new ArrayList<>(chartsCalculator.registerStreamedSongs(250));
    }
    @Test
    public void quicksortEfficiency(){

//        100, 200, 400,
//         800, 1600,
        List<Song> hundred = new ArrayList<>(fewSongs);
//        Collections.shuffle(hundred);

        System.out.println(hundred);
        System.gc();
        test.quickSort(hundred, Comparator.comparing(Song::getStreamsCountTotal));
        System.out.println(hundred);

    }
}
