package uk.ac.ebi.eva.contigalias.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MD5ChecksumUpdater {
    private final Logger logger = LoggerFactory.getLogger(MD5ChecksumUpdater.class);
    private final int DEFAULT_BATCH_SIZE = 10000;
    private String INSDC_ACCESSION_PLACE_HOLDER = "INSDC_ACCESSION_PLACE_HOLDER";
    private String INSDC_CHECKSUM_URL = "https://www.ebi.ac.uk/ena/cram/sequence/insdc:" + INSDC_ACCESSION_PLACE_HOLDER + "/metadata";
    private final ChromosomeService chromosomeService;
    private RestTemplate restTemplate;

    @Autowired
    public MD5ChecksumUpdater(ChromosomeService chromosomeService, RestTemplate restTemplate) {
        this.chromosomeService = chromosomeService;
        this.restTemplate = restTemplate;
    }

    public void updateMD5ChecksumForAssembly(String accession) {
        logger.info("Start Update MD5 Checksum for assembly : " + accession);
        try {
            int pageNumber = 0;
            Page<ChromosomeEntity> chrPage;
            long chromosomeProcessed = 0;
            long chromosomeUpdated = 0;
            do {
                Pageable pageable = PageRequest.of(pageNumber, DEFAULT_BATCH_SIZE);
                chrPage = chromosomeService.getChromosomesByAssemblyAccession(accession, pageable);

                List<ChromosomeEntity> chromosomeEntityList = chrPage.getContent();
                List<ChromosomeEntity> chromosomeEntitiesWithoutMD5 = chromosomeEntityList.stream()
                        .filter(c -> c.getMd5checksum() == null || c.getMd5checksum().isEmpty())
                        .collect(Collectors.toList());

                if(!chromosomeEntitiesWithoutMD5.isEmpty()){
                    updateMd5ChecksumForChromosome(accession, chromosomeEntityList);
                }

                chromosomeProcessed += chromosomeEntityList.size();
                chromosomeUpdated += chromosomeEntitiesWithoutMD5.size();
                logger.info("Chromosomes Processed till now: {}, selected for update till now: {}", chromosomeProcessed, chromosomeUpdated);

                pageNumber++;
            } while (chrPage.hasNext());

            logger.info("Finished updating MD5 Checksum for assembly: " + accession);

        } catch (Exception e) {
            logger.error("Error while updating MD5 Checksum for assembly : " + accession + "\n" + e);
        }
    }

    private void updateMd5ChecksumForChromosome(String assembly, List<ChromosomeEntity> chromosomesList) {
        chromosomesList.parallelStream().forEach(chromosome -> {
            try {
                String md5Checksum = retrieveMd5Checksum(chromosome.getInsdcAccession());
                chromosome.setMd5checksum(md5Checksum);
            } catch (Exception e) {
                logger.info("Could not retrieve MD5 Checksum for insdc accession: " + chromosome.getInsdcAccession());
            }
        });

        chromosomeService.updateMd5ChecksumForAllChromosomeInAssembly(assembly, chromosomesList);
    }

    public String retrieveMd5Checksum(String insdcAccession) {
        String apiURL = INSDC_CHECKSUM_URL.replace(INSDC_ACCESSION_PLACE_HOLDER, insdcAccession);
        JsonNode jsonResponse = restTemplate.getForObject(apiURL, JsonNode.class);
        String md5Checksum = jsonResponse.get("metadata").get("md5").asText();
        return md5Checksum;
    }
}
