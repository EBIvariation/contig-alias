package uk.ac.ebi.eva.contigalias.service;

import io.swagger.models.auth.In;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.ebi.eva.contigalias.entities.JSONObjectL2;
import uk.ac.ebi.eva.contigalias.entities.NamingConvention;
import uk.ac.ebi.eva.contigalias.entities.SeqColL2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SeqColL2ServiceTest {

    @Autowired
    private SeqColL2Service seqColL2Service;

    private static SeqColL2 seqColL2;

    private static final String DIGEST0 = "a6748aa0f6a1e165f871dbed5e54ba62"; // Level 0 digest

    private final NamingConvention.Convention namingConvention = NamingConvention.Convention.ENA;

    @BeforeEach
    void setUp() {
        JSONObjectL2 jsonObjectL2 = new JSONObjectL2();
        List<String> sequences = Arrays.asList(
                "76f9f3315fa4b831e93c36cd88196480",
                "d5171e863a3d8f832f0559235987b1e5",
                "b9b1baaa7abf206f6b70cf31654172db");
        jsonObjectL2.setObject(sequences);
        seqColL2 = new SeqColL2(DIGEST0, jsonObjectL2);
    }


    @AfterEach
    void tearDown() {
    }

    @Test
    void addSequenceCollectionL2() {
        Optional<SeqColL2> seqCol = seqColL2Service.addSequenceCollectionL2(seqColL2);
        System.out.println(seqCol);
        assertNotNull(seqCol.get());
    }
}