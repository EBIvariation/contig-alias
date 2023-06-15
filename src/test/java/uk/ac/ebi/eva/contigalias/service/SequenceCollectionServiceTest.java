package uk.ac.ebi.eva.contigalias.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.AssemblySequencesEntity;
import uk.ac.ebi.eva.contigalias.entities.SeqColEntity;
import uk.ac.ebi.eva.contigalias.repo.AssemblyRepository;
import uk.ac.ebi.eva.contigalias.repo.AssemblySequencesRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class SequenceCollectionServiceTest {

    @Autowired
    private SequenceCollectionService seqColService;

    @Autowired
    private AssemblyRepository assemblyRepository;

    @Autowired
    private AssemblySequencesRepository assemblySequencesRepository;

    private Optional<AssemblyEntity> assemblyEntity;

    private Optional<AssemblySequencesEntity> assemblySequencesEntity;

    private final String ACCESSION = "GCF_000001765.3";

    private final SeqColEntity.NamingConvention namingConvention = SeqColEntity.NamingConvention.ENA;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void fetchAndInsertSequenceCollection() {
        assemblyEntity = assemblyRepository.findAssemblyEntityByAccession(ACCESSION);
        assemblySequencesEntity = assemblySequencesRepository.findAssemblySequenceEntityByAssemblyInsdcAccession(ACCESSION);
        SeqColEntity seqColEntity = seqColService.constructSequenceCollectionObjectL2(assemblyEntity.get(),
                assemblySequencesEntity.get(), namingConvention);
        assertNotNull(seqColEntity);
    }

}