package uk.ac.ebi.eva.contigalias.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Component
public class ChecksumSetter {
    private final Logger logger = LoggerFactory.getLogger(ChecksumSetter.class);
    private final Map<String, CompletableFuture<Void>> runningMD5ChecksumUpdateTasks = new ConcurrentHashMap<>();
    private Set<String> scheduledToRunMD5ChecksumUpdateTasks = new HashSet<>();
    private int DEFAULT_PAGE_SIZE = 10000;
    private JdbcTemplate jdbcTemplate;
    private ChromosomeService chromosomeService;
    private Md5ChecksumRetriever md5ChecksumRetriever;

    @Autowired
    public ChecksumSetter(ChromosomeService chromosomeService, Md5ChecksumRetriever md5ChecksumRetriever,
                          JdbcTemplate jdbcTemplate) {
        this.chromosomeService = chromosomeService;
        this.md5ChecksumRetriever = md5ChecksumRetriever;
        this.jdbcTemplate = jdbcTemplate;
    }

    //@Scheduled(cron = "0 0 1 ? * TUE")
    public void updateMd5CheckSumForAllAssemblies() {
        List<String> assemblyList = chromosomeService.getAssembliesWhereChromosomeMd5ChecksumIsNull();
        logger.info("List of assemblies to be updated for MD5 Checksum: " + assemblyList);
        scheduledToRunMD5ChecksumUpdateTasks = new HashSet<>(assemblyList);

        for (String assembly : assemblyList) {
            scheduledToRunMD5ChecksumUpdateTasks.remove(assembly);
            CompletableFuture<Void> future = updateMd5CheckSumForAssemblyAsync(assembly);
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Encountered an error when running MD5Checksum update for assembly: " + assembly);
            } finally {
                scheduledToRunMD5ChecksumUpdateTasks.remove(assembly);
            }
        }
    }

    public CompletableFuture<Void> updateMd5CheckSumForAssemblyAsync(String assembly) {
        logger.info("Submitted job for updating MD5 Checksum for assembly (asynchronously)");
        // Check if the async task for this assembly is already running
        CompletableFuture<Void> existingTask = runningMD5ChecksumUpdateTasks.get(assembly);
        if (existingTask != null && !existingTask.isDone()) {
            logger.info("Async task is still running for assembly: " + assembly);
            return existingTask;
        }
        // Start the async task (removing existing run if present)
        runningMD5ChecksumUpdateTasks.remove(assembly);
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            updateMD5ChecksumForAllChromosomesInAssembly(assembly);
        });
        // Store the future in the map for the given assembly
        runningMD5ChecksumUpdateTasks.put(assembly, future);

        // check the status of task upon completion and remove from running tasks
        future.whenComplete((result, exception) -> {
            if (exception != null) {
                logger.error("Async task (MD5Checksum setter) failed for assembly: " + assembly, exception);
            } else {
                logger.info("Async task (MD5Checksum setter) completed successfully for assembly: " + assembly);
            }
            runningMD5ChecksumUpdateTasks.remove(assembly);
        });

        return future;
    }

    public void updateMD5ChecksumForAllChromosomesInAssembly(String assembly) {
        logger.info("Trying to update md5checksum for assembly: " + assembly);
        String sql = "select * from chromosome c where c.assembly_insdc_accession = '" + assembly
                + "' AND (c.md5checksum IS NULL OR c.md5checksum = '')";
        jdbcTemplate.query(sql, (ResultSetExtractor<Void>) rs -> {
            long chromosomeUpdated = 0;
            List<ChromosomeEntity> chromosomeEntityList = new ArrayList<>();
            while (rs.next()) {
                ChromosomeEntity chromosome = new ChromosomeEntity();
                chromosome.setInsdcAccession(rs.getString(1));
                chromosomeEntityList.add(chromosome);

                if (chromosomeEntityList.size() == DEFAULT_PAGE_SIZE) {
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

            logger.info("Finished updating md5checksum for assembly: " + assembly);

            return null;
        });
    }

    public void updateMd5ChecksumForChromosome(String assembly, List<ChromosomeEntity> chromosomesList) {
        chromosomesList.parallelStream().forEach(chromosome -> {
            try {
                String md5Checksum = md5ChecksumRetriever.retrieveMd5Checksum(chromosome.getInsdcAccession());
                chromosome.setMd5checksum(md5Checksum);
            } catch (Exception e) {
                logger.info("Could not retrieve md5Checksum for insdc accession: " + chromosome.getInsdcAccession());
            }
        });

        chromosomeService.updateMd5ChecksumForAllChromosomeInAssembly(assembly, chromosomesList);
    }

    public Map<String, Set<String>> getMD5ChecksumUpdateTaskStatus() {
        Map<String, Set<String>> taskStatus = new HashMap<>();
        taskStatus.put("running", runningMD5ChecksumUpdateTasks.keySet());
        taskStatus.put("scheduled", scheduledToRunMD5ChecksumUpdateTasks);
        return taskStatus;
    }
}
