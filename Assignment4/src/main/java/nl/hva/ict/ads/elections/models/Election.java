package nl.hva.ict.ads.elections.models;

import nl.hva.ict.ads.utils.PathUtils;
import nl.hva.ict.ads.utils.xml.XMLParser;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Holds all election data per consituency
 * Provides calculation methods for overall election results
 */
public class Election {

    private String name;

    // all (unique) parties in this election, organised by Id
    // will be build from the XML
    protected Map<Integer, Party> parties;

    // all (unique) constituencies in this election, identified by Id
    protected Set<Constituency> constituencies;

    public Election(String name) {
        this.name = name;
        this.parties = new HashMap<>();
        this.constituencies = new HashSet<>();
    }

    /**
     * finds all (unique) parties registered for this election
     *
     * @return all parties participating in at least one constituency, without duplicates
     */
    public Collection<Party> getParties() {
        return parties.values(); // replace by a proper outcome
    }

    /**
     * finds the party with a given Id
     *
     * @param Id
     * @return the party with given Id, or null if no such party exists.
     */
    public Party getParty(int Id) {
        return parties.get(Id); // replace by a proper outcome
    }

    public Set<? extends Constituency> getConstituencies() {
        return this.constituencies;
    }

    /**
     * finds all unique candidates across all parties across all constituencies
     * organised by increasing party-id and then by increasing candidate id.
     *
     * @return alle unique candidates organised in an ordered set.
     */
    public List<Candidate> getAllCandidates() {
        //Make an arraylist named candidates
        List<Candidate> allCandidates = new ArrayList<>();
        //We make an Treeset and sort it on Party id
        NavigableSet<Party> partiesSortedOnId = new TreeSet<>(Comparator.comparing(Party::getId));
        partiesSortedOnId.addAll(parties.values());

        //We loop through the set and add for each party all the candidates to our list
        partiesSortedOnId.forEach(party -> {
            allCandidates.addAll(party.getCandidates());
        });
        return allCandidates;
    }

    /**
     * Retrieve for the given party the number of Candidates that have been registered per Constituency
     *
     * @param party
     * @return
     */
    public Map<Constituency, Integer> numberOfRegistrationsByConstituency(Party party) {
        Map<Constituency, Integer> registeredCandidatesPerConstituency = new HashMap<>();

        constituencies.forEach(constituency -> {
            registeredCandidatesPerConstituency.put(constituency, constituency.getRankedCandidatesByParty().get(party).size());
        });
        return registeredCandidatesPerConstituency; // replace by a proper outcome
    }

    /**
     * Finds all Candidates that have a duplicate name against another candidate in the election
     * (can be in the same party or in another party)
     *
     * @return
     */
    public Set<Candidate> getCandidatesWithDuplicateNames() {
        //We make an String set of duplicate names
        Set<String> duplicates = this.parties.values().stream()
                .flatMap(x -> x.getCandidates().stream().map(Candidate::getFullName))
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()))
                .entrySet().stream().filter(x -> x.getValue() > 1)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        //We turn a set of candidates who have the same name as the people in our set.
        return this.parties
                .values()
                .stream()
                .flatMap(x -> x.getCandidates()
                        .stream()
                        .filter(y -> duplicates.contains(y.getFullName()))
                )
                .collect(Collectors.toSet());
    }

    /**
     * Retrieve from all constituencies the combined sub set of all polling stations that are located within the area of the specified zip codes
     * i.e. firstZipCode <= pollingStation.zipCode <= lastZipCode
     * All valid zip codes adhere to the pattern 'nnnnXX' with 1000 <= nnnn <= 9999 and 'AA' <= XX <= 'ZZ'
     *
     * @param firstZipCode
     * @param lastZipCode
     * @return the sub set of polling stations within the specified zipCode range
     */
    public Collection<PollingStation> getPollingStationsByZipCodeRange(String firstZipCode, String lastZipCode) {
        //We make an new treeset that sorts on zipcode
        NavigableSet<PollingStation> pollingstations = new TreeSet<>(Comparator.comparing(PollingStation::getZipCode));
        //We loop through the constituencies and add all the pollingstations between the given zipcodes
        constituencies.forEach(constituency -> {
            pollingstations.addAll(constituency.getPollingStationsByZipCodeRange(firstZipCode, lastZipCode));
        });
        return pollingstations; // replace by a proper outcome
    }

    /**
     * Retrieves per party the total number of votes across all candidates, constituencies and polling stations
     *
     * @return
     */
    public Map<Party, Integer> getVotesByParty() {
        Map<Party, Integer> votes = new HashMap<>();
        //We loop through the constituencies and count the all the votes per party
        constituencies.forEach(constituency -> {
            constituency.getVotesByParty().forEach((party, integer) -> votes.merge(party, integer, Integer::sum));
        });
        return votes;
    }

    /**
     * Retrieves per party the total number of votes across all candidates,
     * that were cast in one out of the given collection of polling stations.
     * This method is useful to prepare an election result for any sub-area of a Constituency.
     * Or to obtain statistics of special types of voting, e.g. by mail.
     *
     * @param pollingStations the polling stations that cover the sub-area of interest
     * @return
     */
    public Map<Party, Integer> getVotesByPartyAcrossPollingStations(Collection<PollingStation> pollingStations) {
        Map<Party, Integer> votesPerParty = new HashMap<>();
        //Loop through the pollingstations and all the votes per party
        pollingStations.forEach(pollingStation -> {
            pollingStation.getVotesByParty().forEach((party, integer) -> {
                votesPerParty.merge(party, integer, Integer::sum);
            });
        });

        return votesPerParty; // replace by a proper outcome
    }


    /**
     * Transforms and sorts decreasingly vote counts by party into votes percentages by party
     * The party with the highest vote count shall be ranked upfront
     * The votes percentage by party is calculated from  100.0 * partyVotes / totalVotes;
     *
     * @return the sorted list of (party,votesPercentage) pairs with the highest percentage upfront
     */
    public static List<Map.Entry<Party, Double>> sortedElectionResultsByPartyPercentage(int tops, Map<Party, Integer> votesCounts) {
        //We created a final with value 100.0
        final double percentage = 100.0;
        Map<Party, Double> partyPercentages = new HashMap<>();
        //We create an totalVotes to hold the total votes of the given map
        int totalVotes = votesCounts.values().stream().mapToInt(Integer::intValue).sum();

        //We loop through the given map add the party and its percentage in our hashmap
        votesCounts.forEach((party, integer) -> {
            partyPercentages.put(party, (integer * percentage) / totalVotes);
        });
        //We want the list in a decreasing order and limit the list to the given value
        return partyPercentages.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(tops).toList();
    }

    /**
     * Find the most representative Polling Station, which has got its votes distribution across all parties
     * the most alike the distribution of overall total votes.
     * A perfect match is found, if for each party the percentage of votes won at the polling station
     * is identical to the percentage of votes won by the party overall in the election.
     * The most representative Polling Station has the smallest deviation from that perfect match.
     * <p>
     * There are different metrics possible to calculate a relative deviation between distributions.
     * You may use the helper method {@link #euclidianVotesDistributionDeviation(Map, Map)}
     * which calculates a relative least-squares deviation between two distributions.
     *
     * @return the most representative polling station.
     */
    public PollingStation findMostRepresentativePollingStation() {
        Map<Party, Integer> allVotes = this.getVotesByParty();
        //We return the pollingstation that has the smallest deviation from the perfect match
        return this.constituencies.stream().flatMap(c -> c.getPollingStations().stream())
                .min(Comparator.comparingDouble(x -> euclidianVotesDistributionDeviation(x.getVotesByParty(),
                        allVotes))).orElseThrow();
    }

    /**
     * Calculates the Euclidian distance between the relative distribution across parties of two voteCounts.
     * If the two relative distributions across parties are identical, then the distance will be zero
     * If some parties have relatively more votes in one distribution than the other, the outcome will be positive.
     * The lower the outcome, the more alike are the relative distributions of the voteCounts.
     * ratign of votesCounts1 relative to votesCounts2.
     * see https://towardsdatascience.com/9-distance-measures-in-data-science-918109d069fa
     *
     * @param votesCounts1 one distribution of votes across parties.
     * @param votesCounts2 another distribution of votes across parties.
     * @return de relative distance between the two distributions.
     */
    private double euclidianVotesDistributionDeviation(Map<Party, Integer> votesCounts1, Map<Party, Integer> votesCounts2) {
        // calculate total number of votes in both distributions
        int totalNumberOfVotes1 = integersSum(votesCounts1.values());
        int totalNumberOfVotes2 = integersSum(votesCounts2.values());

        // we calculate the distance as the sum of squares of relative voteCount distribution differences per party
        // if we compare two voteCounts that have the same relative distribution across parties, the outcome will be zero

        return votesCounts1.entrySet().stream()
                .mapToDouble(e -> Math.pow(e.getValue() / (double) totalNumberOfVotes1 -
                        votesCounts2.getOrDefault(e.getKey(), 0) / (double) totalNumberOfVotes2, 2))
                .sum();
    }

    /**
     * auxiliary method to calculate the total sum of a collection of integers
     *
     * @param integers
     * @return
     */
    public static int integersSum(Collection<Integer> integers) {
        return integers.stream().reduce(Integer::sum).orElse(0);
    }


    public String prepareSummary(int partyId) {
        Party party = this.getParty(partyId);
        StringBuilder summary = new StringBuilder()
                .append("\nSummary of ").append(party).append(":\n");
        summary.append("Total number of candidates = ").append(party.getCandidates().size()).append("\n\n");
        summary.append("Candidates: [").append(party.getCandidates().stream().map(Candidate::toString)
                .collect(Collectors.joining(","))).append("]\n");
        //We print the total number of registrations
        this.numberOfRegistrationsByConstituency(party).values()
                .stream().reduce(Integer::sum)
                .ifPresent(x -> summary.append("Total number of registrations = ").append(x).append("\n"));
        //We print the total number of registrations per constituency
        summary.append("Registrations by constituency: ")
                .append(this.numberOfRegistrationsByConstituency(party)
                        .entrySet().stream().map(x -> x.getKey() + "=" + x.getValue())
                        .collect(Collectors.joining(","))).append("\n");
        return summary.toString();
    }

    public String prepareSummary() {

        StringBuilder summary = new StringBuilder()
                .append("\nElection summary of ").append(this.name).append(":\n");

        summary.append("\n").append(parties.values().size()).append(" Participating parties\n");
        summary.append(parties.values());

        summary.append("\nTotal number of constituencies = ").append(constituencies.size());
        summary.append("\nTotal number of polling stations = ")
                .append(constituencies.stream().mapToLong(x -> x.getPollingStations().size()).sum());
        summary.append("\nTotal number of candidates in the election = ")
                .append(parties.values().stream().mapToInt(x -> x.getCandidates().size()).sum());

        summary.append("\nDifferent candidates with duplicate names across different parties are:\n");
        summary.append(getCandidatesWithDuplicateNames()).append("\n");

        summary.append("\nOverall election results by party percentage:\n");
        summary.append(sortedElectionResultsByPartyPercentage(getParties().size(), getVotesByParty())).append("\n");

        summary.append("\nPolling stations in Amsterdam Wibautstraat area with zip codes 1091AA-1091ZZ:\n");
        summary.append(getPollingStationsByZipCodeRange("1091AA", "1091ZZ")).append("\n");

        summary.append("\nTop 10 election results by party percentage in Amsterdam area with zip codes 1091AA-1091ZZ:\n");
        summary.append(sortedElectionResultsByPartyPercentage
                (10, getVotesByPartyAcrossPollingStations(constituencies.stream()
                .flatMap(constituency -> constituency.getPollingStations().stream())
                .filter(pollingStation -> pollingStation.getZipCode().startsWith("1091")).toList()))).append("\n");
        summary.append("\nMost representative polling station is:\n");
        summary.append(findMostRepresentativePollingStation()).append("\n");
        summary.append("\n").append(sortedElectionResultsByPartyPercentage(findMostRepresentativePollingStation()
                        .getVotesByParty().values().size(), findMostRepresentativePollingStation()
                .getVotesByParty())).append("\n");
        return summary.toString();
    }

    /**
     * Reads all data of Parties, Candidates, Contingencies and PollingStations from available files in the given folder and its subfolders
     * This method can cope with any structure of sub folders, but does assume the file names to comply with the conventions
     * as found from downloading the files from https://data.overheid.nl/dataset/verkiezingsuitslag-tweede-kamer-2021
     * So, you can merge folders after unpacking the zip distributions of the data, but do not change file names.
     *
     * @param folderName the root folder with the data files of the election results
     * @return een Election met alle daarbij behorende gegevens.
     * @throws XMLStreamException bij fouten in een van de XML bestanden.
     * @throws IOException        als er iets mis gaat bij het lezen van een van de bestanden.
     */
    public static Election importFromDataFolder(String folderName) throws XMLStreamException, IOException {
        System.out.println("Loading election data from " + folderName);
        Election election = new Election(folderName);
        int progress = 0;
        Map<Integer, Constituency> kieskringen = new HashMap<>();
        for (Path constituencyCandidatesFile : PathUtils.findFilesToScan(folderName, "Kandidatenlijsten_TK2021_")) {
            XMLParser parser = new XMLParser(new FileInputStream(constituencyCandidatesFile.toString()));
            Constituency constituency = Constituency.importFromXML(parser, election.parties);
            //election.constituenciesM.put(constituency.getId(), constituency);
            election.constituencies.add(constituency);
            showProgress(++progress);
        }
        System.out.println();
        progress = 0;
        for (Path votesPerPollingStationFile : PathUtils.findFilesToScan(folderName, "Telling_TK2021_gemeente")) {
            XMLParser parser = new XMLParser(new FileInputStream(votesPerPollingStationFile.toString()));
            election.importVotesFromXml(parser);
            showProgress(++progress);
        }
        System.out.println();
        return election;
    }

    protected static void showProgress(final int progress) {
        System.out.print('.');
        if (progress % 50 == 0) System.out.println();
    }

    /**
     * Auxiliary method for parsing the data from the EML files
     * This methode can be used as-is and does not require your investigation or extension.
     */
    public void importVotesFromXml(XMLParser parser) throws XMLStreamException {
        if (parser.findBeginTag(Constituency.CONSTITUENCY)) {

            int constituencyId = 0;
            if (parser.findBeginTag(Constituency.CONSTITUENCY_IDENTIFIER)) {
                constituencyId = parser.getIntegerAttributeValue(null, Constituency.ID, 0);
                parser.findAndAcceptEndTag(Constituency.CONSTITUENCY_IDENTIFIER);
            }

            //Constituency constituency = this.constituenciesM.get(constituencyId);
            final int finalConstituencyId = constituencyId;
            Constituency constituency = this.constituencies.stream()
                    .filter(c -> c.getId() == finalConstituencyId)
                    .findFirst()
                    .orElse(null);

            //parser.findBeginTag(PollingStation.POLLING_STATION_VOTES);
            while (parser.findBeginTag(PollingStation.POLLING_STATION_VOTES)) {
                PollingStation pollingStation = PollingStation.importFromXml(parser, constituency, this.parties);
                if (pollingStation != null) constituency.add(pollingStation);
            }

            parser.findAndAcceptEndTag(Constituency.CONSTITUENCY);
        }
    }

    /**
     * HINTS:
     * getCandidatesWithDuplicateNames:
     *  Approach-1: first build a Map that counts the number of candidates per given name
     *              then build the collection from all candidates, excluding those whose name occurs only once.
     *  Approach-2: build a stream that is sorted by name
     *              apply a mapMulti that drops unique names but keeps the duplicates
     *              this approach probably requires complex lambda expressions that are difficult to justify
     */

}
