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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
public class ChecksumSetter {
    private final Logger logger = LoggerFactory.getLogger(ChecksumSetter.class);
    private final Map<String, CompletableFuture<Void>> runningMD5ChecksumUpdateTasks = new ConcurrentHashMap<>();
    private Set<String> scheduledToRunMD5ChecksumUpdateTasks = new HashSet<>();
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
    @Scheduled(initialDelay = 24 * 60 * 60 * 1000, fixedDelay = 24 * 60 * 60 * 1000)
    public void updateMd5CheckSumForAllAssemblies() {
        scheduledToRunMD5ChecksumUpdateTasks = new HashSet<>();
        List<String> assemblyList = chromosomeService.getAssembliesWhereChromosomeMd5ChecksumIsNull();
        logger.info("List of assemblies to be updated for MD5 Checksum: " + assemblyList);
        scheduledToRunMD5ChecksumUpdateTasks.addAll(assemblyList.stream().collect(Collectors.toSet()));

        for (String assembly : assemblyList) {
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
        Slice<ChromosomeEntity> chrSlice;
        Pageable pageable = PageRequest.of(0, DEFAULT_PAGE_SIZE);
        do {
            chrSlice = chromosomeService.getChromosomesByAssemblyInsdcAccessionWhereMd5ChecksumIsNull(assembly, pageable);
            List<ChromosomeEntity> chromosomeEntityList = chrSlice.getContent();
            updateMd5ChecksumForChromosome(chromosomeEntityList);
        } while (chrSlice.hasNext());

        logger.info("Updating md5checksum for assembly " + assembly + " completed");
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

    public Map<String, Set<String>> getMD5ChecksumUpdateTaskStatus() {
        Map<String, Set<String>> taskStatus = new HashMap<>();
        taskStatus.put("running", runningMD5ChecksumUpdateTasks.keySet());
        taskStatus.put("scheduled", scheduledToRunMD5ChecksumUpdateTasks);
        return taskStatus;
    }
}
