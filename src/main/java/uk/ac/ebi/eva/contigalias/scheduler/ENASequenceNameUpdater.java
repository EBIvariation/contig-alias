package uk.ac.ebi.eva.contigalias.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.contigalias.datasource.ENAAssemblyDataSource;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ENASequenceNameUpdater {
    private final Logger logger = LoggerFactory.getLogger(MD5ChecksumUpdater.class);
    private final int DEFAULT_BATCH_SIZE = 100000;
    private final ENAAssemblyDataSource enaDataSource;

    private final ChromosomeService chromosomeService;

    public ENASequenceNameUpdater(ENAAssemblyDataSource enaDataSource, ChromosomeService chromosomeService) {
        this.enaDataSource = enaDataSource;
        this.chromosomeService = chromosomeService;
    }

    public void updateENASequenceNameForAssembly(String assembly) {
        Path downloadedENAFilePath = null;
        try {
            logger.info("Trying to update ENA Sequence Name for assembly: " + assembly);
            Optional<Path> downloadENAFilePathOpt = enaDataSource.downloadAssemblyReport(assembly);
            if (downloadENAFilePathOpt.isPresent()) {
                downloadedENAFilePath = downloadENAFilePathOpt.get();

                long numberOfChromosomesInFile = Files.lines(downloadedENAFilePath)
                                                      .filter(line -> !line.startsWith("accession")).count();
                logger.info("Number of chromosomes in assembly (" + assembly + "): " + numberOfChromosomesInFile);

                // retrieve and save ena sequence names
                retrieveAndUpdateENASequenceNames(assembly, downloadedENAFilePath);
            } else {
                logger.warn("Could not download assembly report for assembly : " + assembly);
            }
        } catch (Exception e) {
            logger.error("Error while updating ENA Sequence Name for assembly : " + assembly + "\n" + e);
        } finally {
            try {
                if (downloadedENAFilePath != null) {
                    Files.deleteIfExists(downloadedENAFilePath);
                }
            } catch (IOException e) {
                logger.error("Error while deleting downloaded ENA assembly report file with path " + downloadedENAFilePath
                        + " for assembly : " + assembly);
            }
        }
    }

    private void retrieveAndUpdateENASequenceNames(String assembly, Path downloadedENAFilePath) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(downloadedENAFilePath.toFile()))) {
            long chromosomesProcessedTillNow = 0l;
            List<String> chrLines = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("accession")) {
                    continue;
                }
                chrLines.add(line);
                if (chrLines.size() == DEFAULT_BATCH_SIZE) {
                    List<ChromosomeEntity> chromosomeEntityList = enaDataSource.getChromosomeEntityList(chrLines);
                    chromosomeService.updateENASequenceNameForAllChromosomeInAssembly(assembly, chromosomeEntityList);
                    chromosomesProcessedTillNow += chrLines.size();
                    logger.info("Number of chromosomes updated till now  : " + chromosomesProcessedTillNow);

                    chrLines = new ArrayList<>();
                }
            }
            if (!chrLines.isEmpty()) {
                List<ChromosomeEntity> chromosomeEntityList = enaDataSource.getChromosomeEntityList(chrLines);
                chromosomeService.updateENASequenceNameForAllChromosomeInAssembly(assembly, chromosomeEntityList);
                chromosomesProcessedTillNow += chrLines.size();
                logger.info("Number of chromosomes updated till now  : " + chromosomesProcessedTillNow);
            }
        }

        logger.info("Finished updating ENA Sequence Name for assembly: " + assembly);
    }
}
