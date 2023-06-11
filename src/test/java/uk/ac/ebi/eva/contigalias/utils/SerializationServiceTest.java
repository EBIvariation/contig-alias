package uk.ac.ebi.eva.contigalias.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.webpki.jcs.JsonCanonicalizer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SerializationServiceTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void serialize() throws IOException {
        // Sample data chosen from the seqcol-spec/docs/decision_record.md
        // on https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md#3-creation-of-an-object-composed-of-the-array-names-and-the-digested-arrays
        String seqCol = "{\n" +
                "    \"sequences\": \"EiYgJtUfGyad7wf5atL5OG4Fkzohp2qe\",\n" +
                "    \"lengths\": \"5K4odB173rjao1Cnbk5BnvLt9V7aPAa2\",\n" +
                "    \"names\": \"g04lKdxiYtG3dOGeUC5AdKEifw65G0Wp\"\n" +
                "}";
        String expectedCanonicalRepresentation = "{\"lengths\":\"5K4odB173rjao1Cnbk5BnvLt9V7aPAa2\",\"names\":\"g04lKdxiYtG3dOGeUC5AdKEifw65G0Wp\",\"sequences\":\"EiYgJtUfGyad7wf5atL5OG4Fkzohp2qe\"}";
        JsonCanonicalizer jsonCanonicalizer = new JsonCanonicalizer(seqCol);
        String result = jsonCanonicalizer.getEncodedString();
        assertEquals(expectedCanonicalRepresentation, result);
    }
}