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
import uk.ac.ebi.eva.contigalias.entities.ScaffoldEntity;
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;
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

    private final PagedResourcesAssembler<SequenceEntity> sequenceAssembler;

    // TODO get rid of this
    private final PagedResourcesAssembler<ScaffoldEntity> scaffoldAssembler;

    @Autowired
    public ContigAliasHandler(AssemblyService assemblyService,
                              ChromosomeService chromosomeService,
                              ScaffoldService scaffoldService,
                              PagedResourcesAssembler<AssemblyEntity> assemblyAssembler,
                              PagedResourcesAssembler<SequenceEntity> sequenceAssembler,
                              PagedResourcesAssembler<ScaffoldEntity> scaffoldAssembler) {
        this.assemblyService = assemblyService;
        this.chromosomeService = chromosomeService;
        this.scaffoldService = scaffoldService;
        this.assemblyAssembler = assemblyAssembler;
        this.sequenceAssembler = sequenceAssembler;
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

    public PagedModel<EntityModel<SequenceEntity>> getChromosomesByGenbank(String genbank, Pageable request) {
        long count = chromosomeService.countChromosomeEntitiesByGenbank(genbank);
        List<Pageable>[] pageRequests = createScaffoldsPageRequest(count, request);
        List<Page<? extends SequenceEntity>> pages = new LinkedList<>();
        for (Pageable pageable : pageRequests[0]) {
            pages.add(chromosomeService.getChromosomesByGenbank(genbank, pageable));
        }
        for (Pageable pageable: pageRequests[1]){
            pages.add(scaffoldService.getScaffoldsByGenbank(genbank, pageable));
        }
        return generatePagedModelFromPage(createSequencePage(pages), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getChromosomesByRefseq(String refseq, Pageable request) {
        long count = chromosomeService.countChromosomeEntitiesByRefseq(refseq);
        List<Pageable>[] pageRequests = createScaffoldsPageRequest(count, request);
        List<Page<? extends SequenceEntity>> pages = new LinkedList<>();
        for (Pageable pageable : pageRequests[0]) {
            pages.add(chromosomeService.getChromosomesByRefseq(refseq, pageable));
        }
        for (Pageable pageable: pageRequests[1]){
            pages.add(scaffoldService.getScaffoldsByRefseq(refseq, pageable));
        }
        return generatePagedModelFromPage(createSequencePage(pages), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getChromosomesByAssemblyGenbank(String genbank, Pageable request) {
        long count = chromosomeService.countChromosomeEntitiesByAssembly_Genbank(genbank);
        List<Pageable>[] pageRequests = createScaffoldsPageRequest(count, request);
        List<Page<? extends SequenceEntity>> pages = new LinkedList<>();
        for (Pageable pageable : pageRequests[0]) {
            pages.add(chromosomeService.getChromosomesByAssemblyGenbank(genbank, pageable));
        }
        for (Pageable pageable: pageRequests[1]){
            pages.add(scaffoldService.getScaffoldsByAssemblyGenbank(genbank, pageable));
        }
        return generatePagedModelFromPage(createSequencePage(pages), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getChromosomesByAssemblyRefseq(String refseq, Pageable request) {
        long count = chromosomeService.countChromosomeEntitiesByAssembly_Refseq(refseq);
        List<Pageable>[] pageRequests = createScaffoldsPageRequest(count, request);
        List<Page<? extends SequenceEntity>> pages = new LinkedList<>();
        for (Pageable pageable : pageRequests[0]) {
            pages.add(chromosomeService.getChromosomesByAssemblyRefseq(refseq, pageable));
        }
        for (Pageable pageable: pageRequests[1]){
            pages.add(scaffoldService.getScaffoldsByAssemblyRefseq(refseq, pageable));
        }
        return generatePagedModelFromPage(createSequencePage(pages), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getChromosomesByAssemblyAccession(String accession,
                                                                                     Pageable request) {
        long count = chromosomeService.countChromosomeEntitiesByAssemblyGenbankOrAssemblyRefseq(accession, accession);
        List<Pageable>[] pageRequests = createScaffoldsPageRequest(count, request);
        List<Page<? extends SequenceEntity>> pages = new LinkedList<>();
        for (Pageable pageable : pageRequests[0]) {
            pages.add(chromosomeService.getChromosomesByAssemblyAccession(accession, pageable));
        }
        for (Pageable pageable: pageRequests[1]){
            pages.add(scaffoldService.getScaffoldsByAssemblyAccession(accession, pageable));
        }
        return generatePagedModelFromPage(createSequencePage(pages), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getChromosomesByChromosomeNameAndAssemblyTaxid(
            String name, long taxid, String nameType, Pageable request) {
        Page<? extends SequenceEntity> page;
        if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
            page = chromosomeService.getChromosomesByUcscNameAndAssemblyTaxid(name, taxid, request);
        } else {
            page = chromosomeService.getChromosomesByNameAndAssemblyTaxid(name, taxid, request);
        }
        return generatePagedModelFromPage(createSequencePage(page), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getChromosomesByChromosomeNameAndAssemblyAccession(
            String name, String accession, String nameType, Pageable request) {
        Page<? extends SequenceEntity> page = new PageImpl<>(Collections.emptyList());
        Optional<AssemblyEntity> assembly = assemblyService.getAssemblyByAccession(accession);
        if (assembly.isPresent()) {
            if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
                page = chromosomeService.getChromosomesByUcscNameAndAssembly(name, assembly.get(), request);
            } else {
                page = chromosomeService.getChromosomesByNameAndAssembly(name, assembly.get(), request);
            }
        }
        return generatePagedModelFromPage(createSequencePage(page), sequenceAssembler);
    }

    public PagedModel<EntityModel<SequenceEntity>> getChromosomesByName(
            String name, String nameType, Pageable request) {
        Page<? extends SequenceEntity> page;
        if (nameType.equals(ContigAliasController.NAME_UCSC_TYPE)) {
            page = chromosomeService.getChromosomesByUcscName(name, request);
        } else {
            page = chromosomeService.getChromosomesByName(name, request);
        }
        return generatePagedModelFromPage(createSequencePage(page), sequenceAssembler);
    }

    private Page<SequenceEntity> createSequencePage(Page<? extends SequenceEntity> page) {
        return createSequencePage(Collections.singletonList(page));
    }

    private Page<SequenceEntity> createSequencePage(List<Page<? extends SequenceEntity>> pages) {
        List<SequenceEntity> sequenceEntities = new LinkedList<>();
        for (Page<? extends SequenceEntity> page : pages) {
            sequenceEntities.addAll(page.toList());
        }
        return new PageImpl<>(sequenceEntities);
    }

    List<Pageable>[] createScaffoldsPageRequest(long totalChromosomes, Pageable request) {
        List<Pageable>[] result = new List[2];
        result[0] = new LinkedList<>();
        result[1] = new LinkedList<>();

        int currentPageSize = request.getPageSize(); // 10
        long maxFilledPageSize = request.getOffset() + currentPageSize; //30

        if (maxFilledPageSize > totalChromosomes/*27*/) {

            int totalChrPages = (int) ((totalChromosomes / currentPageSize) + 1); // 3
            int maxFilledChrPageSize = (totalChrPages) * currentPageSize; //30
            int chrResultOffset = (int) (totalChromosomes % currentPageSize); // 7
            int secondPageOffset = currentPageSize - chrResultOffset;

            if (maxFilledPageSize <= maxFilledChrPageSize){
                if (chrResultOffset != 0) {
                    result[0].add(PageRequest.of(request.getPageNumber(), chrResultOffset));
                }
                result[1].add(PageRequest.of(0, secondPageOffset));
            }else{
                int scaffoldPageNumber = (int) ((maxFilledPageSize - maxFilledChrPageSize) / currentPageSize);
                result[1].add(PageRequest.of(scaffoldPageNumber - 1, chrResultOffset));
                result[1].add(PageRequest.of(scaffoldPageNumber, secondPageOffset));
            }
        }else{
            result[0].add(request);
        }
        return result;
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
