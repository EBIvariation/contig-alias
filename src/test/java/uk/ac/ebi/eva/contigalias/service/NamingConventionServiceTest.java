package uk.ac.ebi.eva.contigalias.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.ebi.eva.contigalias.entities.NamingConvention;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class NamingConventionServiceTest {

    @Autowired
    private NamingConventionService namingConventionService;

    private static NamingConvention namingConvention;

    private final NamingConvention.Convention CONVENTION = NamingConvention.Convention.GENBANK;

    private final static String DIGEST0 = "a6748aa0f6a1e165f871dbed5e54ba62";
    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addNamingConvention() {
    }
}