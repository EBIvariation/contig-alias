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

package uk.ac.ebi.eva.contigalias.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.service.AliasService;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.BAD_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.PAGE_NUMBER_DESCRIPTION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.PAGE_SIZE_DESCRIPTION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.buildPageMetadata;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createAppropriateResponseEntity;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createPageRequest;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.getNoContentPagedModelResponse;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.paramsValidForSingleResponseQuery;

@RequestMapping("contig-alias")
@RestController
public class ContigAliasController {

    private final AssemblyService assemblyService;

    private final ChromosomeService chromosomeService;

    private final AliasService aliasService;

    @Autowired
    public ContigAliasController(AssemblyService assemblyService, ChromosomeService chromosomeService,
                                 AliasService aliasService) {
        this.assemblyService = assemblyService;
        this.chromosomeService = chromosomeService;
        this.aliasService = aliasService;
    }

    @ApiOperation(value = "Get an assembly using its GenBank or RefSeq accession. ",
            notes = "Given an assembly's accession, this endpoint will return an assembly that matches that accession" +
                    ". The accession can be either a GenBank or RefSeq accession and the endpoint will automatically " +
                    "fetch a result from the database for any assembly having the accession as its GenBank or RefSeq " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404.")
    @GetMapping(value = "v1/assemblies/{accession}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssemblyByAccession(
            @PathVariable @ApiParam(value = "Genbank or Refseq assembly accession. Eg: GCA_000001405.10") String accession,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            List<AssemblyEntity> entities = assemblyService.getAssemblyByAccession(accession);
            return createAppropriateResponseEntity(entities);
        } else return BaseController.BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its GenBank accession.",
            notes = "Given an assembly's GenBank accession, this endpoint will return an assembly that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404.")
    @GetMapping(value = "v1/assemblies/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssemblyByGenbank(
            @PathVariable @ApiParam(value = "Genbank assembly accession. Eg: GCA_000001405.10") String genbank,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            List<AssemblyEntity> entities = assemblyService.getAssemblyByGenbank(genbank);
            return createAppropriateResponseEntity(entities);
        } else return BaseController.BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its RefSeq accession.",
            notes = "Given an assembly's RefSeq accession, this endpoint will return an assembly that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404.")
    @GetMapping(value = "v1/assemblies/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssemblyByRefseq(
            @PathVariable @ApiParam(value = "Refseq assembly accession. Eg: GCF_000001405.26") String refseq,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            List<AssemblyEntity> entities = assemblyService.getAssemblyByRefseq(refseq);
            return createAppropriateResponseEntity(entities);
        } else return BaseController.BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its Taxonomic ID.",
            notes = "Given an assembly's Taxonomic ID, this endpoint will return all assemblies that match the given " +
                    "Taxonomic ID. This endpoint will either return a list containing one or more assemblies or an " +
                    "HTTP status code of 404.")
    @GetMapping(value = "v1/assemblies/taxid/{taxid}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssembliesByTaxid(
            @PathVariable @ApiParam(value = "Taxonomic ID of a group of accessions. Eg: 9606") long taxid,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        Page<AssemblyEntity> page = assemblyService.getAssembliesByTaxid(taxid,
                                                                         createPageRequest(pageNumber, pageSize));
        long totalElements = page.getTotalElements();
        if (totalElements == 0) {
            return getNoContentPagedModelResponse(page, pageNumber, pageSize);
        } else {
            PagedModel.PageMetadata pageMetadata = buildPageMetadata(totalElements, pageNumber, pageSize);
            return new ResponseEntity(buildPagedResources(page.getContent(), taxid, pageMetadata), HttpStatus.OK);
        }
    }

    @ApiOperation(value = "Get an assembly using the genbank accession of one of its " +
            "chromosomes.")
    @GetMapping(value = "v1/assemblies/chromosome/genbank/{genbank}")
    public ResponseEntity<AssemblyEntity> getAssemblyByChromosomeGenbank
            (@PathVariable String genbank) {
        Optional<AssemblyEntity> entity = aliasService.getAssemblyByChromosomeGenbank(genbank);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Get an assembly using the refseq accession of one of its " +
            "chromosomes.")
    @GetMapping(value = "v1/assemblies/chromosome/refseq/{refseq}")
    public ResponseEntity<AssemblyEntity> getAssemblyByChromosomeRefseq(@PathVariable String refseq) {
        Optional<AssemblyEntity> entity = aliasService.getAssemblyByChromosomeRefseq(refseq);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Get an chromosome using its Genbank accession.",
            notes = "Given a chromosome's genbank accession this endpoint will return a chromosome that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP " +
                    "Response with error code 404.")
    @GetMapping(value = "v1/chromosomes/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<ChromosomeEntity> getChromosomeByGenbank(
            @PathVariable @ApiParam(value = "Genbank chromosome accession. Eg: CM000663.2") String genbank,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Optional<ChromosomeEntity> entities = chromosomeService.getChromosomeByGenbank(genbank);
            return createAppropriateResponseEntity(entities);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get an chromosome using its RefSeq accession.",
            notes = "Given a chromosome's RefSeq accession, this endpoint will return a chromosome that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 400 in case the user is trying to insert an assembly that already exists in the local " +
                    "database.")
    @GetMapping(value = "v1/chromosomes/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<ChromosomeEntity> getChromosomeByRefseq(
            @PathVariable @ApiParam(value = "Refseq chromosome accession. Eg: NC_000001.11") String refseq,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Optional<ChromosomeEntity> entity = chromosomeService.getChromosomeByRefseq(refseq);
            return createAppropriateResponseEntity(entity);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get chromosomes using the genbank accession of its parent assembly.")
    @GetMapping(value = "v1/assemblies/genbank/{genbank}/chromosomes", produces = "application/json")
    public ResponseEntity<List<ChromosomeEntity>> getChromosomesByAssemblyGenbank(@PathVariable String genbank) {
        List<ChromosomeEntity> entities = aliasService.getChromosomesByAssemblyGenbank(genbank);
        if (entities != null && !entities.isEmpty()) {
            return new ResponseEntity<>(entities, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Get chromosomes using the refseq accession of its parent assembly.")
    @GetMapping(value = "v1/assemblies/refseq/{refseq}/chromosomes", produces = "application/json")
    public ResponseEntity<List<ChromosomeEntity>> getChromosomesByAssemblyRefseq(@PathVariable String refseq) {
        List<ChromosomeEntity> entities = aliasService.getChromosomesByAssemblyRefseq(refseq);
        if (entities != null && !entities.isEmpty()) {
            return new ResponseEntity<>(entities, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    private <T> PagedModel<T> buildPagedResources(List<T> entities, long taxid, PagedModel.PageMetadata pageMetadata) {

        PagedModel<T> pagedResources = new PagedModel<>(entities, pageMetadata);

        int pageNumber = (int) pageMetadata.getNumber();
        int pageSize = (int) pageMetadata.getSize();

        if (pageNumber > 0) {
            pagedResources.add(createPaginationLink(taxid, pageNumber - 1, pageSize, "prev"));

            pagedResources.add(createPaginationLink(taxid, 0, pageSize, "first"));
        }

        if (pageNumber < (pageMetadata.getTotalPages() - 1)) {
            pagedResources.add(createPaginationLink(taxid, pageNumber + 1, pageSize, "next"));

            pagedResources.add(createPaginationLink(taxid, (int) pageMetadata.getTotalPages() - 1,
                                                    pageSize, "last"));
        }
        return pagedResources;
    }

    private Link createPaginationLink(long taxid, int pageNumber, int pageSize, String linkName) {
        return new Link(linkTo(methodOn(ContigAliasController.class).getAssembliesByTaxid(taxid, pageNumber, pageSize))
                                .toUriComponentsBuilder().toUriString(), linkName);
    }

}
