package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.PathUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SummaryTest {

    private Election election;

    @BeforeEach
    void setup() throws XMLStreamException, IOException {
        election = Election.importFromDataFolder(PathUtils.getResourcePath("/EML_bestanden_TK2021_HvA_UvA"));
    }



    @Test
    @DisplayName("numberOfCandidatesInPartyOutput")
    void shouldContainCorrectNumberOfCandidatesInPartyId3() {
        assertTrue(election.prepareSummary(3).contains("Total number of candidates = 66"));
    }

    @Test
    @DisplayName("numberOfRegistrationInPartyOutput")
    void shouldContainCorrectNumberOfRegistrationInPartyId16() {
        assertTrue(election.prepareSummary(16).contains("Total number of registrations = 100"));
    }

    @Test
    @DisplayName("numberOfRegistrationInPartyOutput")
    void shouldContainCorrectNumberOfRegistrationInPartyId4() {
        assertTrue(election.prepareSummary(4).contains("Total number of registrations = 160"));
    }
}
