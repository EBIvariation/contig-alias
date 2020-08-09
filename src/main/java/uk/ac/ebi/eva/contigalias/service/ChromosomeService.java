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

import java.util.LinkedList;
import java.util.List;

@Service
public class ChromosomeService {

    private final ChromosomeRepository repository;

    @Autowired
    public ChromosomeService(ChromosomeRepository repository) {
        this.repository = repository;
    }

    public Page<ChromosomeEntity> getChromosomesByGenbank(String genbank, Pageable request) {
        Page<ChromosomeEntity> chromosomes = repository.findChromosomeEntitiesByGenbank(genbank, request);
        return stripAssembliesFromChromosomes(chromosomes);
    }

    public Page<ChromosomeEntity> getChromosomesByRefseq(String refseq, Pageable request) {
        Page<ChromosomeEntity> chromosomes = repository.findChromosomeEntitiesByRefseq(refseq, request);
        return stripAssembliesFromChromosomes(chromosomes);
    }

    public Page<ChromosomeEntity> getChromosomesByAssemblyGenbank(String asmGenbank, Pageable request) {
        Page<ChromosomeEntity> chromosomes = repository.findChromosomeEntitiesByAssembly_Genbank(asmGenbank, request);
        return stripAssembliesFromChromosomes(chromosomes);
    }

    public Page<ChromosomeEntity> getChromosomesByAssemblyRefseq(String asmRefseq, Pageable request) {
        Page<ChromosomeEntity> chromosomes = repository.findChromosomeEntitiesByAssembly_Refseq(asmRefseq, request);
        return stripAssembliesFromChromosomes(chromosomes);
    }

    public List<AssemblyEntity> getAssembliesByChromosomeGenbank(String chrGenbank) {
        Page<ChromosomeEntity> page = repository.findChromosomeEntitiesByGenbank(chrGenbank, Pageable.unpaged());
        return extractAssembliesFromChromosomes(page);
    }

    public List<AssemblyEntity> getAssembliesByChromosomeRefseq(String chrRefseq) {
        Page<ChromosomeEntity> page = repository.findChromosomeEntitiesByRefseq(chrRefseq, Pageable.unpaged());
        return extractAssembliesFromChromosomes(page);
    }

    public List<AssemblyEntity> extractAssembliesFromChromosomes(Page<ChromosomeEntity> page) {
        List<AssemblyEntity> list = new LinkedList<>();
        if (page != null && page.getTotalElements() > 0) {
            for (ChromosomeEntity chromosomeEntity : page) {
                AssemblyEntity assembly = chromosomeEntity.getAssembly();
                assembly.setChromosomes(null);
                list.add(assembly);
            }
        }
        return list;
    }

    public Page<ChromosomeEntity> getChromosomesByName(String name, Pageable request) {
        Page<ChromosomeEntity> page = repository.findChromosomeEntitiesByName(name, request);
        return stripChromosomeFromAssembly(page);
    }

    public Page<ChromosomeEntity> getChromosomesByNameAndAssemblyTaxid(String name, long asmTaxid, Pageable request) {
        Page<ChromosomeEntity> page = repository.findChromosomeEntitiesByNameAndAssembly_Taxid(name, asmTaxid, request);
        return stripChromosomeFromAssembly(page);
    }

    public Page<ChromosomeEntity> getChromosomesByNameAndAssembly(
            String name, AssemblyEntity assembly, Pageable request) {
        Page<ChromosomeEntity> page = repository.findChromosomeEntitiesByNameAndAssembly(name, assembly, request);
        assembly.setChromosomes(null);
        return injectAssemblyIntoChromosomes(page, assembly);
    }

    public Page<ChromosomeEntity> getChromosomesByAssemblyAccession(String accession, Pageable request) {
        Page<ChromosomeEntity> chromosomes = repository.findChromosomeEntitiesByAssemblyGenbankOrAssemblyRefseq(
                accession, accession, request);
        return stripAssembliesFromChromosomes(chromosomes);
    }

    public Page<ChromosomeEntity> getChromosomesByUcscName(String ucscName, Pageable request) {
        Page<ChromosomeEntity> page = repository.findChromosomeEntitiesByUcscName(ucscName, request);
        return stripChromosomeFromAssembly(page);
    }

    public Page<ChromosomeEntity> getChromosomesByUcscNameAndAssemblyTaxid(
            String ucscName, long asmTaxid, Pageable request) {
        Page<ChromosomeEntity> page
                = repository.findChromosomeEntitiesByUcscNameAndAssembly_Taxid(ucscName, asmTaxid, request);
        return stripChromosomeFromAssembly(page);
    }

    public Page<ChromosomeEntity> getChromosomesByUcscNameAndAssembly(String ucscName, AssemblyEntity assembly,
                                                                      Pageable request) {
        Page<ChromosomeEntity> page
                = repository.findChromosomeEntitiesByUcscNameAndAssembly(ucscName, assembly, request);
        assembly.setChromosomes(null);
        return injectAssemblyIntoChromosomes(page, assembly);
    }

    private Page<ChromosomeEntity> injectAssemblyIntoChromosomes(Page<ChromosomeEntity> page, AssemblyEntity assembly) {
        if (page != null && page.getTotalElements() > 0) {
            page.forEach(it -> it.setAssembly(assembly));
        }
        return page;
    }

    private Page<ChromosomeEntity> stripChromosomeFromAssembly(Page<ChromosomeEntity> page) {
        if (page != null && page.getTotalElements() > 0) {
            page.forEach(this::stripChromosomeFromAssembly);
        }
        return page;
    }

    private void stripChromosomeFromAssembly(ChromosomeEntity chromosome) {
        AssemblyEntity assembly = chromosome.getAssembly();
        if (assembly != null) {
            assembly.setChromosomes(null);
        }
    }

    private Page<ChromosomeEntity> stripAssembliesFromChromosomes(Page<ChromosomeEntity> page) {
        if (page != null && page.getTotalElements() > 0) {
            page.forEach(this::stripAssemblyFromChromosome);
        }
        return page;
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
