package uk.ac.ebi.eva.contigalias.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.ArrayList;
import java.util.List;

@Component
public class MD5ChecksumUpdater {
    private final Logger logger = LoggerFactory.getLogger(MD5ChecksumUpdater.class);
    private final int DEFAULT_BATCH_SIZE = 10000;
    private String INSDC_ACCESSION_PLACE_HOLDER = "INSDC_ACCESSION_PLACE_HOLDER";
    private String INSDC_CHECKSUM_URL = "https://www.ebi.ac.uk/ena/cram/sequence/insdc:" + INSDC_ACCESSION_PLACE_HOLDER + "/metadata";
    private RestTemplate restTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final ChromosomeService chromosomeService;

    @Autowired
    public MD5ChecksumUpdater(RestTemplate restTemplate, JdbcTemplate jdbcTemplate, ChromosomeService chromosomeService) {
        this.restTemplate = restTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.chromosomeService = chromosomeService;
    }

    public void updateMD5ChecksumForAssembly(String assembly) {
        try {
            logger.info("Trying to update MD5 Checksum for assembly: " + assembly);
            String sql = "select * from chromosome c where c.assembly_insdc_accession = '" + assembly
                    + "' AND (c.md5checksum IS NULL OR c.md5checksum = '')";
            jdbcTemplate.query(sql, (ResultSetExtractor<Void>) rs -> {
                long chromosomeUpdated = 0;
                List<ChromosomeEntity> chromosomeEntityList = new ArrayList<>();
                while (rs.next()) {
                    ChromosomeEntity chromosome = new ChromosomeEntity();
                    chromosome.setInsdcAccession(rs.getString(1));
                    chromosomeEntityList.add(chromosome);

                    if (chromosomeEntityList.size() == DEFAULT_BATCH_SIZE) {
                        updateMd5ChecksumForChromosome(assembly, chromosomeEntityList);
                        chromosomeUpdated += chromosomeEntityList.size();
                        logger.info("Chromosomes Updated till now: " + chromosomeUpdated);
                        chromosomeEntityList = new ArrayList<>();
                    }
                }
                if (chromosomeEntityList.size() > 0) {
                    updateMd5ChecksumForChromosome(assembly, chromosomeEntityList);
                    chromosomeUpdated += chromosomeEntityList.size();
                    logger.info("Chromosomes Updated till now: " + chromosomeUpdated);
                }

                logger.info("Finished updating MD5 Checksum for assembly: " + assembly);

                return null;
            });
        } catch (Exception e) {
            logger.error("Error while updating MD5 Checksum for assembly : " + assembly + "\n" + e);
        }
    }

    public void updateMd5ChecksumForChromosome(String assembly, List<ChromosomeEntity> chromosomesList) {
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
