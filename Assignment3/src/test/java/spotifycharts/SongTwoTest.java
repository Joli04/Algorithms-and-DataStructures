package spotifycharts;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.*;

public class SongTwoTest {

    private static Comparator<Song> compareStreams;
    private static Comparator<Song> compareDutch;
    private Song song21, songKen, songAde, songMar;

    @BeforeAll
    static void setupClass() {
        compareStreams = Song::compareByHighestStreamsCountTotal;
        compareDutch = Song::compareForDutchNationalChart;
    }

    @BeforeEach
    public void setup() {
        song21 = new Song("21 Savage", "Gun Smoke", Song.Language.EN);
        song21.setStreamsCountOfCountry(Song.Country.NL, 150);
        song21.setStreamsCountOfCountry(Song.Country.SP, 100);
        song21.setStreamsCountOfCountry(Song.Country.BE, 11);
        song21.setStreamsCountOfCountry(Song.Country.DE, 30);
        song21.setStreamsCountOfCountry(Song.Country.FR, 120);
        song21.setStreamsCountOfCountry(Song.Country.UK, 250);

        songAde = new Song("Adelle", "Hello", Song.Language.EN);
        songAde.setStreamsCountOfCountry(Song.Country.UK, 300);
        songAde.setStreamsCountOfCountry(Song.Country.NL, 200);

        songKen = new Song("Kendrick Lamar", "Be Humble", Song.Language.EN);
        songKen.setStreamsCountOfCountry(Song.Country.SP, 20);
        songKen.setStreamsCountOfCountry(Song.Country.DE, 50);
        songKen.setStreamsCountOfCountry(Song.Country.FR, 40);
        songKen.setStreamsCountOfCountry(Song.Country.BE, 25);
        songKen.setStreamsCountOfCountry(Song.Country.UK, 300);
        songKen.setStreamsCountOfCountry(Song.Country.NL, 200);

        songMar = new Song("Maradonnie", "Snelle Planga", Song.Language.NL);
        songMar.setStreamsCountOfCountry(Song.Country.NL, 100);
        songMar.setStreamsCountOfCountry(Song.Country.BE, 40);


    }


    @Test
    public void amountStreamsTest() {
        // "Hello There!"
        assertEquals(500, songAde.getStreamsCountTotal());
    }

    @Test
    public void compareTest() {
        assertEquals(1, -compareStreams.compare(song21, songKen));

        songKen.setStreamsCountOfCountry(Song.Country.NL, 226);
        assertEquals(0, -compareStreams.compare(song21, songKen));

        songKen.setStreamsCountOfCountry(Song.Country.NL, 228);
        assertEquals(-1, -compareStreams.compare(song21, songKen));

        assertTrue(songMar.getStreamsCountTotal() < songKen.getStreamsCountTotal());
        // Dutch nationality songs ranked before all other songs in other languages
        assertEquals(-1, compareDutch.compare(songMar, songKen));

        // Verify stability of both comparators
        assertTrue(compareStreams.compare(song21, songKen) ==
                -compareStreams.compare(songKen, song21));

        assertTrue(compareDutch.compare(song21, songKen) ==
                -compareDutch.compare(songKen, song21));

    }


    @DisplayName("Format test")
    @Test
    void toStringFormatsCorrectly() {
        assertEquals("21 Savage/Gun Smoke{EN}(661)", song21.toString());
        assertEquals("Kendrick Lamar/Be Humble{EN}(635)", songKen.toString());
    }
}
