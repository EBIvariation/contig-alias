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

@Service
public class AssemblyService {

    private final AssemblyRepository repository;

    private final AssemblyDataSource dataSource;

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
        Optional<AssemblyEntity> fetchAssembly = dataSource.getAssemblyByAccession(accession);
        fetchAssembly.ifPresent(this::insertAssembly);
        return fetchAssembly;
    }

    public Optional<AssemblyEntity> getAssemblyByAccession(String accession) {
        Optional<AssemblyEntity> assembly = repository.findAssemblyEntityByAccession(accession);
        assembly.ifPresent(asm -> {
            List<ChromosomeEntity> chromosomes = asm.getChromosomes();
            if (chromosomes != null && chromosomes.size() > 0) {
                chromosomes.forEach(chr -> chr.setAssembly(null));
            }
        });
        return assembly;
    }

    public void insertAssembly(AssemblyEntity entity) {
        // Limit cache size to 10 assemblies
        while (repository.countByIdNotNull() >= 10) {
            repository.findTopByIdNotNullOrderById().ifPresent(it -> repository.deleteById(it.getId()));
        }

        if (isEntityPresent(entity)) {
            throw new IllegalArgumentException(
                    "An assembly with the same genbank or refseq accession already exists!");
        } else {
            repository.save(entity);
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

}
