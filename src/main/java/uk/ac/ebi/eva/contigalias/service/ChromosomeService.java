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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.repo.ChromosomeRepository;

import java.util.Optional;

@Service
public class ChromosomeService {

    private final ChromosomeRepository repository;

    @Autowired
    public ChromosomeService(ChromosomeRepository repository) {
        this.repository = repository;
    }

    public Optional<ChromosomeEntity> getChromosomeByGenbank(String genbank) {
        return getChromosomeByGenbank(genbank, PageRequest.of(0, 1));
    }

    public Optional<ChromosomeEntity> getChromosomeByGenbank(String genbank, Pageable request) {
        Slice<ChromosomeEntity> slice = repository.findChromosomeEntityByGenbank(genbank, request);
        return getOptionalFromSlice(slice);
    }

    public Optional<ChromosomeEntity> getChromosomeByRefseq(String refseq) {
        return getChromosomeByRefseq(refseq, PageRequest.of(0, 1));
    }

    public Optional<ChromosomeEntity> getChromosomeByRefseq(String refseq, Pageable request) {
        Slice<ChromosomeEntity> slice = repository.findChromosomeEntityByRefseq(refseq, request);
        return getOptionalFromSlice(slice);
    }

    private Optional<ChromosomeEntity> getOptionalFromSlice(Slice<ChromosomeEntity> slice) {
        if (slice.getNumberOfElements() > 0) {
            ChromosomeEntity chromosomeEntity = slice.getContent().get(0);
            stripChromosomeFromAssembly(chromosomeEntity);
            return Optional.of(chromosomeEntity);
        } else return Optional.empty();
    }

    private void stripChromosomeFromAssembly(ChromosomeEntity chromosome) {
        AssemblyEntity assembly = chromosome.getAssembly();
        if (assembly != null) {
            assembly.setChromosomes(null);
        }
    }

    public void insertChromosome(ChromosomeEntity entity) {
        // TODO check if entity already exists in db
        repository.save(entity);
    }

    public void deleteChromosome(ChromosomeEntity entity) {
        // TODO check if entity already exists in db
        repository.delete(entity);
    }

}
