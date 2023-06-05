package uk.ac.ebi.eva.contigalias.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.ac.ebi.eva.contigalias.repo.AssemblySequencesRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AssemblySequencesServiceTest {


    @Autowired
    private AssemblySequencesService assemblySequencesService;

    @Autowired
    private AssemblySequencesRepository assemblySequencesRepository;

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void fetchAndInsertAssemblySequence() throws IOException, NoSuchAlgorithmException {
        String accession = "GCF_000001765.3";
        assemblySequencesService.fetchAndInsertAssemblySequence(accession);
        assertNotNull(assemblySequencesRepository.findAssemblySequenceEntityByInsdcAccession(accession));
        assertEquals(accession, assemblySequencesRepository.findAssemblySequenceEntityByInsdcAccession(accession).get().getInsdcAccession());
    }

    @Test
    void insertAssemblySequence() {
    }
}