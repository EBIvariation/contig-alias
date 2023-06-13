package uk.ac.ebi.eva.contigalias.service;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import uk.ac.ebi.eva.contigalias.entities.SeqCol2;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class SeqCol2ServiceTest {

    @Autowired
    private SeqCol2Service service;

    private final String BASE_DIGEST = "a6748aa0__";

    private final String BASE_NAMES = "ce04be12__";

    private final String BASE_LENGTHS = "4925cdbd__";

    private final String BASE_SEQUENCES = "3b379221__";

    private final SeqCol2.Attribute names = SeqCol2.Attribute.names;
    private final SeqCol2.Attribute sequences = SeqCol2.Attribute.sequences;
    private final SeqCol2.Attribute lengths = SeqCol2.Attribute.lengths;
    private final SeqCol2.Attribute convention = SeqCol2.Attribute.convention;
    private final List<String> NAMES_ARRAY = Arrays.asList("AB__", "CD__", "EF__", "GH__");
    private final List<String> LENGTHS_ARRAY = Arrays.asList("12", "34", "56", "78");
    private final List<String> SEQUENCES_ARRAY = Arrays.asList("AA__", "TT__", "GG__", "CC__");

    /**
     * The JSON object will contain a map with three
     * keys and values*/
    SeqCol2 generateSeqColLevel1(String id, SeqCol2.NamingConvention namingConvention){
        SeqCol2 seqCol2 = new SeqCol2();
        seqCol2.setDigest(BASE_DIGEST + id);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(names.toString(), BASE_NAMES + id);
        jsonObject.put(lengths.toString(), BASE_LENGTHS + id);
        jsonObject.put(sequences.toString(), BASE_SEQUENCES + id);
        jsonObject.put(convention.toString(), namingConvention);
        seqCol2.setObject(jsonObject);
        return seqCol2;
    }

    /**
     * The JSON object will contain the digests
     * as keys and their corresponding values in
     * a JSONArray*/
    SeqCol2 generateSeqColLevel2(String id, String digest, SeqCol2.Attribute attribute){
        SeqCol2 seqCol2 = new SeqCol2();
        seqCol2.setDigest(digest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(attribute.toString(), generateSampleArray(id, attribute));
        seqCol2.setObject(jsonObject);
        return seqCol2;
    }

    /**
     * Generate a sample array*/
    JSONArray generateSampleArray(String id, SeqCol2.Attribute attribute){
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
            SeqCol2 level1 = generateSeqColLevel1(Integer.toString(i), SeqCol2.NamingConvention.ENA);
            SeqCol2 namesArr = generateSeqColLevel2(Integer.toString(i),
                                                    level1.getObject().get(names.toString()).toString(),
                                                    SeqCol2.Attribute.names);
            SeqCol2 sequencesArr = generateSeqColLevel2(Integer.toString(i),
                                                        level1.getObject().get(sequences.toString()).toString(),
                                                        SeqCol2.Attribute.sequences);
            SeqCol2 lengthsArr = generateSeqColLevel2(Integer.toString(i),
                                                      level1.getObject().get(lengths.toString()).toString(),
                                                      lengths);
            service.addSeqCol(level1);
            service.addSeqCol(namesArr);
            service.addSeqCol(sequencesArr);
            service.addSeqCol(lengthsArr);
        }
        List<SeqCol2> seqCol2List = service.getAll();

        for (SeqCol2 seqCol: seqCol2List){
            System.out.println(seqCol);
        }

        assertNotNull(seqCol2List);
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