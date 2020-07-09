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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.datasource.AssemblyDataSource;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.repo.AssemblyRepository;

import java.io.IOException;
import java.util.Collections;
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

    @Autowired
    public AssemblyService(AssemblyRepository repository, @Qualifier("NCBIDataSource") AssemblyDataSource dataSource) {
        this.repository = repository;
        this.dataSource = dataSource;
    }

    public List<AssemblyEntity> getAssemblyOrFetchByAccession(String accession) throws IOException {

        List<AssemblyEntity> entities = getAssemblyByAccession(accession);
        if (!entities.isEmpty()) {
            return entities;
        }
        fetchAndInsertAssembly(accession);

        entities = getAssemblyByAccession(accession);
        if (!entities.isEmpty()) {
            return entities;
        } else return new LinkedList<>();
    }

    public List<AssemblyEntity> getAssemblyByGenbank(String genbank) {
        Optional<AssemblyEntity> entity = repository.findAssemblyEntityByGenbank(genbank);
        return convertOptionalToList(entity);
    }

    public List<AssemblyEntity> getAssemblyByRefseq(String refseq) {
        Optional<AssemblyEntity> entity = repository.findAssemblyEntityByRefseq(refseq);
        return convertOptionalToList(entity);
    }

    public List<AssemblyEntity> getAssembliesByTaxid(long taxid, Pageable request) {
        Slice<AssemblyEntity> slice = repository.findAssemblyEntitiesByTaxid(taxid, request);
        return convertSliceToList(slice);
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

    public List<AssemblyEntity> getAssemblyByAccession(String accession) {
        Optional<AssemblyEntity> entity = repository.findAssemblyEntityByAccession(accession);
        return convertOptionalToList(entity);
    }

    public List<AssemblyEntity> convertSliceToList(Slice<AssemblyEntity> slice) {
        if (slice.getNumberOfElements() > 0) {
            List<AssemblyEntity> content = slice.getContent();
            content.forEach(this::stripAssemblyFromChromosomes);
            return content;
        } else {
            return new LinkedList<>();
        }
    }

    public List<AssemblyEntity> convertOptionalToList(Optional<AssemblyEntity> optional) {
        if (optional.isPresent()) {
            AssemblyEntity entity = optional.get();
            stripAssemblyFromChromosomes(entity);
            return Collections.singletonList(entity);
        } else {
            return new LinkedList<>();
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
        while (repository.count() >= CACHE_SIZE) {
            repository.findTopByIdNotNullOrderById().ifPresent(it -> repository.deleteById(it.getId()));
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
        List<AssemblyEntity> assemblies = getAssemblyByAccession(accession);
        if (!assemblies.isEmpty()) {
            assemblies.forEach(this::deleteAssembly);
        }
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

}
