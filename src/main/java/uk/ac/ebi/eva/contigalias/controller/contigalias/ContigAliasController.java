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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
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

import static uk.ac.ebi.eva.contigalias.controller.BaseController.BAD_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.PAGE_NUMBER_DESCRIPTION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.PAGE_SIZE_DESCRIPTION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createAppropriateResponseEntity;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createPageRequest;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.paramsValidForSingleResponseQuery;

@RequestMapping("contig-alias/v1")
@RestController
public class ContigAliasController {

    private final AssemblyService assemblyService;

    private final ChromosomeService chromosomeService;

    private final AliasService aliasService;

    private final PagedResourcesAssembler<AssemblyEntity> assemblyAssembler;

    private final PagedResourcesAssembler<ChromosomeEntity> chromosomeAssembler;

    @Autowired
    public ContigAliasController(AssemblyService assemblyService,
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

    @ApiOperation(value = "Get an assembly using its GenBank or RefSeq accession. ",
            notes = "Given an assembly's accession, this endpoint will return an assembly that matches that accession" +
                    ". The accession can be either a GenBank or RefSeq accession and the endpoint will automatically " +
                    "fetch a result from the database for any assembly having the accession as its GenBank or RefSeq " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404.")
    @GetMapping(value = "assemblies/{accession}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByAccession(
            @PathVariable @ApiParam(value = "Genbank or Refseq assembly accession. Eg: GCA_000001405.10") String accession,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Page<AssemblyEntity> page = assemblyService.getAssemblyByAccession(accession, DEFAULT_PAGE_REQUEST);
            return createAppropriateResponseEntity(page, assemblyAssembler);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its GenBank accession.",
            notes = "Given an assembly's GenBank accession, this endpoint will return an assembly that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404.")
    @GetMapping(value = "assemblies/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByGenbank(
            @PathVariable @ApiParam(value = "Genbank assembly accession. Eg: GCA_000001405.10") String genbank,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Page<AssemblyEntity> page = assemblyService.getAssemblyByGenbank(genbank, DEFAULT_PAGE_REQUEST);
            return createAppropriateResponseEntity(page, assemblyAssembler);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its RefSeq accession.",
            notes = "Given an assembly's RefSeq accession, this endpoint will return an assembly that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404.")
    @GetMapping(value = "assemblies/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByRefseq(
            @PathVariable @ApiParam(value = "Refseq assembly accession. Eg: GCF_000001405.26") String refseq,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Page<AssemblyEntity> page = assemblyService.getAssemblyByRefseq(refseq, DEFAULT_PAGE_REQUEST);
            return createAppropriateResponseEntity(page, assemblyAssembler);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its Taxonomic ID.",
            notes = "Given an assembly's Taxonomic ID, this endpoint will return all assemblies that match the given " +
                    "Taxonomic ID. This endpoint will either return a list containing one or more assemblies or an " +
                    "HTTP status code of 404.")
    @GetMapping(value = "assemblies/taxid/{taxid}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssembliesByTaxid(
            @PathVariable @ApiParam(value = "Taxonomic ID of a group of accessions. Eg: 9606") long taxid,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        Page<AssemblyEntity> page = assemblyService.getAssembliesByTaxid(taxid,
                                                                         createPageRequest(pageNumber, pageSize));
        return createAppropriateResponseEntity(page, assemblyAssembler);
    }

    @ApiOperation(value = "Get an assembly using the genbank accession of one of its " +
            "chromosomes.")
    @GetMapping(value = "assemblies/chromosome/genbank/{genbank}")
    public ResponseEntity<AssemblyEntity> getAssemblyByChromosomeGenbank
            (@PathVariable String genbank) {
        Optional<AssemblyEntity> entity = aliasService.getAssemblyByChromosomeGenbank(genbank);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Get an assembly using the refseq accession of one of its " +
            "chromosomes.")
    @GetMapping(value = "assemblies/chromosome/refseq/{refseq}")
    public ResponseEntity<AssemblyEntity> getAssemblyByChromosomeRefseq(@PathVariable String refseq) {
        Optional<AssemblyEntity> entity = aliasService.getAssemblyByChromosomeRefseq(refseq);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Get an chromosome using its Genbank accession.",
            notes = "Given a chromosome's genbank accession this endpoint will return a chromosome that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP " +
                    "Response with error code 404.")
    @GetMapping(value = "chromosomes/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomeByGenbank(
            @PathVariable @ApiParam(value = "Genbank chromosome accession. Eg: CM000663.2") String genbank,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Page<ChromosomeEntity> page = chromosomeService.getChromosomeByGenbank(genbank, DEFAULT_PAGE_REQUEST);
            return createAppropriateResponseEntity(page, chromosomeAssembler);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get an chromosome using its RefSeq accession.",
            notes = "Given a chromosome's RefSeq accession, this endpoint will return a chromosome that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 400 in case the user is trying to insert an assembly that already exists in the local " +
                    "database.")
    @GetMapping(value = "chromosomes/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomeByRefseq(
            @PathVariable @ApiParam(value = "Refseq chromosome accession. Eg: NC_000001.11") String refseq,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Page<ChromosomeEntity> page = chromosomeService.getChromosomeByRefseq(refseq, DEFAULT_PAGE_REQUEST);
            return createAppropriateResponseEntity(page, chromosomeAssembler);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get chromosomes using the genbank accession of its parent assembly.")
    @GetMapping(value = "assemblies/genbank/{genbank}/chromosomes", produces = "application/json")
    public ResponseEntity<List<ChromosomeEntity>> getChromosomesByAssemblyGenbank(@PathVariable String genbank) {
        List<ChromosomeEntity> entities = aliasService.getChromosomesByAssemblyGenbank(genbank);
        if (entities != null && !entities.isEmpty()) {
            return new ResponseEntity<>(entities, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Get chromosomes using the refseq accession of its parent assembly.")
    @GetMapping(value = "assemblies/refseq/{refseq}/chromosomes", produces = "application/json")
    public ResponseEntity<List<ChromosomeEntity>> getChromosomesByAssemblyRefseq(@PathVariable String refseq) {
        List<ChromosomeEntity> entities = aliasService.getChromosomesByAssemblyRefseq(refseq);
        if (entities != null && !entities.isEmpty()) {
            return new ResponseEntity<>(entities, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

}
