package uk.ac.ebi.eva.contigalias.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.ac.ebi.eva.contigalias.datasource.NCBIAssemblyDataSource;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class MD5ChecksumUpdater {
    private final Logger logger = LoggerFactory.getLogger(MD5ChecksumUpdater.class);
    private final int DEFAULT_BATCH_SIZE = 10000;
    private String INSDC_ACCESSION_PLACE_HOLDER = "INSDC_ACCESSION_PLACE_HOLDER";
    private String INSDC_CHECKSUM_URL = "https://www.ebi.ac.uk/ena/cram/sequence/insdc:" + INSDC_ACCESSION_PLACE_HOLDER + "/metadata";
    private final NCBIAssemblyDataSource ncbiDataSource;
    private final ChromosomeService chromosomeService;
    private RestTemplate restTemplate;

    @Autowired
    public MD5ChecksumUpdater(ChromosomeService chromosomeService, NCBIAssemblyDataSource ncbiDataSource, RestTemplate restTemplate) {
        this.chromosomeService = chromosomeService;
        this.ncbiDataSource = ncbiDataSource;
        this.restTemplate = restTemplate;
    }

    public void updateMD5ChecksumForAssembly(String accession) {
        logger.info("Start Update MD5 Checksum for assembly : " + accession);
        Path downloadedNCBIFilePath = null;
        try {
            Optional<Path> downloadNCBIFilePathOpt = ncbiDataSource.downloadAssemblyReport(accession);
            downloadedNCBIFilePath = downloadNCBIFilePathOpt.orElseThrow(() -> new AssemblyNotFoundException(accession));

            AssemblyEntity assemblyEntity = new AssemblyEntity();
            assemblyEntity.setInsdcAccession(accession);

            long numberOfChromosomesInFile = Files.lines(downloadedNCBIFilePath).filter(line -> !line.startsWith("#")).count();
            logger.info("Number of chromosomes in assembly (" + accession + "): " + numberOfChromosomesInFile);

            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(downloadedNCBIFilePath.toFile()))) {
                long chromosomesUpdatedTillNow = 0l;
                List<String> chrLines = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.startsWith("#")) {
                        continue;
                    }
                    chrLines.add(line);
                    if (chrLines.size() == DEFAULT_BATCH_SIZE) {
                        List<ChromosomeEntity> chromosomeEntityList = ncbiDataSource.getChromosomeEntityList(assemblyEntity, chrLines);
                        updateMd5ChecksumForChromosome(accession, chromosomeEntityList);
                        chromosomesUpdatedTillNow += chrLines.size();
                        logger.info("Number of chromosomes updated till now  : " + chromosomesUpdatedTillNow);

                        chrLines = new ArrayList<>();
                    }
                }
                if (!chrLines.isEmpty()) {
                    List<ChromosomeEntity> chromosomeEntityList = ncbiDataSource.getChromosomeEntityList(assemblyEntity, chrLines);
                    updateMd5ChecksumForChromosome(accession, chromosomeEntityList);
                    chromosomesUpdatedTillNow += chrLines.size();
                    logger.info("Number of chromosomes updated till now  : " + chromosomesUpdatedTillNow);
                }
            }

            logger.info("MD5 Checksum update finished successfully for assembly: " + accession);
        } catch (Exception e) {
            logger.error("Error while updating MD5 Checksum for assembly : " + accession + "\n" + e);
        } finally {
            if (downloadedNCBIFilePath != null) {
                try {
                    Files.deleteIfExists(downloadedNCBIFilePath);
                } catch (Exception e) {
                    logger.warn("Could not delete file : " + downloadedNCBIFilePath);
                }
            }
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
