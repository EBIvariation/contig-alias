package uk.ac.ebi.eva.contigalias.scheduler;

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
import uk.ac.ebi.eva.contigalias.datasource.NCBIAssemblyDataSource;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest
class MD5ChecksumUpdaterTest {
    private static final String GCA_ACCESSION_HAVING_CHROMOSOMES = "GCA_000003055.5";
    private String INSDC_CHECKSUM_URL = "https://www.ebi.ac.uk/ena/cram/sequence/insdc:INSDC_ACCESSION_PLACE_HOLDER/metadata";
    private AssemblyEntity assemblyEntity;
    private List<ChromosomeEntity> chromosomeEntityList = new ArrayList<>();
    @Autowired
    private AssemblyService assemblyService;
    @Autowired
    private ChromosomeService chromosomeService;
    @Autowired
    private NCBIAssemblyDataSource ncbiDataSource;
    private MD5ChecksumUpdater md5ChecksumUpdater;

    @BeforeEach
    void setup() throws IOException {
        Path assemblyReportPath = ncbiDataSource.downloadAssemblyReport(GCA_ACCESSION_HAVING_CHROMOSOMES).get();
        assemblyEntity = ncbiDataSource.getAssemblyEntity(assemblyReportPath);
        assemblyService.insertAssembly(assemblyEntity);
        List<String> chrDataLines = Files.lines(assemblyReportPath).filter(l -> !l.startsWith("#"))
                .collect(Collectors.toList());
        chromosomeEntityList = ncbiDataSource.getChromosomeEntityList(assemblyEntity, chrDataLines);
        chromosomeService.insertAllChromosomes(chromosomeEntityList);

        RestTemplate restTemplate = mock(RestTemplate.class);
        md5ChecksumUpdater = new MD5ChecksumUpdater(chromosomeService, ncbiDataSource, restTemplate);
        for (int i = 0; i < chromosomeEntityList.size(); i++) {
            ChromosomeEntity chromosomeEntity = chromosomeEntityList.get(i);
            String jsonMD5Response = "{\"metadata\": {\"md5\": \"" + chromosomeEntity.getInsdcAccession() + "-MD5\"}}";
            Mockito.when(restTemplate.getForObject(INSDC_CHECKSUM_URL.replace("INSDC_ACCESSION_PLACE_HOLDER",
                            chromosomeEntity.getInsdcAccession()), JsonNode.class))
                    .thenReturn(new ObjectMapper().readTree(jsonMD5Response));
        }
    }

    @Test
    void testUpdateMD5ChecksumForAssembly() {
        chromosomeService.getChromosomesByAssemblyInsdcAccession(assemblyEntity.getInsdcAccession(),
                        PageRequest.of(0, 5000))
                .forEach(c -> assertNull(c.getMd5checksum()));

        md5ChecksumUpdater.updateMD5ChecksumForAssembly(assemblyEntity.getInsdcAccession());

        chromosomeService.getChromosomesByAssemblyInsdcAccession(assemblyEntity.getInsdcAccession(),
                        PageRequest.of(0, 5000))
                .forEach(c -> assertEquals(c.getInsdcAccession() + "-MD5", c.getMd5checksum()));
    }
}
