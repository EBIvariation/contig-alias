package uk.ac.ebi.eva.contigalias.service;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import uk.ac.ebi.eva.contigalias.entities.SeqCol;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class SeqColServiceTest {

    @Autowired
    private SeqCol2Service service;

    private final String BASE_DIGEST = "a6748aa0__";

    private final String BASE_NAMES = "ce04be12__";

    private final String BASE_LENGTHS = "4925cdbd__";

    private final String BASE_SEQUENCES = "3b379221__";

    private final SeqCol.Attribute names = SeqCol.Attribute.names;
    private final SeqCol.Attribute sequences = SeqCol.Attribute.sequences;
    private final SeqCol.Attribute lengths = SeqCol.Attribute.lengths;
    private final SeqCol.Attribute convention = SeqCol.Attribute.convention;
    private final List<String> NAMES_ARRAY = Arrays.asList("AB__", "CD__", "EF__", "GH__");
    private final List<String> LENGTHS_ARRAY = Arrays.asList("12", "34", "56", "78");
    private final List<String> SEQUENCES_ARRAY = Arrays.asList("AA__", "TT__", "GG__", "CC__");

    /**
     * The JSON object will contain a map with three
     * keys and values*/
    SeqCol generateSeqColLevel1(String id, SeqCol.NamingConvention namingConvention){
        SeqCol seqCol = new SeqCol();
        seqCol.setDigest(BASE_DIGEST + id);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(names.toString(), BASE_NAMES + id);
        jsonObject.put(lengths.toString(), BASE_LENGTHS + id);
        jsonObject.put(sequences.toString(), BASE_SEQUENCES + id);
        jsonObject.put(convention.toString(), namingConvention);
        seqCol.setObject(jsonObject);
        return seqCol;
    }

    /**
     * The JSON object will contain the digests
     * as keys and their corresponding values in
     * a JSONArray*/
    SeqCol generateSeqColLevel2(String id, String digest, SeqCol.Attribute attribute){
        SeqCol seqCol = new SeqCol();
        seqCol.setDigest(digest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(attribute.toString(), generateSampleArray(id, attribute));
        seqCol.setObject(jsonObject);
        return seqCol;
    }

    /**
     * Generate a sample array*/
    JSONArray generateSampleArray(String id, SeqCol.Attribute attribute){
        JSONArray array = new JSONArray();
        switch (attribute){
            case names:
                for (String s: NAMES_ARRAY) {
                    array.add(s + id);
                }
                break;
            case sequences:
                for (String s: SEQUENCES_ARRAY) {
                    array.add(s + id);
                }
                break;
            case lengths:
                for (String s: LENGTHS_ARRAY) {
                    array.add(s + id);
                }
                break;
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
            SeqCol level1 = generateSeqColLevel1(Integer.toString(i), SeqCol.NamingConvention.ENA);
            SeqCol namesArr = generateSeqColLevel2(Integer.toString(i),
                                                   level1.getObject().get(names.toString()).toString(),
                                                   SeqCol.Attribute.names);
            SeqCol sequencesArr = generateSeqColLevel2(Integer.toString(i),
                                                       level1.getObject().get(sequences.toString()).toString(),
                                                       SeqCol.Attribute.sequences);
            SeqCol lengthsArr = generateSeqColLevel2(Integer.toString(i),
                                                     level1.getObject().get(lengths.toString()).toString(),
                                                     lengths);
            service.addSeqCol(level1);
            service.addSeqCol(namesArr);
            service.addSeqCol(sequencesArr);
            service.addSeqCol(lengthsArr);
        }
        List<SeqCol> seqColList = service.getAll();

        for (SeqCol seqCol: seqColList){
            System.out.println(seqCol);
        }

        assertNotNull(seqColList);
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