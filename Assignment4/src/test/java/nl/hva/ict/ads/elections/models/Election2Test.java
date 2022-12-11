package nl.hva.ict.ads.elections.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

public class Election2Test {

    static Election election;
    private Candidate candidate12, candidate21, candidate21c, candidate12b;


    private Constituency constituency;
    private PollingStation pollingStation1, pollingStation2;


    @BeforeEach
    void setup() {
        Party party1 = new Party(1, "Party-1");
        Party party2 = new Party(2, "Party-2");
        this.candidate12 = new Candidate("B.", "van", "Candidate", party1);
        this.candidate12b = new Candidate("B.", "van", "Candidate", party2);
        this.candidate21 = new Candidate("A.", null, "Candidate", party2);
        this.candidate21c = new Candidate("A.", null, "Candidates", party2);

        this.constituency = new Constituency(0, "HvA");

        this.pollingStation1 = new PollingStation("WHB", "1091GH", "Wibauthuis");
        this.pollingStation2 = new PollingStation("LWB", "1097DZ", "Leeuwenburg");
        this.constituency.add(this.pollingStation1);
        this.constituency.add(this.pollingStation2);
    }


    @DisplayName("hashcode PartyId/FullName")
    @Test
    void hashCodeShallIdentifyByPartyIdAndCandidateName() {
        // check for the same fullName different party should not have the same hashcode
        assertNotEquals(this.candidate12.hashCode(), this.candidate12b.hashCode());

        // check same partyId different name
        assertNotEquals(this.candidate21.hashCode(), this.candidate21c.hashCode());
    }

    @Test
    void getPollingStationsByZipCodeRangeShouldReturnSome() {
        // what if we swap around the from and to values?
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            this.constituency.getPollingStationsByZipCodeRange("1091ZZ", "1091AA");
        });

        String expectedMessage = "firstzipcode cant be larger than lastZipCode";
        String actualMessage = exception.getMessage();

        assertEquals(actualMessage, expectedMessage);
    }

}
