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
import uk.ac.ebi.eva.contigalias.entities.ScaffoldEntity;
import uk.ac.ebi.eva.contigalias.repo.ScaffoldRepository;

@Service
public class ScaffoldService {

    private final ScaffoldRepository repository;

    @Autowired
    public ScaffoldService(ScaffoldRepository repository) {
        this.repository = repository;
    }

    public Page<ScaffoldEntity> getScaffoldsByGenbank(String genbank, Pageable request) {
        Page<ScaffoldEntity> page = repository.findScaffoldEntitiesByGenbank(genbank, request);
        return stripChromosomesAndScaffoldsFromAssembly(page);
    }

    public Page<ScaffoldEntity> getScaffoldsByRefseq(String refseq, Pageable request) {
        Page<ScaffoldEntity> page = repository.findScaffoldEntitiesByRefseq(refseq, request);
        return stripChromosomesAndScaffoldsFromAssembly(page);
    }

    public Page<ScaffoldEntity> getScaffoldsByAssemblyGenbank(String asmGenbank, Pageable request) {
        Page<ScaffoldEntity> page = repository.findScaffoldEntitiesByAssembly_Genbank(asmGenbank, request);
        return stripAssembliesFromScaffolds(page);
    }

    public Page<ScaffoldEntity> getScaffoldsByAssemblyRefseq(String asmRefseq, Pageable request) {
        Page<ScaffoldEntity> page = repository.findScaffoldEntitiesByAssembly_Refseq(asmRefseq, request);
        return stripAssembliesFromScaffolds(page);
    }

    public Page<ScaffoldEntity> getScaffoldsByName(String name, Pageable request) {
        Page<ScaffoldEntity> page = repository.findScaffoldEntitiesByName(name, request);
        return stripChromosomesAndScaffoldsFromAssembly(page);
    }

    public Page<ScaffoldEntity> getScaffoldsByNameAndAssemblyTaxid(String name, long asmTaxid, Pageable request) {
        Page<ScaffoldEntity> page = repository.findScaffoldEntitiesByNameAndAssembly_Taxid(name, asmTaxid, request);
        return stripChromosomesAndScaffoldsFromAssembly(page);
    }

    public Page<ScaffoldEntity> getScaffoldsByNameAndAssembly(
            String name, AssemblyEntity assembly, Pageable request) {
        Page<ScaffoldEntity> page = repository.findScaffoldEntitiesByNameAndAssembly(name, assembly, request);
        assembly.setScaffolds(null);
        return injectAssemblyIntoScaffolds(page, assembly);
    }

    public Page<ScaffoldEntity> getScaffoldsByAssemblyAccession(String accession, Pageable request) {
        Page<ScaffoldEntity> Scaffolds = repository.findScaffoldEntitiesByAssemblyGenbankOrAssemblyRefseq(
                accession, accession, request);
        return stripAssembliesFromScaffolds(Scaffolds);
    }

    public Page<ScaffoldEntity> getScaffoldsByUcscName(String ucscName, Pageable request) {
        Page<ScaffoldEntity> page = repository.findScaffoldEntitiesByUcscName(ucscName, request);
        return stripChromosomesAndScaffoldsFromAssembly(page);
    }

    public Page<ScaffoldEntity> getScaffoldsByUcscNameAndAssemblyTaxid(
            String ucscName, long asmTaxid, Pageable request) {
        Page<ScaffoldEntity> page
                = repository.findScaffoldEntitiesByUcscNameAndAssembly_Taxid(ucscName, asmTaxid, request);
        return stripChromosomesAndScaffoldsFromAssembly(page);
    }

    public Page<ScaffoldEntity> getScaffoldsByUcscNameAndAssembly(String ucscName, AssemblyEntity assembly,
                                                                  Pageable request) {
        Page<ScaffoldEntity> page
                = repository.findScaffoldEntitiesByUcscNameAndAssembly(ucscName, assembly, request);
        assembly.setScaffolds(null);
        return injectAssemblyIntoScaffolds(page, assembly);
    }

    private Page<ScaffoldEntity> injectAssemblyIntoScaffolds(Page<ScaffoldEntity> page, AssemblyEntity assembly) {
        if (page != null && page.getTotalElements() > 0) {
            page.forEach(it -> it.setAssembly(assembly));
        }
        return page;
    }

    private Page<ScaffoldEntity> stripChromosomesAndScaffoldsFromAssembly(Page<ScaffoldEntity> page) {
        if (page != null && page.getTotalElements() > 0) {
            page.forEach(it -> {
                stripChromosomeFromAssembly(it);
                stripScaffoldFromAssembly(it);
            });
        }
        return page;
    }

    private void stripScaffoldFromAssembly(ScaffoldEntity scaffold) {
        AssemblyEntity assembly = scaffold.getAssembly();
        if (assembly != null) {
            assembly.setScaffolds(null);
        }
    }

    private void stripChromosomeFromAssembly(ScaffoldEntity scaffold) {
        AssemblyEntity assembly = scaffold.getAssembly();
        if (assembly != null) {
            assembly.setChromosomes(null);
        }
    }

    private Page<ScaffoldEntity> stripAssembliesFromScaffolds(Page<ScaffoldEntity> page) {
        if (page != null && page.getTotalElements() > 0) {
            page.forEach(this::stripAssemblyFromChromosome);
        }
        return page;
    }

    private void stripAssemblyFromChromosome(ScaffoldEntity scaffold) {
        scaffold.setAssembly(null);
    }

    public void insertScaffold(ScaffoldEntity entity) {
        // TODO check if entity already exists in db
        repository.save(entity);
    }

    public void deleteScaffold(ScaffoldEntity entity) {
        // TODO check if entity already exists in db
        repository.delete(entity);
    }

}
