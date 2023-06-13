package uk.ac.ebi.eva.contigalias.service;

import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import uk.ac.ebi.eva.contigalias.entities.SeqColL1;
import uk.ac.ebi.eva.contigalias.entities.SeqColL2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SeqColL1ServiceTest {

    @Autowired
    private SeqColL1Service seqColL1Service;

    @Autowired
    private SeqColL2Service seqColL2Service;

    private final String BASE_DIGEST = "a6748aa0__";

    private final String BASE_NAMES = "ce04be12__";

    private final String BASE_LENGTHS = "4925cdbd";

    private final String BASE_SEQUENCES = "3b379221";

    private final List<String> NAMES_ARRAY = Arrays.asList("AB__", "CD__", "EF__", "GH__");
    private final List<String> LENGTHS_ARRAY = Arrays.asList("12", "34", "56", "78");
    private final List<String> SEQUENCES_ARRAY = Arrays.asList("AA__", "TT__", "GG__", "CC__");

    /**
     * Generate a unique sample of a sequence collection object
     * */
    SeqColL1 generateSeqColL1(String id, int random){
        SeqColL1 seqColL1 = new SeqColL1();
        seqColL1.setDigest(BASE_DIGEST + id);
        seqColL1.setLengths(BASE_LENGTHS);
        seqColL1.setNames(BASE_NAMES + id);
        seqColL1.setSequences(BASE_SEQUENCES);
        seqColL1.setNaming_convention((random % 2 == 0) ? SeqColL1.NamingConvention.ENA : SeqColL1.NamingConvention.GENBANK);
        return seqColL1;
    }

    /**
     * Generate unique names samples
     * The only difference will be in the names array*/
    List<String> generateNamesValues(String id){
        List<String> names = new ArrayList<>();
        for (String name: NAMES_ARRAY){
            names.add(name + id);
        }
        return names;
    }

    List<String> generateLengthsValues(String id){
        List<String> lengths = new ArrayList<>();
        for (String length: LENGTHS_ARRAY) {
            lengths.add(length + id);
        }
        return lengths;
    }

    List<String> generateSequencesValues(String id) {
        List<String> sequences = new ArrayList<>();
        for (String seq: SEQUENCES_ARRAY) {
            sequences.add(seq);
        }
        return sequences;
    }

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    /**
     * Insert 3 Sequence collection objects in different levels*/
    void addSeqColL() {
        List<String> sharedSequencesList = generateSequencesValues("XXXX");
        List<String> sharedLengthsList = generateLengthsValues("5555");

        for (int i=1000; i<1003; i++) {
            SeqColL1 seqColL1 = generateSeqColL1(Integer.toString(i), i);
            seqColL1Service.addSeqColL1(seqColL1);

            // Adding the attributes of the sequence collection object
            // Adding names values
            for (String name: generateNamesValues(Integer.toString(i))){
                SeqColL2 seqColL2 = new SeqColL2(seqColL1.getNames(), name);
                seqColL2Service.addSeqColL2(seqColL2);
            }

            // Adding sequences values
            for (String seq: sharedSequencesList){
                SeqColL2 seqColL2 = new SeqColL2(seqColL1.getSequences(), seq);
                seqColL2Service.addSeqColL2(seqColL2);
            }

            // Adding lengths values
            for (String len: sharedLengthsList) {
                SeqColL2 seqColL2 = new SeqColL2(seqColL1.getLengths(), len);
                seqColL2Service.addSeqColL2(seqColL2);
            }
        }
        List<SeqColL1> seqColL1List = seqColL1Service.getAllSequenceCollections();
        List<SeqColL2> seqColL2List = seqColL2Service.getAllSequenceCollections();

        assertNotNull(seqColL1List);
        assertNotNull(seqColL2List);
    }
}