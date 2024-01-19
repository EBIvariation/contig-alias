package uk.ac.ebi.eva.contigalias.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.List;

@Component
public class ChecksumSetter {
    private final Logger logger = LoggerFactory.getLogger(ChecksumSetter.class);
    private int DEFAULT_PAGE_SIZE = 10000;
    private ChromosomeService chromosomeService;
    private Md5ChecksumRetriever md5ChecksumRetriever;

    @Autowired
    public ChecksumSetter(ChromosomeService chromosomeService, Md5ChecksumRetriever md5ChecksumRetriever) {
        this.chromosomeService = chromosomeService;
        this.md5ChecksumRetriever = md5ChecksumRetriever;
    }

    // @Scheduled(cron = "30 15 10 1 * ? 2023")   -- the task to run at 10:15:30 AM on the 1st day of every month in the year 2023.
    //Seconds: 30 Minutes: 15 Hours: 10 Day of the month: 1 Month: Every month Day of the week: Every day of the week Year: 2023
    @Scheduled(initialDelay = 0, fixedDelay = 24 * 60 * 60 * 1000)
    public void updateMd5CheckSumForAllAssemblies() {
        List<String> assemblyList = chromosomeService.getAssembliesWhereChromosomeMd5ChecksumIsNull();
        for (String assembly : assemblyList) {
            logger.info("Trying to update md5checksum for assembly: " + assembly);
            updateMD5ChecksumForAllChromosomesInAssembly(assembly);
        }
    }

    public void updateMD5ChecksumForAllChromosomesInAssembly(String assembly) {
        int pageNumber = 0;
        Pageable pageable = PageRequest.of(pageNumber, DEFAULT_PAGE_SIZE);
        Slice<ChromosomeEntity> chrSlice = chromosomeService.getChromosomesByAssemblyInsdcAccessionWhereMd5ChecksumIsNull(assembly, pageable);
        while (chrSlice.hasContent()) {
            List<ChromosomeEntity> chromosomeEntityList = chrSlice.getContent();
            updateMd5ChecksumForChromosome(chromosomeEntityList);

            pageNumber++;
            pageable = PageRequest.of(pageNumber, DEFAULT_PAGE_SIZE);
            chrSlice = chromosomeService.getChromosomesByAssemblyInsdcAccessionWhereMd5ChecksumIsNull(assembly, pageable);
        }
    }

    public void updateMd5ChecksumForChromosome(List<ChromosomeEntity> chromosomesList) {
        chromosomesList.parallelStream().forEach(chromosome -> {
            try {
                String md5Checksum = md5ChecksumRetriever.retrieveMd5Checksum(chromosome.getInsdcAccession());
                chromosome.setMd5checksum(md5Checksum);
            } catch (Exception e) {
                logger.info("Could not retrieve md5Checksum for insdc accession: " + chromosome.getInsdcAccession());
            }
        });

        chromosomeService.updateMd5ChecksumForAll(chromosomesList);
    }
}
