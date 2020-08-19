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

package uk.ac.ebi.eva.contigalias.controller.contigalias;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entities.ScaffoldEntity;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;
import uk.ac.ebi.eva.contigalias.service.ScaffoldService;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.eva.contigalias.controller.BaseHandler.convertToPage;
import static uk.ac.ebi.eva.contigalias.controller.BaseHandler.generatePagedModelFromPage;

@Service
public class ContigAliasHandler {

    private final AssemblyService assemblyService;

    private final ChromosomeService chromosomeService;

    private final ScaffoldService scaffoldService;

    private final PagedResourcesAssembler<AssemblyEntity> assemblyAssembler;

    private final PagedResourcesAssembler<ChromosomeEntity> chromosomeAssembler;

    private final PagedResourcesAssembler<ScaffoldEntity> scaffoldAssembler;

    @Autowired
    public ContigAliasHandler(AssemblyService assemblyService,
                              ChromosomeService chromosomeService,
                              ScaffoldService scaffoldService,
                              PagedResourcesAssembler<AssemblyEntity> assemblyAssembler,
                              PagedResourcesAssembler<ChromosomeEntity> chromosomeAssembler,
                              PagedResourcesAssembler<ScaffoldEntity> scaffoldAssembler) {
        this.assemblyService = assemblyService;
        this.chromosomeService = chromosomeService;
        this.scaffoldService = scaffoldService;
        this.assemblyAssembler = assemblyAssembler;
        this.chromosomeAssembler = chromosomeAssembler;
        this.scaffoldAssembler = scaffoldAssembler;
    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssemblyByAccession(String accession) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByAccession(accession);
        entity.ifPresent(it -> it.setChromosomes(null));
        return generatePagedModelFromPage(convertToPage(entity), assemblyAssembler);
    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssemblyByGenbank(String genbank) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByGenbank(genbank);
        entity.ifPresent(it -> it.setChromosomes(null));
        return generatePagedModelFromPage(convertToPage(entity), assemblyAssembler);
    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssemblyByRefseq(String refseq) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByRefseq(refseq);
        entity.ifPresent(it -> it.setChromosomes(null));
        return generatePagedModelFromPage(convertToPage(entity), assemblyAssembler);

    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssembliesByTaxid(long taxid, Pageable request) {
        Page<AssemblyEntity> page = assemblyService.getAssembliesByTaxid(taxid, request);
        return generatePagedModelFromPage(page, assemblyAssembler);
    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssembliesByChromosomeGenbank(String genbank) {
        List<AssemblyEntity> assemblies = chromosomeService.getAssembliesByChromosomeGenbank(genbank);
        assemblies.addAll(scaffoldService.getAssembliesByScaffoldGenbank(genbank));
        return generatePagedModelFromPage(new PageImpl<>(assemblies), assemblyAssembler);
    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssembliesByChromosomeRefseq(String refseq) {
        List<AssemblyEntity> assemblies = chromosomeService.getAssembliesByChromosomeRefseq(refseq);
        assemblies.addAll(scaffoldService.getAssembliesByScaffoldRefseq(refseq));
        return generatePagedModelFromPage(new PageImpl<>(assemblies), assemblyAssembler);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomesByGenbank(String genbank, Pageable request) {
        Page<ChromosomeEntity> page = chromosomeService.getChromosomesByGenbank(genbank, request);
        return generatePagedModelFromPage(page, chromosomeAssembler);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomesByRefseq(String refseq, Pageable request) {
        Page<ChromosomeEntity> page = chromosomeService.getChromosomesByRefseq(refseq, request);
        return generatePagedModelFromPage(page, chromosomeAssembler);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomesByAssemblyGenbank(String genbank, Pageable request) {
        Page<ChromosomeEntity> page = chromosomeService.getChromosomesByAssemblyGenbank(genbank, request);
        return generatePagedModelFromPage(page, chromosomeAssembler);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomesByAssemblyRefseq(String refseq, Pageable request) {
        Page<ChromosomeEntity> page = chromosomeService.getChromosomesByAssemblyRefseq(refseq, request);
        return generatePagedModelFromPage(page, chromosomeAssembler);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomesByAssemblyAccession(String accession,
                                                                                       Pageable request) {
        Page<ChromosomeEntity> page = chromosomeService.getChromosomesByAssemblyAccession(accession, request);
        return generatePagedModelFromPage(page, chromosomeAssembler);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomesByChromosomeNameAndAssemblyTaxid(
            String name, long taxid, String nameType, Pageable request) {
        Page<ChromosomeEntity> page;
        if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
            page = chromosomeService.getChromosomesByUcscNameAndAssemblyTaxid(name, taxid, request);
        } else {
            page = chromosomeService.getChromosomesByNameAndAssemblyTaxid(name, taxid, request);
        }
        return generatePagedModelFromPage(page, chromosomeAssembler);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomesByChromosomeNameAndAssemblyAccession(
            String name, String accession, String nameType, Pageable request) {
        Page<ChromosomeEntity> page = new PageImpl<>(Collections.emptyList());
        Optional<AssemblyEntity> assembly = assemblyService.getAssemblyByAccession(accession);
        if (assembly.isPresent()) {
            if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
                page = chromosomeService.getChromosomesByUcscNameAndAssembly(name, assembly.get(), request);
            } else {
                page = chromosomeService.getChromosomesByNameAndAssembly(name, assembly.get(), request);
            }
        }
        return generatePagedModelFromPage(page, chromosomeAssembler);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomesByName(
            String name, String nameType, Pageable request) {
        Page<ChromosomeEntity> page;
        if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
            page = chromosomeService.getChromosomesByUcscName(name, request);
        } else {
            page = chromosomeService.getChromosomesByName(name, request);
        }
        return generatePagedModelFromPage(page, chromosomeAssembler);
    }

    public PagedModel<EntityModel<ScaffoldEntity>> getScaffoldsByGenbank(String genbank, Pageable request) {
        Page<ScaffoldEntity> page = scaffoldService.getScaffoldsByGenbank(genbank, request);
        return generatePagedModelFromPage(page, scaffoldAssembler);
    }

    public PagedModel<EntityModel<ScaffoldEntity>> getScaffoldsByRefseq(String refseq, Pageable request) {
        Page<ScaffoldEntity> page = scaffoldService.getScaffoldsByRefseq(refseq, request);
        return generatePagedModelFromPage(page, scaffoldAssembler);
    }

    public PagedModel<EntityModel<ScaffoldEntity>> getScaffoldsByAssemblyGenbank(String genbank, Pageable request) {
        Page<ScaffoldEntity> page = scaffoldService.getScaffoldsByAssemblyGenbank(genbank, request);
        return generatePagedModelFromPage(page, scaffoldAssembler);
    }

    public PagedModel<EntityModel<ScaffoldEntity>> getScaffoldsByAssemblyRefseq(String refseq, Pageable request) {
        Page<ScaffoldEntity> page = scaffoldService.getScaffoldsByAssemblyRefseq(refseq, request);
        return generatePagedModelFromPage(page, scaffoldAssembler);
    }

    public PagedModel<EntityModel<ScaffoldEntity>> getScaffoldsByAssemblyAccession(String accession, Pageable request) {
        Page<ScaffoldEntity> page = scaffoldService.getScaffoldsByAssemblyAccession(accession, request);
        return generatePagedModelFromPage(page, scaffoldAssembler);
    }

    public PagedModel<EntityModel<ScaffoldEntity>> getScaffoldsByScaffoldNameAndAssemblyTaxid(
            String name, long taxid, String nameType, Pageable request) {
        Page<ScaffoldEntity> page;
        if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
            page = scaffoldService.getScaffoldsByUcscNameAndAssemblyTaxid(name, taxid, request);
        } else {
            page = scaffoldService.getScaffoldsByNameAndAssemblyTaxid(name, taxid, request);
        }
        return generatePagedModelFromPage(page, scaffoldAssembler);
    }

    public PagedModel<EntityModel<ScaffoldEntity>> getScaffoldsByScaffoldNameAndAssemblyAccession(
            String name, String accession, String nameType, Pageable request) {
        Page<ScaffoldEntity> page = new PageImpl<>(Collections.emptyList());
        Optional<AssemblyEntity> assembly = assemblyService.getAssemblyByAccession(accession);
        if (assembly.isPresent()) {
            if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
                page = scaffoldService.getScaffoldsByUcscNameAndAssembly(name, assembly.get(), request);
            } else {
                page = scaffoldService.getScaffoldsByNameAndAssembly(name, assembly.get(), request);
            }
        }
        return generatePagedModelFromPage(page, scaffoldAssembler);
    }

    public PagedModel<EntityModel<ScaffoldEntity>> getScaffoldsByName(
            String name, String nameType, Pageable request) {
        Page<ScaffoldEntity> page;
        if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
            page = scaffoldService.getScaffoldsByUcscName(name, request);
        } else {
            page = scaffoldService.getScaffoldsByName(name, request);
        }
        return generatePagedModelFromPage(page, scaffoldAssembler);
    }

}
