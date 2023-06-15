package uk.ac.ebi.eva.contigalias.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import uk.ac.ebi.eva.contigalias.entities.SeqColL1;
import uk.ac.ebi.eva.contigalias.repo.ChromosomeRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SeqColServiceTest {

    @Autowired
    private SeqColL1Service seqColL1Service;

    @Autowired
    private ChromosomeRepository chromosomeRepository;

    private final String ASSEMBLY_INSCD_ACCESSION = "GCA_000001765.2";

    private final String BASE_DIGEST = "a6748aa0__";

    private final String BASE_NAMES = "ce04be12__";

    private final String BASE_LENGTHS = "4925cdbd__";

    private final String BASE_SEQUENCES = "3b379221__";

    private final List<String> NAMES_ARRAY = Arrays.asList("AB__", "CD__", "EF__", "GH__");
    private final List<String> LENGTHS_ARRAY = Arrays.asList("12", "34", "56", "78");
    private final List<String> SEQUENCES_ARRAY = Arrays.asList("AA__", "TT__", "GG__", "CC__");



    SeqColL1 generateLevel2SeqCol(String id) {
        SeqColL1 seqColL1 = new SeqColL1();
        seqColL1.setSequences(BASE_SEQUENCES + id);
        seqColL1.setLengths(BASE_LENGTHS + id);
        seqColL1.setNames(BASE_NAMES + id);
        seqColL1.setDigest(BASE_DIGEST + id);
        return seqColL1;
    }

    /**
     * Generate a sample sequences array*/
    List<String> generateSampleSequencesArray(String id){
        List<String> array = new ArrayList<>();
        for (String s: SEQUENCES_ARRAY) {
            array.add(s + id);
        }
        return array;
    }


    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addSeqCol() {
        for (int i=1000; i<1003; i++) {
            SeqColL1 seqColL1 = generateLevel2SeqCol(Integer.toString(i))   ;
            List<String> sequences = generateSampleSequencesArray(Integer.toString(i));

            // saving the seqCol level 2
            seqColL1Service.addSeqColL1(seqColL1);

            // saving the sequences in the chromosome table
            for (String seq: sequences){
                // This will update all rows, only for testing
                chromosomeRepository.updateChromosomeEntityByAccessionSetMD5Checksum(seq);
            }
        }

        List<SeqColL1> seqColL1List = seqColL1Service.getAll();
        assertNotNull(seqColL1List);

    }

    @Test
    void timedTest(){
        long start = System.currentTimeMillis();
        long end;
        addSeqCol();
        end = System.currentTimeMillis();
        System.out.println("Execution time: " + (end - start));
    }
}