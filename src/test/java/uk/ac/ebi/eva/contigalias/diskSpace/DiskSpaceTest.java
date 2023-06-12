package uk.ac.ebi.eva.contigalias.diskSpace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import uk.ac.ebi.eva.contigalias.entities.JSONObjectL1;
import uk.ac.ebi.eva.contigalias.entities.JSONObjectL2;
import uk.ac.ebi.eva.contigalias.entities.NamingConvention;
import uk.ac.ebi.eva.contigalias.entities.SeqColL1;
import uk.ac.ebi.eva.contigalias.entities.SeqColL2;
import uk.ac.ebi.eva.contigalias.service.SeqColL1Service;
import uk.ac.ebi.eva.contigalias.service.SeqColL2Service;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootTest
public class DiskSpaceTest {

    @Autowired
    private SeqColL1Service seqColL1Service;

    @Autowired
    private SeqColL2Service seqColL2Service;

    private final String DIGEST0 = "a6748aa0f6a1e165f871dbed5_"; // Level 0 digest

    private final String SEQUENCES = "3b379221b4d6ea26da26cec571e5911c";

    private final String LENGTHS = "4925cdbd780a71e332d13145141863c1";

    private final String NAMES = "ce04be1226e56f48da55b6c1__";


    JSONObjectL1 generateJsonObjectL1(String names_id){
        return new JSONObjectL1(SEQUENCES, NAMES + names_id, LENGTHS);
    }

    /**
     * Generate a JsonObjectL2 that has a list of lengths
     * as a value in its object
     * */
    JSONObjectL2 generateJsonObjectL2Lengths(){
        List<String> names = Arrays.asList("1216",
                                           "970",
                                           "1788");
        JSONObjectL2 jsonObjectL2 = new JSONObjectL2();
        jsonObjectL2.setObject(names);
        return jsonObjectL2;
    }

    /**
     * Generate a JsonObjectL2 that has a list of sequences
     * as a value in its object
     * */
    JSONObjectL2 generateJsonObjectL2Sequences(){
        List<String> sequences = Arrays.asList("76f9f3315fa4b831e93c36cd88196480",
                                               "d5171e863a3d8f832f0559235987b1e5",
                                               "b9b1baaa7abf206f6b70cf31654172db");
        JSONObjectL2 jsonObjectL2 = new JSONObjectL2();
        jsonObjectL2.setObject(sequences);
        return jsonObjectL2;
    }

    /**
     * Generate a JsonObjectL2 that has a list of names
     * as a value in its object. the id argument is made to make
     * different samples.
     * */
    JSONObjectL2 generateJsonObjectL2Names(String id){
        List<String> names = Arrays.asList("AAAAAAA_" + id,
                                           "BBBBBBB_" + id,
                                           "CCCCCC_" + id);
        JSONObjectL2 jsonObjectL2 = new JSONObjectL2();
        jsonObjectL2.setObject(names);
        return jsonObjectL2;
    }

    @Test
    void insertSeqCols(){
        // Saving 5 different sequence collection objects in the db
        String namingConventionsArr[] = new String[]{"ENA", "GENBANK", "USCS"}; //Just for testing. Not the correct type
        for (int i=10000; i<10003; i++){
            JSONObjectL1 jsonObjectL1 = generateJsonObjectL1(Integer.toString(i));
            SeqColL1 seqColL1 = new SeqColL1(DIGEST0 + Integer.toString(i), jsonObjectL1, NamingConvention.valueOf(namingConventionsArr[(i - 1000)%3]));
            seqColL1Service.addSequenceCollectionL1(seqColL1);


            JSONObjectL2 jsonObjectL2 = generateJsonObjectL2Names(Integer.toString(i));
            Optional<SeqColL2> seqNames = seqColL2Service.addSequenceCollectionL2(new SeqColL2(seqColL1.getObject().getNames(), jsonObjectL2)); //Names

            JSONObjectL2 jsonObjectL3 = generateJsonObjectL2Sequences();
            Optional<SeqColL2> seqSequences = seqColL2Service.addSequenceCollectionL2(new SeqColL2(seqColL1.getObject().getSequences(), jsonObjectL3)); //Sequences

            JSONObjectL2 jsonObjectL4 = generateJsonObjectL2Lengths();
            Optional<SeqColL2> seqLengths = seqColL2Service.addSequenceCollectionL2(new SeqColL2(seqColL1.getObject().getLengths(), jsonObjectL4)); // Lengths

            assertNotNull(seqNames);
            assertNotNull(seqSequences);
            assertNotNull(seqLengths);
        }

        List<SeqColL1> seqColL1s = seqColL1Service.getAllSeqCollections();
        for (SeqColL1 seq : seqColL1s){
            System.out.println(seq.getObject());
        }
        assertNotNull(seqColL1s);
    }

    @Test
    /**
     * Calculate how much time needed to insert the given
     * seqCol objects*/
    void timedTest(){
        long start = System.currentTimeMillis();
        insertSeqCols();
        long end = System.currentTimeMillis();
        System.out.println("Execution time is: " + (end - start) + " Milliseconds.");
    }
}
