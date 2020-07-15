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
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.service.AliasService;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.eva.contigalias.controller.BaseController.BAD_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createAppropriateResponseEntity;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createPageRequest;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.paramsValidForSingleResponseQuery;

@Service
public class ContigAliasHandler {

    private final AssemblyService assemblyService;

    private final ChromosomeService chromosomeService;

    private final AliasService aliasService;

    private final PagedResourcesAssembler<AssemblyEntity> assemblyAssembler;

    private final PagedResourcesAssembler<ChromosomeEntity> chromosomeAssembler;

    @Autowired
    public ContigAliasHandler(AssemblyService assemblyService,
                              ChromosomeService chromosomeService,
                              AliasService aliasService,
                              PagedResourcesAssembler<AssemblyEntity> assemblyAssembler,
                              PagedResourcesAssembler<ChromosomeEntity> chromosomeAssembler) {
        this.assemblyService = assemblyService;
        this.chromosomeService = chromosomeService;
        this.aliasService = aliasService;
        this.assemblyAssembler = assemblyAssembler;
        this.chromosomeAssembler = chromosomeAssembler;
    }

    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByAccession(
            String accession,
            Integer pageNumber,
            Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Page<AssemblyEntity> page = assemblyService.getAssemblyByAccession(accession, DEFAULT_PAGE_REQUEST);
            return createAppropriateResponseEntity(page, assemblyAssembler);
        } else return BAD_REQUEST;
    }

    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByGenbank(
            String genbank,
            Integer pageNumber,
            Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Page<AssemblyEntity> page = assemblyService.getAssemblyByGenbank(genbank, DEFAULT_PAGE_REQUEST);
            return createAppropriateResponseEntity(page, assemblyAssembler);
        } else return BAD_REQUEST;
    }

    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByRefseq(
            String refseq,
            Integer pageNumber,
            Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Page<AssemblyEntity> page = assemblyService.getAssemblyByRefseq(refseq, DEFAULT_PAGE_REQUEST);
            return createAppropriateResponseEntity(page, assemblyAssembler);
        } else return BAD_REQUEST;
    }

    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssembliesByTaxid(
            long taxid,
            Integer pageNumber,
            Integer pageSize) {
        Page<AssemblyEntity> page = assemblyService.getAssembliesByTaxid(taxid,
                                                                         createPageRequest(pageNumber, pageSize));
        return createAppropriateResponseEntity(page, assemblyAssembler);
    }

    public ResponseEntity<AssemblyEntity> getAssemblyByChromosomeGenbank(String genbank) {
        Optional<AssemblyEntity> entity = aliasService.getAssemblyByChromosomeGenbank(genbank);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<AssemblyEntity> getAssemblyByChromosomeRefseq(String refseq) {
        Optional<AssemblyEntity> entity = aliasService.getAssemblyByChromosomeRefseq(refseq);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomeByGenbank(
            String genbank,
            Integer pageNumber,
            Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Page<ChromosomeEntity> page = chromosomeService.getChromosomeByGenbank(genbank, DEFAULT_PAGE_REQUEST);
            return createAppropriateResponseEntity(page, chromosomeAssembler);
        } else return BAD_REQUEST;
    }

    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomeByRefseq(
            String refseq,
            Integer pageNumber,
            Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Page<ChromosomeEntity> page = chromosomeService.getChromosomeByRefseq(refseq, DEFAULT_PAGE_REQUEST);
            return createAppropriateResponseEntity(page, chromosomeAssembler);
        } else return BAD_REQUEST;
    }

    public ResponseEntity<List<ChromosomeEntity>> getChromosomesByAssemblyGenbank(String genbank) {
        List<ChromosomeEntity> entities = aliasService.getChromosomesByAssemblyGenbank(genbank);
        if (entities != null && !entities.isEmpty()) {
            return new ResponseEntity<>(entities, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<List<ChromosomeEntity>> getChromosomesByAssemblyRefseq(String refseq) {
        List<ChromosomeEntity> entities = aliasService.getChromosomesByAssemblyRefseq(refseq);
        if (entities != null && !entities.isEmpty()) {
            return new ResponseEntity<>(entities, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }


}
