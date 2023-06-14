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
    private SeqColService service;

    private final String BASE_DIGEST = "a6748aa0__";

    private final String BASE_NAMES = "ce04besq12__";

    private final String BASE_LENGTHS = "4925csdfsdbd__";

    private final String BASE_SEQUENCES = "3b37sdf9221__";

    private final List<String> NAMES_ARRAY = Arrays.asList("ADB_", "CXD_", "EVF_");
    private final List<String> LENGTHS_ARRAY = Arrays.asList("125", "344", "556");
    private final List<String> SEQUENCES_ARRAY = Arrays.asList("AAN_" , "TGT_", "GGH_");

    /**
     * The JSON object will contain a map with three
     * keys and values*/
    SeqCol generateSeqColLevel1(String id, SeqCol.NamingConvention namingConvention){
        SeqCol seqCol = new SeqCol();
        seqCol.setLevel(1);
        seqCol.setDigest(BASE_DIGEST + id);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("names", BASE_NAMES + id);
        jsonObject.put("lengths", BASE_LENGTHS + id);
        jsonObject.put("sequences", BASE_SEQUENCES + id);
        jsonObject.put("N_convention", namingConvention.toString());
        seqCol.setObject(jsonObject);
        return seqCol;
    }

    /**
     * The JSON object will contain the digests
     * as keys and their corresponding values in
     * a JSONArray*/
    JSONObject generateSeqColLevel2(String id, String digest, String attribute){
        SeqCol seqCol = new SeqCol();
        seqCol.setDigest(digest);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(attribute.toString(), generateSampleArray(id, attribute));
        return jsonObject;
    }

    /**
     * Generate a sample array*/
    JSONArray generateSampleArray(String id, String attribute){
        JSONArray array = new JSONArray();
        switch (attribute){
            case "names":
                for (String s: NAMES_ARRAY) {
                    array.add(s + id);
                }
                break;
            case "sequences":
                for (String s: SEQUENCES_ARRAY) {
                    array.add(s + id);
                }
                break;
            case "lengths":
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

            System.out.println("LEVEL1 OBJECT: " + level1);
            JSONObject object = new JSONObject();
            object.put("names", generateSampleArray(Integer.toString(i), "names"));
            object.put("sequences", generateSampleArray(Integer.toString(i), "sequences"));
            object.put("lengths", generateSampleArray(Integer.toString(i), "lengths"));
            object.put("N_convention", (i % 2 == 0) ? "ENA" : "NCBI");
            SeqCol level2 = new SeqCol(BASE_DIGEST + i, 2, object);

            service.addSeqCol(level1);
            service.addSeqCol(level2);

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