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
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.repo.ChromosomeRepository;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@Service
public class ChromosomeService {

    private final ChromosomeRepository repository;

    @Autowired
    public ChromosomeService(ChromosomeRepository repository) {
        this.repository = repository;
    }

    public List<ChromosomeEntity> getChromosomeByGenbank(String genbank) {
        Optional<ChromosomeEntity> entity = repository.findChromosomeEntityByGenbank(genbank);
        return convertOptionalToList(entity);
    }

    public List<ChromosomeEntity> getChromosomeByRefseq(String refseq) {
        Optional<ChromosomeEntity> entity = repository.findChromosomeEntityByRefseq(refseq);
        return convertOptionalToList(entity);
    }

    public List<ChromosomeEntity> convertOptionalToList(Optional<ChromosomeEntity> optional) {
        if (optional.isPresent()) {
            ChromosomeEntity entity = optional.get();
            stripChromosomeFromAssembly(entity);
            return List.of(entity);
        } else {
            return new LinkedList<>();
        }
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
