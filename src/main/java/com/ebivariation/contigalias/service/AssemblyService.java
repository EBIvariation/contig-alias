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

package com.ebivariation.contigalias.service;

import com.ebivariation.contigalias.datasource.AssemblyDataSource;
import com.ebivariation.contigalias.entities.AssemblyEntity;
import com.ebivariation.contigalias.entities.ChromosomeEntity;
import com.ebivariation.contigalias.repo.AssemblyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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

    @Autowired
    public AssemblyService(AssemblyRepository repository, @Qualifier("NCBIDataSource") AssemblyDataSource dataSource) {
        this.repository = repository;
        this.dataSource = dataSource;
    }

    public Optional<AssemblyEntity> getAssemblyOrFetchByAccession(String accession) throws IOException {
        Optional<AssemblyEntity> assembly = getAssemblyByAccession(accession);
        if (assembly.isPresent()) {
            return assembly;
        }
        Optional<AssemblyEntity> fetchAssembly = fetchAndInsertAssembly(accession);
        if (fetchAssembly.isPresent()) {
            AssemblyEntity asm = fetchAssembly.get();
            List<ChromosomeEntity> chromosomes = asm.getChromosomes();
            if (chromosomes != null) {
                chromosomes.forEach(chr -> chr.setAssembly(null));
            }
            // Setting this to null to remove inconsistency in results
            // When fetching from db bring no results, chromosomes is an empty list
            // whereas here chromosomes is value
            else {
                asm.setChromosomes(new LinkedList<>());
            }
            return fetchAssembly;
        }
        return Optional.empty();
    }

    public Optional<AssemblyEntity> fetchAndInsertAssembly(String accession) throws IOException {
        if (repository.findAssemblyEntityByAccession(accession).isPresent()) {
            throw duplicateAssemblyInsertionException(accession);
        }
        Optional<AssemblyEntity> fetchAssembly = dataSource.getAssemblyByAccession(accession);
        fetchAssembly.ifPresent(this::insertAssembly);
        return fetchAssembly;
    }

    public Optional<AssemblyEntity> getAssemblyByAccession(String accession) {
        Optional<AssemblyEntity> assembly = repository.findAssemblyEntityByAccession(accession);
        assembly.ifPresent(this::stripAssemblyFromChromosomes);
        return assembly;
    }

    private void stripAssemblyFromChromosomes(AssemblyEntity assembly) {
        List<ChromosomeEntity> chromosomes = assembly.getChromosomes();
        if (chromosomes != null && chromosomes.size() > 0) {
            chromosomes.forEach(chr -> chr.setAssembly(null));
        }
    }

    public void insertAssembly(AssemblyEntity entity) {
        setCacheSizeLimit();

        if (isEntityPresent(entity)) {
            throw duplicateAssemblyInsertionException(entity);
        } else {
            repository.save(entity);
        }
    }

    /**
     * Limits the size of the cache to a maximum of 10 assemblies
     * <p>
     * I'm using a while loop instead of an if statement because
     * if two requests reach at once, they both might read cache
     * size < 10 and add an entry leading to cache having more than
     * 10 entries. While loop on next run deletes entities till cache
     * size < 10. Now of course the same problem can arise if two
     * requests start deleting at the same time but all that will lead
     * to is the cache getting completely emptied.
     * </p>
     */
    private void setCacheSizeLimit() {
        // Limit cache size to 10 assemblies
        while (repository.countByIdNotNull() >= 10) {
            repository.findTopByIdNotNullOrderById().ifPresent(it -> repository.deleteById(it.getId()));
        }
    }

    public void deleteAssembly(AssemblyEntity entity) {
        if (isEntityPresent(entity)) {
            repository.delete(entity);
        }
    }

    public boolean isEntityPresent(AssemblyEntity entity) {
        Optional<AssemblyEntity> existingAssembly = repository.findAssemblyEntityByGenbankOrRefseq(
                // Setting to invalid prevents finding random accessions with null GCA/GCF
                entity.getGenbank() == null ? "##########" : entity.getGenbank(),
                entity.getRefseq() == null ? "##########" : entity.getRefseq());
        return existingAssembly.isPresent();
    }

    public void fetchAndInsertAssembly(List<String> accessions) {
        accessions.forEach(it -> executor.submit(() -> {
            try {
                this.fetchAndInsertAssembly(it);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
    }

    public void deleteAssembly(String accession) {
        Optional<AssemblyEntity> assembly = getAssemblyByAccession(accession);
        assembly.ifPresent(repository::delete);
    }

    private IllegalArgumentException duplicateAssemblyInsertionException(String accession) {
        return new IllegalArgumentException(
                "An assembly having the accession " + accession + " already exists!");
    }

    private IllegalArgumentException duplicateAssemblyInsertionException(AssemblyEntity accession) {
        return new IllegalArgumentException(
                "A similar assembly already exists!\n" + accession.toString());
    }

}
