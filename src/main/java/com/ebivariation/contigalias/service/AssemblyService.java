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
        Optional<AssemblyEntity> fetchAssembly = dataSource.getAssemblyByAccession(accession);
        if (fetchAssembly.isPresent()) {
            AssemblyEntity asm = fetchAssembly.get();
            insertAssembly(asm);
            List<ChromosomeEntity> chromosomes = asm.getChromosomes();
            if (chromosomes != null) {
                chromosomes.forEach(chr -> chr.setAssembly(null));
            }
            return fetchAssembly;
        }
        return Optional.empty();
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
                entity.getGenbank(), entity.getRefseq());
        return existingAssembly.isPresent();
    }

}
