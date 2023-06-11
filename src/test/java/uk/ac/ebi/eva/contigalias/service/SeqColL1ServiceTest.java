package uk.ac.ebi.eva.contigalias.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.ebi.eva.contigalias.entities.JSONObjectL1;
import uk.ac.ebi.eva.contigalias.entities.NamingConvention;
import uk.ac.ebi.eva.contigalias.entities.SeqColL1;

import javax.transaction.Transactional;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class SeqColL1ServiceTest {

    @Autowired
    private SeqColL1Service seqColL1Service;

    @Autowired
    private NamingConventionService namingConventionService;

    private static SeqColL1 seqCol;

    private final String DIGEST0 = "a6748aa0f6a1e165f871dbed5e54ba62"; // Level 0 digest

    private final NamingConvention.Convention namingConvention = NamingConvention.Convention.ENA;

    private static NamingConvention convention;

    @BeforeEach
    void setUp() {
        JSONObjectL1 jsonL1 = new JSONObjectL1("3b379221b4d6ea26da26cec571e5911c",
                "ce04be1226e56f48da55b6c130d45b94", "4925cdbd780a71e332d13145141863c1");
        seqCol = new SeqColL1(DIGEST0, jsonL1, convention);
        convention = new NamingConvention(namingConvention, seqCol);

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addSequenceCollectionL1() {
        Optional<NamingConvention> namingConvention1 = namingConventionService.addNamingConvention(convention);
        System.out.println(namingConvention1);
        assertNotNull(namingConvention1.get());
    }

    @Test
    void getSequenceCollectionL1(){
        Optional<SeqColL1> seqColL1 = seqColL1Service.getSeqColL1ByDigest(DIGEST0);
        System.out.println(seqColL1.get().getObject().toString());
        assertNotNull(seqColL1.get());
    }
}