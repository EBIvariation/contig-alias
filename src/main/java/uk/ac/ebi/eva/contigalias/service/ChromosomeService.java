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
        return stripChromosomesAndScaffoldsFromAssembly(chromosomes);
    }

    public Page<ChromosomeEntity> getChromosomesByRefseq(String refseq, Pageable request) {
        Page<ChromosomeEntity> chromosomes = repository.findChromosomeEntitiesByRefseq(refseq, request);
        return stripChromosomesAndScaffoldsFromAssembly(chromosomes);
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
        return stripChromosomesAndScaffoldsFromAssembly(page);
    }

    public Page<ChromosomeEntity> getChromosomesByNameAndAssemblyTaxid(String name, long asmTaxid, Pageable request) {
        Page<ChromosomeEntity> page = repository.findChromosomeEntitiesByNameAndAssembly_Taxid(name, asmTaxid, request);
        return stripChromosomesAndScaffoldsFromAssembly(page);
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
        return stripChromosomesAndScaffoldsFromAssembly(page);
    }

    public Page<ChromosomeEntity> getChromosomesByUcscNameAndAssemblyTaxid(
            String ucscName, long asmTaxid, Pageable request) {
        Page<ChromosomeEntity> page
                = repository.findChromosomeEntitiesByUcscNameAndAssembly_Taxid(ucscName, asmTaxid, request);
        return stripChromosomesAndScaffoldsFromAssembly(page);
    }

    public Page<ChromosomeEntity> getChromosomesByUcscNameAndAssembly(String ucscName, AssemblyEntity assembly,
                                                                      Pageable request) {
        Page<ChromosomeEntity> page
                = repository.findChromosomeEntitiesByUcscNameAndAssembly(ucscName, assembly, request);
        assembly.setChromosomes(null);
        return injectAssemblyIntoChromosomes(page, assembly);
    }

    public Page<ChromosomeEntity> getChromosomesByEnaName(String enaName, Pageable request) {
        Page<ChromosomeEntity> page = repository.findChromosomeEntitiesByEnaSequenceName(enaName, request);
        return stripChromosomesAndScaffoldsFromAssembly(page);
    }

    public Page<ChromosomeEntity> getChromosomesByEnaNameAndAssemblyTaxid(
            String enaName, long asmTaxid, Pageable request) {
        Page<ChromosomeEntity> page
                = repository.findChromosomeEntitiesByEnaSequenceNameAndAssembly_Taxid(enaName, asmTaxid, request);
        return stripChromosomesAndScaffoldsFromAssembly(page);
    }

    public Page<ChromosomeEntity> getChromosomesByEnaNameAndAssembly(
            String enaName, AssemblyEntity assembly, Pageable request) {
        Page<ChromosomeEntity> page
                = repository.findChromosomeEntitiesByEnaSequenceNameAndAssembly(enaName, assembly, request);
        assembly.setChromosomes(null);
        return injectAssemblyIntoChromosomes(page, assembly);
    }

    private Page<ChromosomeEntity> injectAssemblyIntoChromosomes(Page<ChromosomeEntity> page, AssemblyEntity assembly) {
        if (page != null && page.getTotalElements() > 0) {
            page.forEach(it -> it.setAssembly(assembly));
        }
        return page;
    }

    private Page<ChromosomeEntity> stripChromosomesAndScaffoldsFromAssembly(Page<ChromosomeEntity> page) {
        if (page != null && page.getTotalElements() > 0) {
            page.forEach(it -> {
                stripChromosomeFromAssembly(it);
                stripScaffoldFromAssembly(it);
            });
        }
        return page;
    }

    private void stripScaffoldFromAssembly(ChromosomeEntity chromosome) {
        AssemblyEntity assembly = chromosome.getAssembly();
        if (assembly != null) {
            assembly.setScaffolds(null);
        }
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

    public void putChromosomeChecksumsByAccession(String accession, String md5, String trunc512) {
        Page<ChromosomeEntity> page = repository.findChromosomeEntitiesByGenbankOrRefseq(
                accession, accession, Pageable.unpaged());
        if (page.isEmpty()){
            throw new IllegalArgumentException(
                    "No chromosomes corresponding to accession " + accession + " found in the database");
        }
        page.forEach(it -> {
            it.setMd5checksum(md5).setTrunc512checksum(trunc512);
            repository.save(it);
        });
    }

    public void insertChromosome(ChromosomeEntity entity) {
        // TODO check if entity already exists in db
        repository.save(entity);
    }

    public void deleteChromosome(ChromosomeEntity entity) {
        // TODO check if entity already exists in db
        repository.delete(entity);
    }

    public long countChromosomeEntitiesByGenbank(String genbank) {
        return repository.countChromosomeEntitiesByGenbank(genbank);
    }

    public long countChromosomeEntitiesByRefseq(String refseq) {
        return repository.countChromosomeEntitiesByRefseq(refseq);
    }

    public long countChromosomeEntitiesByAssembly_Genbank(String asmGenbank) {
        return repository.countChromosomeEntitiesByAssembly_Genbank(asmGenbank);
    }

    public long countChromosomeEntitiesByAssembly_Refseq(String asmRefseq) {
        return repository.countChromosomeEntitiesByAssembly_Refseq(asmRefseq);
    }

    public long countChromosomeEntitiesByNameAndAssembly_Taxid(String name, long asmTaxid) {
        return repository.countChromosomeEntitiesByNameAndAssembly_Taxid(name, asmTaxid);
    }

    public long countChromosomeEntitiesByUcscNameAndAssembly_Taxid(String ucscName, long asmTaxid) {
        return repository.countChromosomeEntitiesByUcscNameAndAssembly_Taxid(ucscName, asmTaxid);
    }

    public long countChromosomeEntitiesByEnaNameAndAssembly_Taxid(String enaName, long asmTaxid) {
        return repository.countChromosomeEntitiesByEnaNameAndAssembly_Taxid(enaName, asmTaxid);
    }

    public long countChromosomeEntitiesByNameAndAssembly(String name, AssemblyEntity assembly) {
        return repository.countChromosomeEntitiesByNameAndAssembly(name, assembly);
    }

    public long countChromosomeEntitiesByUcscNameAndAssembly(String ucscName, AssemblyEntity assembly) {
        return repository.countChromosomeEntitiesByUcscNameAndAssembly(ucscName, assembly);
    }

    public long countChromosomeEntitiesByEnaNameAndAssembly(String enaName, AssemblyEntity assembly) {
        return repository.countChromosomeEntitiesByEnaNameAndAssembly(enaName, assembly);
    }

    public long countChromosomeEntitiesByName(String name) {
        return repository.countChromosomeEntitiesByName(name);
    }

    public long countChromosomeEntitiesByAssemblyGenbankOrAssemblyRefseq(String genbank, String refseq) {
        return repository.countChromosomeEntitiesByAssemblyGenbankOrAssemblyRefseq(genbank, refseq);
    }

    public long countChromosomeEntitiesByUcscName(String ucscName) {
        return repository.countChromosomeEntitiesByUcscName(ucscName);
    }

    public long countChromosomeEntitiesByEnaName(String enaName) {
        return repository.countChromosomeEntitiesByEnaName(enaName);
    }

}
