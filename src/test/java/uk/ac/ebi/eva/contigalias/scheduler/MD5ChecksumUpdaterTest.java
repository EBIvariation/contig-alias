package uk.ac.ebi.eva.contigalias.scheduler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest
class MD5ChecksumUpdaterTest {
    private String INSDC_CHECKSUM_URL = "https://www.ebi.ac.uk/ena/cram/sequence/insdc:INSDC_ACCESSION_PLACE_HOLDER/metadata";
    private AssemblyEntity assemblyEntity = AssemblyGenerator.generate();
    private List<ChromosomeEntity> chromosomeEntityList = new ArrayList<>();
    @Autowired
    private ChromosomeService chromosomeService;
    private MD5ChecksumUpdater md5ChecksumUpdater;

    @BeforeEach
    void setup() throws JsonProcessingException {
        RestTemplate restTemplate = mock(RestTemplate.class);
        md5ChecksumUpdater = new MD5ChecksumUpdater(chromosomeService, restTemplate);
        for (int i = 0; i < 5; i++) {
            ChromosomeEntity chromosomeEntity = ChromosomeGenerator.generate(assemblyEntity);
            chromosomeEntityList.add(chromosomeEntity);
            chromosomeService.insertChromosome(chromosomeEntity);

            String jsonMD5Response = "{\"metadata\": {\"md5\": \"" + chromosomeEntity.getInsdcAccession() + "-MD5\"}}";
            Mockito.when(restTemplate.getForObject(INSDC_CHECKSUM_URL.replace("INSDC_ACCESSION_PLACE_HOLDER",
                            chromosomeEntity.getInsdcAccession()), JsonNode.class))
                    .thenReturn(new ObjectMapper().readTree(jsonMD5Response));
        }
    }

    @Test
    void testUpdateMD5ChecksumForAssembly() {
        chromosomeService.getChromosomesByAssemblyInsdcAccession(assemblyEntity.getInsdcAccession(),
                        PageRequest.of(0, 10))
                .forEach(c -> assertNull(c.getMd5checksum()));

        md5ChecksumUpdater.updateMD5ChecksumForAssembly(assemblyEntity.getInsdcAccession());

        chromosomeService.getChromosomesByAssemblyInsdcAccession(assemblyEntity.getInsdcAccession(),
                        PageRequest.of(0, 10))
                .forEach(c -> assertEquals(c.getInsdcAccession() + "-MD5", c.getMd5checksum()));
    }
}
