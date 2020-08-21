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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.datasource.AssemblyDataSource;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.repo.AssemblyRepository;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AssemblyService {

    private final AssemblyRepository repository;

    private final AssemblyDataSource dataSource;

    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final Logger logger = LoggerFactory.getLogger(AssemblyService.class);

    private int CACHE_SIZE = 10;

    // TODO allow configuring this from application.properties, or remove cache limit feature
    private boolean enableCacheLimit = false;

    @Autowired
    public AssemblyService(AssemblyRepository repository, @Qualifier("NCBIDataSource") AssemblyDataSource dataSource) {
        this.repository = repository;
        this.dataSource = dataSource;
    }

    public Optional<AssemblyEntity> getAssemblyOrFetchByAccession(String accession) throws IOException {

        Optional<AssemblyEntity> entities = getAssemblyByAccession(accession);
        if (entities.isPresent()) {
            return entities;
        }
        fetchAndInsertAssembly(accession);

        entities = getAssemblyByAccession(accession);
        return entities;
    }

    public Optional<AssemblyEntity> getAssemblyByGenbank(String genbank) {
        Optional<AssemblyEntity> entity = repository.findAssemblyEntityByGenbank(genbank);
        stripAssemblyFromChromosomes(entity);
        return entity;
    }

    public Optional<AssemblyEntity> getAssemblyByRefseq(String refseq) {
        Optional<AssemblyEntity> entity = repository.findAssemblyEntityByRefseq(refseq);
        stripAssemblyFromChromosomes(entity);
        return entity;
    }

    public Page<AssemblyEntity> getAssembliesByTaxid(long taxid, Pageable request) {
        Page<AssemblyEntity> page = repository.findAssemblyEntitiesByTaxid(taxid, request);
        page.forEach(this::stripAssemblyFromChromosomes);
        return page;
    }

    public void putAssemblyChecksumsByAccession(String accession, String md5, String trunc512) {
        Optional<AssemblyEntity> entity = repository.findAssemblyEntityByAccession(accession);
        if (!entity.isPresent()) {
            throw new IllegalArgumentException(
                    "No assembly corresponding to accession " + accession + " found in the database");
        }
        AssemblyEntity assemblyEntity = entity.get();
        assemblyEntity.setMd5checksum(md5).setTrunc512checksum(trunc512);
        repository.save(assemblyEntity);
    }

    public void fetchAndInsertAssembly(String accession)
            throws IOException, IllegalArgumentException {
        Optional<AssemblyEntity> entity = repository.findAssemblyEntityByAccession(accession);
        if (entity.isPresent()) {
            throw duplicateAssemblyInsertionException(accession, entity.get());
        }
        Optional<AssemblyEntity> fetchAssembly = dataSource.getAssemblyByAccession(accession);
        fetchAssembly.ifPresent(this::insertAssembly);
    }

    public Optional<AssemblyEntity> getAssemblyByAccession(String accession) {
        Optional<AssemblyEntity> entity = repository.findAssemblyEntityByAccession(accession);
        stripAssemblyFromChromosomes(entity);
        return entity;
    }

    public void stripAssemblyFromChromosomes(Optional<AssemblyEntity> optional) {
        if (optional.isPresent()) {
            AssemblyEntity entity = optional.get();
            stripAssemblyFromChromosomes(entity);
        }
    }

    private void stripAssemblyFromChromosomes(AssemblyEntity assembly) {
        List<ChromosomeEntity> chromosomes = assembly.getChromosomes();
        if (chromosomes != null && chromosomes.size() > 0) {
            chromosomes.forEach(chr -> chr.setAssembly(null));
        } else {
            assembly.setChromosomes(new LinkedList<>());
        }
    }

    public void insertAssembly(AssemblyEntity entity) {
        setCacheSizeLimit();

        if (isEntityPresent(entity)) {
            throw duplicateAssemblyInsertionException(null, entity);
        } else {
            repository.save(entity);
        }
    }

    /**
     * Limits the size of the cache to a maximum of CACHE_SIZE assemblies
     * <p>
     * I'm using a while loop instead of an if statement because
     * if two requests reach at once, they both might read cache
     * size < CACHE_SIZE and add an entry leading to cache having more than
     * 10 entries. While loop on next run deletes entities till cache
     * size < CACHE_SIZE. Now of course the same problem can arise if two
     * requests start deleting at the same time but all that will lead
     * to is the cache getting completely emptied.
     * </p>
     */
    private void setCacheSizeLimit() {
        if (enableCacheLimit) {
            while (repository.count() >= CACHE_SIZE) {
                repository.findTopByIdNotNullOrderById().ifPresent(it -> repository.deleteById(it.getId()));
            }
        }
    }

    public boolean isEntityPresent(AssemblyEntity entity) {
        String genbank = entity.getGenbank();
        String refseq = entity.getRefseq();
        if (genbank == null && refseq == null) {
            return false;
        }
        Optional<AssemblyEntity> existingAssembly = repository.findAssemblyEntityByGenbankOrRefseq(
                // Setting to invalid prevents finding random accessions with null GCA/GCF
                genbank == null ? "##########" : genbank,
                refseq == null ? "##########" : refseq);
        return existingAssembly.isPresent();
    }

    public void fetchAndInsertAssembly(List<String> accessions) {
        accessions.forEach(it -> executor.submit(() -> {
            try {
                this.fetchAndInsertAssembly(it);
            } catch (IOException e) {
                logger.error("IOException while fetching and inserting " + it, e);
            }
        }));
    }

    public void deleteAssemblyByGenbank(String genbank) {
        repository.deleteAssemblyEntityByGenbank(genbank);
    }

    public void deleteAssemblyByRefseq(String refseq) {
        repository.deleteAssemblyEntityByRefseq(refseq);
    }

    public void deleteAssemblyByAccession(String accession) {
        Optional<AssemblyEntity> assembly = getAssemblyByAccession(accession);
        assembly.ifPresent(this::deleteAssembly);
    }

    public void deleteAssembly(AssemblyEntity entity) {
        repository.delete(entity);
    }

    private IllegalArgumentException duplicateAssemblyInsertionException(String accession, AssemblyEntity present) {
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
            exception.append(present.toString());
        }
        return new IllegalArgumentException(exception.toString());
    }

    public int getCacheSize() {
        return CACHE_SIZE;
    }

    public void setCacheSize(int CACHE_SIZE) {
        this.CACHE_SIZE = CACHE_SIZE;
    }

    public boolean isEnableCacheLimit() {
        return enableCacheLimit;
    }

    public void setEnableCacheLimit(boolean enableCacheLimit) {
        this.enableCacheLimit = enableCacheLimit;
    }
}