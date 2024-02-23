package uk.ac.ebi.eva.contigalias.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest
public class ENASequenceNameUpdaterTest {
    private static final String GCA_ACCESSION_HAVING_CHROMOSOMES = "GCA_000003055.5";

    @Autowired
    private ENASequenceNameUpdater enaSequenceNameUpdater;

    @Autowired
    private AssemblyService assemblyService;

    @Autowired
    private ChromosomeService chromosomeService;
    private final List<ChromosomeEntity> chromosomeEntities = new LinkedList<>();

    @BeforeEach
    void setup() {
        assemblyService.fetchAndInsertAssembly(GCA_ACCESSION_HAVING_CHROMOSOMES);
    }

    @AfterEach
    void tearDown() {
        chromosomeEntities.stream().forEach(c -> chromosomeService.deleteChromosome(c));
        assemblyService.deleteEntriesForAssembly(assemblyService.getAssemblyByAccession(GCA_ACCESSION_HAVING_CHROMOSOMES).get().getInsdcAccession());
    }

    @Test
    public void testUpdateENASequenceName() {
        List<ChromosomeEntity> chromosomeListBeforeUpdate = chromosomeService.getChromosomesByInsdcAccession(GCA_ACCESSION_HAVING_CHROMOSOMES,
                PageRequest.of(0, 5000)).getContent();
        chromosomeListBeforeUpdate.stream().forEach(c -> assertNull(c.getEnaSequenceName()));

        enaSequenceNameUpdater.updateENASequenceNameForAssembly(GCA_ACCESSION_HAVING_CHROMOSOMES);

        List<ChromosomeEntity> chromosomeListAfterUpdate = chromosomeService.getChromosomesByInsdcAccession(GCA_ACCESSION_HAVING_CHROMOSOMES,
                PageRequest.of(0, 5000)).getContent();
        chromosomeListAfterUpdate.stream().forEach(c -> assertNotNull(c.getEnaSequenceName()));
    }
}
