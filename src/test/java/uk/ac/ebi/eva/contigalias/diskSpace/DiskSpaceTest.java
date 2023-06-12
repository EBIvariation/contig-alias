package uk.ac.ebi.eva.contigalias.diskSpace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import uk.ac.ebi.eva.contigalias.entities.JSONObjectL1;
import uk.ac.ebi.eva.contigalias.entities.JSONObjectL2;
import uk.ac.ebi.eva.contigalias.entities.NamingConvention;
import uk.ac.ebi.eva.contigalias.entities.SeqColL1;
import uk.ac.ebi.eva.contigalias.entities.SeqColL2;
import uk.ac.ebi.eva.contigalias.service.NamingConventionService;
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
    private NamingConventionService namingConventionService;

    @Autowired
    private SeqColL2Service seqColL2Service;

    private final String DIGEST0 = "a6748aa0f6a1e165f871dbed5_"; // Level 0 digest

    private final String SEQUENCES = "3b379221b4d6ea26da26cec571e5911c";

    private final String LENGTHS = "4925cdbd780a71e332d13145141863c1";

    private final String NAMES = "ce04be1226e56f48da55b6c1__";

    JSONObjectL1 generateJsonObjectL1(String names_id){
        return new JSONObjectL1(SEQUENCES, NAMES + names_id, LENGTHS);
    }


    JSONObjectL2 generateJsonObjectL2(String id){
        List<Integer> lengths = Arrays.asList(
                1216,
                970,
                1788);
        List<String> names = Arrays.asList("AAAAAAA_" + id,
                                           "BBBBBBB_" + id,
                                           "CCCCCC_" + id);
        List<String> sequences = Arrays.asList("76f9f3315fa4b831e93c36cd88196480",
                                               "d5171e863a3d8f832f0559235987b1e5",
                                               "b9b1baaa7abf206f6b70cf31654172db");
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
            SeqColL1 seqColL1 = new SeqColL1(DIGEST0 + Integer.toString(i), jsonObjectL1);
            NamingConvention namingConvention = new NamingConvention(NamingConvention.Convention.valueOf(namingConventionsArr[(i % 10000)%3]),
                                                                     seqColL1);
            namingConventionService.addNamingConvention(namingConvention);


            JSONObjectL2 jsonObjectL2 = generateJsonObjectL2(Integer.toString(i));
            Optional<SeqColL2> seq = seqColL2Service.addSequenceCollectionL2(new SeqColL2(seqColL1.getObject().getNames(), jsonObjectL2)); //Names
            Optional<SeqColL2> seq1 = seqColL2Service.addSequenceCollectionL2(new SeqColL2(seqColL1.getObject().getSequences(), jsonObjectL2)); //Sequences
            Optional<SeqColL2> seq2 = seqColL2Service.addSequenceCollectionL2(new SeqColL2(seqColL1.getObject().getLengths(), jsonObjectL2)); // Lengths
            //System.out.println(jsonObjectL2);
            assertNotNull(seq);
            assertNotNull(seq1);
            assertNotNull(seq2);
        }

        //List<SeqColL1> seqColL1s = seqColL1Service.getAllSeqCollections();
        /*for (SeqColL1 seq : seqColL1s){
            System.out.println(seq.getObject());
        }*/
        //assertNotNull(seqColL1s);
    }

    @Test
    void timedTest(){
        long start = System.currentTimeMillis();
        insertSeqCols();
        long end = System.currentTimeMillis();
        System.out.println("Execution time is: " + (end - start) + " Milliseconds.");
    }
}
