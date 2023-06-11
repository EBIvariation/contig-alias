package uk.ac.ebi.eva.contigalias.utils;

import org.junit.jupiter.api.Test;
import org.webpki.jcs.JsonCanonicalizer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JsonCanonicalizerTest {

    @Test
    public void testJsonCanonicalizer() throws IOException {
        //String jsonString = "[248956422, 242193529, 198295559]";
        //String jsonString = "[\"染色体-1\",\"染色体-2\",\"染色体-3\"]";
        String jsonString = "{\n" +
                "    \"sequences\": \"EiYgJtUfGyad7wf5atL5OG4Fkzohp2qe\",\n" +
                "    \"lengths\": \"5K4odB173rjao1Cnbk5BnvLt9V7aPAa2\",\n" +
                "    \"names\": \"g04lKdxiYtG3dOGeUC5AdKEifw65G0Wp\"\n" +
                "}";
        JsonCanonicalizer jsonCanonicalizer = new JsonCanonicalizer(jsonString);
        String result = jsonCanonicalizer.getEncodedString();
        System.out.println("RESULT IS: " + result);
        assertNotNull(result);
    }

}
