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
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.ac.ebi.eva.contigalias.controller.BaseHandler.convertToPage;
import static uk.ac.ebi.eva.contigalias.controller.BaseHandler.generatePagedModelFromPage;

@Service
public class ContigAliasHandler {

    private final AssemblyService assemblyService;

    private final ChromosomeService chromosomeService;

    private final PagedResourcesAssembler<AssemblyEntity> assemblyAssembler;

    private final PagedResourcesAssembler<SequenceEntity> sequenceAssembler;

    @Autowired
    public ContigAliasHandler(AssemblyService assemblyService,
                              ChromosomeService chromosomeService,
                              PagedResourcesAssembler<AssemblyEntity> assemblyAssembler,
                              PagedResourcesAssembler<SequenceEntity> sequenceAssembler) {
        this.assemblyService = assemblyService;
        this.chromosomeService = chromosomeService;
        this.assemblyAssembler = assemblyAssembler;
        this.sequenceAssembler = sequenceAssembler;
    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssemblyByAccession(String accession) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByAccession(accession);
        entity.ifPresent(it -> it.setChromosomes(null));
        return generatePagedModelFromPage(convertToPage(entity), assemblyAssembler);
    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssemblyByInsdcAccession(String insdcAccession) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByInsdcAccession(insdcAccession);
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

    public PagedModel<EntityModel<AssemblyEntity>> getAssembliesBySequenceInsdcAccession(String insdcAccession) {
        List<AssemblyEntity> assemblies = chromosomeService.getAssembliesByChromosomeInsdcAccession(insdcAccession);
        return generatePagedModelFromPage(new PageImpl<>(assemblies), assemblyAssembler);
    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssembliesBySequenceRefseq(String refseq) {
        List<AssemblyEntity> assemblies = chromosomeService.getAssembliesByChromosomeRefseq(refseq);
        return generatePagedModelFromPage(new PageImpl<>(assemblies), assemblyAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getSequencesByInsdcAccession(String insdcAccession, Pageable request) {
        Page<ChromosomeEntity> chrPage = chromosomeService.getChromosomesByInsdcAccession(insdcAccession, request);
        return generatePagedModelFromPage(createSequencePage(chrPage), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getSequencesByRefseq(String refseq, Pageable request) {
        Page<ChromosomeEntity> chrPage = chromosomeService.getChromosomesByRefseq(refseq, request);
        return generatePagedModelFromPage(createSequencePage(chrPage), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getSequencesByAssemblyInsdcAccession(String insdcAccession, Pageable request) {
        Page<ChromosomeEntity> chrPage = chromosomeService.getChromosomesByAssemblyInsdcAccession(insdcAccession, request);
        return generatePagedModelFromPage(createSequencePage(chrPage), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getSequencesByAssemblyRefseq(String refseq, Pageable request) {
        Page<ChromosomeEntity> chrPage = chromosomeService.getChromosomesByAssemblyRefseq(refseq, request);
        return generatePagedModelFromPage(createSequencePage(chrPage), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getSequencesByAssemblyAccession(String accession,
                                                                                   Pageable request) {
        Page<ChromosomeEntity> chrPage = chromosomeService.getChromosomesByAssemblyAccession(accession, request);
        return generatePagedModelFromPage(createSequencePage(chrPage), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getSequencesBySequenceNameAndAssemblyTaxid(
            String name, long taxid, String nameType, Pageable request) {
        Page<ChromosomeEntity> chrPage;
        if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
            chrPage = chromosomeService.getChromosomesByUcscNameAndAssemblyTaxid(name, taxid, request);
        } else if (nameType.equals(ContigAliasController.NAME_ENA_TYPE)) {
            chrPage = chromosomeService.getChromosomesByEnaNameAndAssemblyTaxid(name, taxid, request);
        } else {
            chrPage = chromosomeService.getChromosomesByNameAndAssemblyTaxid(name, taxid, request);
        }
        return generatePagedModelFromPage(createSequencePage(chrPage), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getSequencesBySequenceNameAndAssemblyAccession(
            String name, String accession, String nameType, Pageable request) {
        Page<ChromosomeEntity> chrPage = new PageImpl<>(new ArrayList<>());
        Optional<AssemblyEntity> assembly = assemblyService.getAssemblyByAccession(accession);
        if (assembly.isPresent()) {
            AssemblyEntity assemblyEntity = assembly.get();
            if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
                chrPage = chromosomeService.getChromosomesByUcscNameAndAssembly(name, assemblyEntity, request);
            } else if (nameType.equals(ContigAliasController.NAME_ENA_TYPE)) {
                chrPage = chromosomeService.getChromosomesByEnaNameAndAssembly(name, assemblyEntity, request);
            } else {
                chrPage = chromosomeService.getChromosomesByNameAndAssembly(name, assemblyEntity, request);
            }
        }
        return generatePagedModelFromPage(createSequencePage(chrPage), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getSequencesByName(
            String name, String nameType, Pageable request) {
        Page<ChromosomeEntity> chrPage;
        if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
            chrPage = chromosomeService.getChromosomesByUcscName(name, request);
        } else if (nameType.equals(ContigAliasController.NAME_ENA_TYPE)) {
            chrPage = chromosomeService.getChromosomesByEnaName(name, request);
        } else {
            chrPage = chromosomeService.getChromosomesByName(name, request);
        }
        return generatePagedModelFromPage(createSequencePage(chrPage), sequenceAssembler);
    }

    private Page<SequenceEntity> createSequencePage(Page<? extends SequenceEntity> page) {
        return new PageImpl<>(page.getContent().stream().map(e -> (SequenceEntity)e)
                .collect(Collectors.toList()), page.getPageable(), page.getTotalElements());
    }
}
