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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.repo.ChromosomeRepository;

import java.util.Collections;
import java.util.List;

@Service
public class ChromosomeService {

    private final ChromosomeRepository repository;

    @Autowired
    public ChromosomeService(ChromosomeRepository repository) {
        this.repository = repository;
    }


    public Page<ChromosomeEntity> getChromosomeByGenbank(String genbank, Pageable request) {
        Page<ChromosomeEntity> page = repository.findChromosomeEntityByGenbank(genbank, request);
        return stripChromosomeFromAssembly(page);
    }

    public Page<ChromosomeEntity> getChromosomeByRefseq(String refseq, Pageable request) {
        Page<ChromosomeEntity> page = repository.findChromosomeEntityByRefseq(refseq, request);
        return stripChromosomeFromAssembly(page);
    }

    public List<ChromosomeEntity> getChromosomesByAssemblyGenbank(String asmGenbank) {
        List<ChromosomeEntity> chromosomes = repository.findChromosomeEntitiesByAssembly_Genbank(asmGenbank);
        if (chromosomes == null) {
            return Collections.emptyList();
        }
        chromosomes.forEach(this::stripAssemblyFromChromosome);
        return chromosomes;
    }

    public List<ChromosomeEntity> getChromosomesByAssemblyRefseq(String asmRefseq) {
        List<ChromosomeEntity> chromosomes = repository.findChromosomeEntitiesByAssembly_Refseq(asmRefseq);
        if (chromosomes == null) {
            return Collections.emptyList();
        }
        chromosomes.forEach(this::stripAssemblyFromChromosome);
        return chromosomes;
    }

    private Page<ChromosomeEntity> stripChromosomeFromAssembly(Page<ChromosomeEntity> page) {
        if (page != null && page.getTotalElements() > 0) {
            page.get().forEach(this::stripChromosomeFromAssembly);
        }
        return page;
    }

    private void stripChromosomeFromAssembly(ChromosomeEntity chromosome) {
        AssemblyEntity assembly = chromosome.getAssembly();
        if (assembly != null) {
            assembly.setChromosomes(null);
        }
    }

    private void stripAssemblyFromChromosome(ChromosomeEntity chromosome) {
        chromosome.setAssembly(null);
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
