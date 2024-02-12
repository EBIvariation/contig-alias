/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.contigalias.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.ac.ebi.eva.contigalias.datasource.ENAAssemblyDataSource;
import uk.ac.ebi.eva.contigalias.datasource.NCBIAssemblyDataSource;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.exception.AssemblyIngestionException;
import uk.ac.ebi.eva.contigalias.exception.AssemblyNotFoundException;
import uk.ac.ebi.eva.contigalias.exception.DuplicateAssemblyException;
import uk.ac.ebi.eva.contigalias.repo.AssemblyRepository;
import uk.ac.ebi.eva.contigalias.repo.ChromosomeRepository;
import uk.ac.ebi.eva.contigalias.scheduler.ChromosomeUpdater;
import uk.ac.ebi.eva.contigalias.scheduler.Job.Job;
import uk.ac.ebi.eva.contigalias.scheduler.Job.JobType;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class AssemblyService {
    private final int BATCH_SIZE = 100000;

    private final ChromosomeService chromosomeService;

    private final AssemblyRepository assemblyRepository;

    private final ChromosomeRepository chromosomeRepository;

    private final NCBIAssemblyDataSource ncbiDataSource;

    private final ENAAssemblyDataSource enaDataSource;

    private final ChromosomeUpdater chromosomeUpdater;

    private final Logger logger = LoggerFactory.getLogger(AssemblyService.class);

    @Autowired
    public AssemblyService(ChromosomeService chromosomeService, AssemblyRepository repository, ChromosomeRepository chromosomeRepository,
                           NCBIAssemblyDataSource ncbiDataSource, ENAAssemblyDataSource enaDataSource,
                           ChromosomeUpdater chromosomeUpdater) {
        this.chromosomeService = chromosomeService;
        this.assemblyRepository = repository;
        this.chromosomeRepository = chromosomeRepository;
        this.ncbiDataSource = ncbiDataSource;
        this.enaDataSource = enaDataSource;
        this.chromosomeUpdater = chromosomeUpdater;
    }

    public Optional<AssemblyEntity> getAssemblyByInsdcAccession(String insdcAccession) {
        Optional<AssemblyEntity> entity = assemblyRepository.findAssemblyEntityByInsdcAccession(insdcAccession);
        return entity;
    }

    public Optional<AssemblyEntity> getAssemblyByRefseq(String refseq) {
        Optional<AssemblyEntity> entity = assemblyRepository.findAssemblyEntityByRefseq(refseq);
        return entity;
    }

    public Page<AssemblyEntity> getAssembliesByTaxid(long taxid, Pageable request) {
        Page<AssemblyEntity> page = assemblyRepository.findAssemblyEntitiesByTaxid(taxid, request);
        return page;
    }

    public void putAssemblyChecksumsByAccession(String accession, String md5, String trunc512) {
        Optional<AssemblyEntity> entity = assemblyRepository.findAssemblyEntityByAccession(accession);
        if (!entity.isPresent()) {
            throw new IllegalArgumentException(
                    "No assembly corresponding to accession " + accession + " found in the database");
        }
        AssemblyEntity assemblyEntity = entity.get();
        assemblyEntity.setMd5checksum(md5).setTrunc512checksum(trunc512);
        assemblyRepository.save(assemblyEntity);
    }

    public void fetchAndInsertAssembly(String accession) {
        // check if assembly already exists in db
        Optional<AssemblyEntity> entity = assemblyRepository.findAssemblyEntityByAccession(accession);
        if (entity.isPresent()) {
            throw duplicateAssemblyInsertionException(accession, entity.get());
        }

        try {
            // download file and save assembly and chromosome data
            logger.info("Start inserting assembly for accession " + accession);
            parseFileAndInsertAssembly(accession);
            logger.info("Successfully inserted assembly for accession " + accession);
        } catch (Exception e) {
            // roll back inserted entries in case of any exception or error
            logger.error("Exception while inserting assembly " + accession + " Rolling back changes. \n" + e);
            deleteEntriesForAssembly(accession);
            throw new AssemblyIngestionException(accession);
        }
    }

    public void parseFileAndInsertAssembly(String accession) throws IOException {
        Optional<Path> downloadNCBIFilePathOpt = ncbiDataSource.downloadAssemblyReport(accession);
        Path downloadedNCBIFilePath = downloadNCBIFilePathOpt.orElseThrow(() -> new AssemblyNotFoundException(accession));

        long numberOfChromosomesInFile = Files.lines(downloadedNCBIFilePath).filter(line -> !line.startsWith("#")).count();
        logger.info("Number of chromosomes in assembly (" + accession + "): " + numberOfChromosomesInFile);

        AssemblyEntity assemblyEntity = ncbiDataSource.getAssemblyEntity(downloadedNCBIFilePath);
        assemblyRepository.save(assemblyEntity);

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(downloadedNCBIFilePath.toFile()))) {
            long chromosomesSavedTillNow = 0l;
            List<String> chrLines = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                chrLines.add(line);
                if (chrLines.size() == BATCH_SIZE) {
                    List<ChromosomeEntity> chromosomeEntityList = ncbiDataSource.getChromosomeEntityList(assemblyEntity, chrLines);
                    chromosomeService.saveAllChromosomes(chromosomeEntityList);
                    chromosomesSavedTillNow += chrLines.size();
                    logger.info("Number of chromosomes saved till now  : " + chromosomesSavedTillNow);

                    chrLines = new ArrayList<>();
                }
            }
            if (!chrLines.isEmpty()) {
                // add ena sequence name and save
                List<ChromosomeEntity> chromosomeEntityList = ncbiDataSource.getChromosomeEntityList(assemblyEntity, chrLines);
                chromosomeService.saveAllChromosomes(chromosomeEntityList);
                chromosomesSavedTillNow += chrLines.size();
                logger.info("Number of chromosomes saved till now  : " + chromosomesSavedTillNow);
            }
        }

        // delete the files after assembly insertion
        Files.deleteIfExists(downloadedNCBIFilePath);
    }

    public void deleteEntriesForAssembly(String accession) {
        chromosomeRepository.deleteChromosomeEntitiesByAssembly_InsdcAccession(accession);
        assemblyRepository.deleteAssemblyEntityByInsdcAccessionOrRefseq(accession);
    }

    public void retrieveAndInsertMd5ChecksumForAssembly(String assembly) {
        Job md5ChecksumupdateJob = new Job(JobType.MD5_CHECKSUM_UPDATE, assembly);
        chromosomeUpdater.submitJob(md5ChecksumupdateJob);
    }

    public void retrieveAndInsertMd5ChecksumForAssembly(List<String> assemblies) {
        List<Job> jobsList = new ArrayList();
        for (String assembly : assemblies) {
            jobsList.add(new Job(JobType.MD5_CHECKSUM_UPDATE, assembly));
        }
        chromosomeUpdater.submitJob(jobsList);
    }

    public void retrieveAndInsertENASequenceNameForAssembly(String assembly) {
        Job enaSequenceNameupdateJob = new Job(JobType.ENA_SEQUENCE_NAME_UPDATE, assembly);
        chromosomeUpdater.submitJob(enaSequenceNameupdateJob);
    }

    public void retrieveAndInsertENASequenceNameForAssembly(List<String> assemblies) {
        List<Job> jobsList = new ArrayList();
        for (String assembly : assemblies) {
            jobsList.add(new Job(JobType.ENA_SEQUENCE_NAME_UPDATE, assembly));
        }
        chromosomeUpdater.submitJob(jobsList);
    }

    public List<String> getScheduledJobStatus() {
        return chromosomeUpdater.getScheduledJobStatus();
    }

    public Optional<AssemblyEntity> getAssemblyByAccession(String accession) {
        Optional<AssemblyEntity> assemblyEntity = assemblyRepository.findAssemblyEntityByAccession(accession);
        return assemblyEntity;
    }

    @Transactional
    public void insertAssembly(AssemblyEntity entity) {
        if (isEntityPresent(entity)) {
            throw duplicateAssemblyInsertionException(null, entity);
        } else {
            assemblyRepository.save(entity);
        }
    }


    public boolean isEntityPresent(AssemblyEntity entity) {
        String insdcAccession = entity.getInsdcAccession();
        String refseq = entity.getRefseq();
        if (insdcAccession == null && refseq == null) {
            return false;
        }
        Optional<AssemblyEntity> existingAssembly = assemblyRepository.findAssemblyEntityByInsdcAccessionOrRefseq(
                // Setting to invalid prevents finding random accessions with null GCA/GCF
                insdcAccession == null ? "##########" : insdcAccession,
                refseq == null ? "##########" : refseq);
        return existingAssembly.isPresent();
    }

    public Map<String, List<String>> fetchAndInsertAssembly(List<String> accessions) {
        Map<String, List<String>> accessionResult = new HashMap<>();
        accessionResult.put("SUCCESS", new ArrayList<>());
        accessionResult.put("FAILURE", new ArrayList<>());

        for (String accession : accessions) {
            try {
                logger.info("Started processing assembly accession : " + accession);
                this.fetchAndInsertAssembly(accession);
                accessionResult.get("SUCCESS").add(accession);
            } catch (Exception e) {
                logger.error("Exception while loading assembly for accession " + accession + e);
                accessionResult.get("FAILURE").add(accession);
            }
        }
        logger.info("Success: " + accessionResult.getOrDefault("SUCCESS", Collections.emptyList()));
        logger.info("Failure: " + accessionResult.getOrDefault("FAILURE", Collections.emptyList()));

        return accessionResult;
    }

    public void deleteAssemblyByInsdcAccession(String insdcAccession) {
        assemblyRepository.deleteAssemblyEntityByInsdcAccession(insdcAccession);
    }

    public void deleteAssemblyByRefseq(String refseq) {
        assemblyRepository.deleteAssemblyEntityByRefseq(refseq);
    }

    public void deleteAssemblyByAccession(String accession) {
        Optional<AssemblyEntity> assembly = getAssemblyByAccession(accession);
        assembly.ifPresent(this::deleteAssembly);
    }

    public void deleteAssembly(AssemblyEntity entity) {
        assemblyRepository.delete(entity);
    }

    private DuplicateAssemblyException duplicateAssemblyInsertionException(String accession, AssemblyEntity present) {
        StringBuilder exception = new StringBuilder("A similar assembly already exists!");
        if (accession != null) {
            exception.append("\n");
            exception.append("Assembly trying to insert:");
            exception.append("\t");
            exception.append(accession);
        }
        if (present != null) {
            exception.append("\n");
            exception.append("Assembly already present");
            exception.append("\t");
            exception.append(present);
        }
        return new DuplicateAssemblyException(exception.toString());
    }

}