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
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.eva.contigalias.controller.BaseHandler.convertToPage;
import static uk.ac.ebi.eva.contigalias.controller.BaseHandler.generatePagedModelFromPage;

@Service
public class ContigAliasHandler {

    private final AssemblyService assemblyService;

    private final ChromosomeService chromosomeService;

    private final PagedResourcesAssembler<AssemblyEntity> assemblyAssembler;

    private final PagedResourcesAssembler<ChromosomeEntity> chromosomeAssembler;

    @Autowired
    public ContigAliasHandler(AssemblyService assemblyService,
                              ChromosomeService chromosomeService,
                              PagedResourcesAssembler<AssemblyEntity> assemblyAssembler,
                              PagedResourcesAssembler<ChromosomeEntity> chromosomeAssembler) {
        this.assemblyService = assemblyService;
        this.chromosomeService = chromosomeService;
        this.assemblyAssembler = assemblyAssembler;
        this.chromosomeAssembler = chromosomeAssembler;
    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssemblyByAccession(String accession) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByAccession(accession);
        return generatePagedModelFromPage(convertToPage(entity), assemblyAssembler);
    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssemblyByGenbank(String genbank) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByGenbank(genbank);
        return generatePagedModelFromPage(convertToPage(entity), assemblyAssembler);
    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssemblyByRefseq(String refseq) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByRefseq(refseq);
        return generatePagedModelFromPage(convertToPage(entity), assemblyAssembler);

    }

    public PagedModel<EntityModel<AssemblyEntity>> getAssembliesByTaxid(long taxid, Pageable request) {
        Page<AssemblyEntity> page = assemblyService.getAssembliesByTaxid(taxid, request);
        return generatePagedModelFromPage(page, assemblyAssembler);
    }

    public Optional<AssemblyEntity> getAssemblyByChromosomeGenbank(String genbank) {
        return chromosomeService.getAssemblyByChromosomeGenbank(genbank);
    }

    public Optional<AssemblyEntity> getAssemblyByChromosomeRefseq(String refseq) {
        return chromosomeService.getAssemblyByChromosomeRefseq(refseq);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomeByGenbank(String genbank) {
        Optional<ChromosomeEntity> entity = chromosomeService.getChromosomeByGenbank(genbank);
        return generatePagedModelFromPage(convertToPage(entity), chromosomeAssembler);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomeByRefseq(String refseq) {
        Optional<ChromosomeEntity> entity = chromosomeService.getChromosomeByRefseq(refseq);
        return generatePagedModelFromPage(convertToPage(entity), chromosomeAssembler);
    }

    public List<ChromosomeEntity> getChromosomesByAssemblyGenbank(String genbank) {
        return chromosomeService.getChromosomesByAssemblyGenbank(genbank);
    }

    public List<ChromosomeEntity> getChromosomesByAssemblyRefseq(String refseq) {
        return chromosomeService.getChromosomesByAssemblyRefseq(refseq);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomesByChromosomeNameAndAssemblyTaxid(
            String name,
            long taxid,
            Pageable request) {
        Page<ChromosomeEntity> page = chromosomeService.getChromosomesByNameAndAssemblyTaxid(name, taxid, request);
        return generatePagedModelFromPage(page, chromosomeAssembler);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomesByChromosomeNameAndAssemblyAccession(
            String name,
            String accession,
            Pageable request) {
        Page<ChromosomeEntity> page = new PageImpl<>(Collections.emptyList());
        Optional<AssemblyEntity> assembly = assemblyService.getAssemblyByAccession(accession);
        if (assembly.isPresent()) {
            page = chromosomeService.getChromosomesByNameAndAssembly(name, assembly.get(), request);

        }
        return generatePagedModelFromPage(page, chromosomeAssembler);
    }

    public PagedModel<EntityModel<ChromosomeEntity>> getChromosomesByName(String name, Pageable request) {
        Page<ChromosomeEntity> page = chromosomeService.getChromosomesByName(name, request);
        return generatePagedModelFromPage(page, chromosomeAssembler);
    }
}
