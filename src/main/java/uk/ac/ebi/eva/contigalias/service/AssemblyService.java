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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    public static final Optional<Integer> DEFAULT_PAGE_NUMBER = Optional.of(0);

    public static Optional<Integer> DEFAULT_PAGE_SIZE = Optional.of(10);

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

    public List<AssemblyEntity> getAssemblyOrFetchByAccession(String accession,
                                                              Pageable request) throws IOException {
        List<AssemblyEntity> entities = getAssemblyByAccession(accession, request);
        if (!entities.isEmpty()) {
            return entities;
        }
        Optional<AssemblyEntity> fetchAssembly = fetchAndInsertAssembly(accession, request);

        List<AssemblyEntity> list = new LinkedList<>();

        fetchAssembly.ifPresent(it -> {
            stripAssemblyFromChromosomes(it);
            list.add(it);
        });
        return list;
    }

    public Optional<AssemblyEntity> getAssemblyByGenbank(String genbank) {
        Optional<AssemblyEntity> entity = repository.findAssemblyEntityByGenbank(genbank);
        entity.ifPresent(this::stripAssemblyFromChromosomes);
        return entity;
    }

    public Optional<AssemblyEntity> getAssemblyByRefseq(String refseq) {
        Optional<AssemblyEntity> entity = repository.findAssemblyEntityByRefseq(refseq);
        entity.ifPresent(this::stripAssemblyFromChromosomes);
        return entity;
    }

    public List<AssemblyEntity> getAssembliesByTaxid(long taxid, Pageable request) {
        Slice<AssemblyEntity> slice = repository.findAssemblyEntitiesByTaxid(taxid, request);
        return convertSliceToList(slice);
    }

    public void fetchAndInsertAssembly(String accession, Pageable request)
            throws IOException, IllegalArgumentException {
        Slice<AssemblyEntity> slice = repository.findAssemblyEntitiesByAccession(accession, request);
        List<AssemblyEntity> entities = convertSliceToList(slice);
        if (!entities.isEmpty()) {
            throw duplicateAssemblyInsertionException(accession, entities.get(0));
        }
        Optional<AssemblyEntity> fetchAssembly = dataSource.getAssemblyByAccession(accession);
        fetchAssembly.ifPresent(this::insertAssembly);
    }

    public List<AssemblyEntity> getAssemblyByAccession(String accession, Pageable request) {
        Slice<AssemblyEntity> slice = repository.findAssemblyEntitiesByAccession(accession, request);
        return convertSliceToList(slice);
    }

    public List<AssemblyEntity> convertSliceToList(Slice<AssemblyEntity> slice) {
        if (slice.getNumberOfElements() > 0) {
            List<AssemblyEntity> content = slice.getContent();
            content.forEach(this::stripAssemblyFromChromosomes);
            return content;
        } else return new LinkedList<>();
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
        Slice<AssemblyEntity> existingAssembly = repository.findAssemblyEntitiesByGenbankOrRefseq(
                // Setting to invalid prevents finding random accessions with null GCA/GCF
                genbank == null ? "##########" : genbank,
                refseq == null ? "##########" : refseq,
                PageRequest.of(0, 1));
        return existingAssembly.getNumberOfElements() > 0;
    }

    public void fetchAndInsertAssembly(List<String> accessions) {
        accessions.forEach(it -> executor.submit(() -> {
            try {
                this.fetchAndInsertAssembly(it, PageRequest.of(0,0));
            } catch (IOException e) {
                logger.error("IOException while fetching and inserting " + it, e);
            }
        }));
    }

    public void deleteAssembly(String accession) {
        List<AssemblyEntity> assemblies = getAssemblyByAccession(accession, Pageable.unpaged());
        if (!assemblies.isEmpty()) {
            assemblies.forEach(this::deleteAssembly);
        }
    }

    public void deleteAssembly(AssemblyEntity entity) {
        if (isEntityPresent(entity)) {
            repository.delete(entity);
        }
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
