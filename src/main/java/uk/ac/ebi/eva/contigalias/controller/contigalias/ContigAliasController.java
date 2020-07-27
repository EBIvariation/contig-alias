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
import org.springframework.data.domain.PageRequest;
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

import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.eva.contigalias.controller.BaseController.BAD_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.PAGE_NUMBER_DESCRIPTION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.PAGE_SIZE_DESCRIPTION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createAppropriateResponseEntity;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createPageRequest;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.paramsValidForSingleResponseQuery;

@RequestMapping("contig-alias/v1")
@RestController
public class ContigAliasController {

    private final ContigAliasHandler handler;

    @Autowired
    public ContigAliasController(ContigAliasHandler handler) {
        this.handler = handler;
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
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssemblyByAccession(accession);
            return createAppropriateResponseEntity(pagedModel);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its GenBank accession.",
            notes = "Given an assembly's GenBank accession, this endpoint will return an assembly that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404.")
    @GetMapping(value = "assemblies/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByGenbank(
            @PathVariable @ApiParam(value = "Genbank assembly accession. Eg: GCA_000001405.10") String genbank,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssemblyByGenbank(genbank);
            return createAppropriateResponseEntity(pagedModel);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its RefSeq accession.",
            notes = "Given an assembly's RefSeq accession, this endpoint will return an assembly that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404.")
    @GetMapping(value = "assemblies/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByRefseq(
            @PathVariable @ApiParam(value = "Refseq assembly accession. Eg: GCF_000001405.26") String refseq,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssemblyByRefseq(refseq);
            return createAppropriateResponseEntity(pagedModel);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its Taxonomic ID.",
            notes = "Given an assembly's Taxonomic ID, this endpoint will return all assemblies that match the given " +
                    "Taxonomic ID. This endpoint will either return a list containing one or more assemblies or an " +
                    "HTTP status code of 404.")
    @GetMapping(value = "assemblies/taxid/{taxid}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssembliesByTaxid(
            @PathVariable @ApiParam(value = "Taxonomic ID of a group of accessions. Eg: 9606") long taxid,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssembliesByTaxid(taxid, pageRequest);
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get an assembly using the genbank accession of one of its " +
            "chromosomes.")
    @GetMapping(value = "assemblies/chromosome/genbank/{genbank}")
    public ResponseEntity<AssemblyEntity> getAssemblyByChromosomeGenbank
            (@PathVariable String genbank) {
        Optional<AssemblyEntity> entity = handler.getAssemblyByChromosomeGenbank(genbank);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @ApiOperation(value = "Get an assembly using the refseq accession of one of its " +
            "chromosomes.")
    @GetMapping(value = "assemblies/chromosome/refseq/{refseq}")
    public ResponseEntity<AssemblyEntity> getAssemblyByChromosomeRefseq(@PathVariable String refseq) {
        Optional<AssemblyEntity> entity = handler.getAssemblyByChromosomeRefseq(refseq);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NO_CONTENT));
    }

    @ApiOperation(value = "Get a chromosome using its Genbank accession.",
            notes = "Given a chromosome's genbank accession this endpoint will return a chromosome that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP " +
                    "Response with error code 404.")
    @GetMapping(value = "chromosomes/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomeByGenbank(
            @PathVariable @ApiParam(value = "Genbank chromosome accession. Eg: CM000663.2") String genbank,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<ChromosomeEntity>> pagedModel = handler.getChromosomeByGenbank(genbank);
            return createAppropriateResponseEntity(pagedModel);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get a chromosome using its RefSeq accession.",
            notes = "Given a chromosome's RefSeq accession, this endpoint will return a chromosome that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 400 in case the user is trying to insert an assembly that already exists in the local " +
                    "database.")
    @GetMapping(value = "chromosomes/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomeByRefseq(
            @PathVariable @ApiParam(value = "Refseq chromosome accession. Eg: NC_000001.11") String refseq,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<ChromosomeEntity>> pagedModel = handler.getChromosomeByRefseq(refseq);
            return createAppropriateResponseEntity(pagedModel);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get chromosomes using the accession of its parent assembly.")
    @GetMapping(value = "assemblies/{accession}/chromosomes", produces = "application/json")
    public ResponseEntity<List<ChromosomeEntity>> getChromosomesByAssemblyAccession(
            @PathVariable String accession,
            @RequestParam(required = false) String authority) {
        if (accession == null || accession.isEmpty()) {
            return BAD_REQUEST;
        }
        List<ChromosomeEntity> entities;
        if (authority != null && !authority.isEmpty()) {
            if ("genbank".equals(authority.toLowerCase())) {
                entities = handler.getChromosomesByAssemblyGenbank(accession);
            } else if ("refseq".equals(authority.toLowerCase())) {
                entities = handler.getChromosomesByAssemblyRefseq(accession);
            } else {
                return BAD_REQUEST;
            }
        } else {
            entities = handler.getChromosomesByAssemblyAccession(accession);
        }
        return createAppropriateResponseEntity(entities);
    }

    @ApiOperation(value = "Get chromosomes using the genbank accession of its parent assembly.")
    @GetMapping(value = "assemblies/genbank/{genbank}/chromosomes", produces = "application/json")
    public ResponseEntity<List<ChromosomeEntity>> getChromosomesByAssemblyGenbank(@PathVariable String genbank) {
        List<ChromosomeEntity> entities = handler.getChromosomesByAssemblyGenbank(genbank);
        return createAppropriateResponseEntity(entities);
    }

    @ApiOperation(value = "Get chromosomes using the refseq accession of its parent assembly.")
    @GetMapping(value = "assemblies/refseq/{refseq}/chromosomes", produces = "application/json")
    public ResponseEntity<List<ChromosomeEntity>> getChromosomesByAssemblyRefseq(@PathVariable String refseq) {
        List<ChromosomeEntity> entities = handler.getChromosomesByAssemblyRefseq(refseq);
        return createAppropriateResponseEntity(entities);
    }

    @ApiOperation(value = "Get chromosomes using a combination of their own name and the Taxonomic ID's of their " +
            "parent assemblies.",
            notes = "Given a chromosome's name and the Taxonomic ID or the GenBank/RefSeq accession of the assembly " +
                    "that it belongs to, this endpoint will return a non-emtpy list of chromosomes that satisfy the " +
                    "given parameters.If no Taxonomic ID or accession are provided then the endpoint returns a list " +
                    "of chromosomes which have the given name. Each chromosome will also have its parent assembly " +
                    "nested inside it. The endpoint will either return a list of chromosomes or it will either return" +
                    " an HTTP error code 204 if no chromosomes are found or return an HTTP error code 400 if invalid " +
                    "parameters are found.")
    @GetMapping(value = "chromosomes/{name}")
    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomesByChromosomeNameAndAssemblyTaxidOrAccession(
            @PathVariable @ApiParam(value = "Name of chromosome. Eg: HSCHR1_RANDOM_CTG5") String name,
            @RequestParam(required = false) @ApiParam(value = "Taxonomic ID of a group of accessions. Eg: 9606") Optional<Long> taxid,
            @RequestParam(required = false) @ApiParam(value = "Genbank or Refseq assembly accession. Eg: " +
                    "GCA_000001405.10") Optional<String> accession,
            @RequestParam(required = false) @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false) @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        boolean isNameValid = name != null && !name.isEmpty();
        boolean isTaxidValid = taxid.isPresent();
        boolean isAccessionValid = accession.isPresent() && !accession.get().isEmpty();
        if (!isNameValid || (isTaxidValid && isAccessionValid)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<ChromosomeEntity>> pagedModel;
        if (!isTaxidValid && !isAccessionValid) {
            pagedModel = handler.getChromosomesByName(name, pageRequest);
        } else if (isTaxidValid) {
            pagedModel = handler.getChromosomesByChromosomeNameAndAssemblyTaxid(name, taxid.get(), pageRequest);
        } else {
            pagedModel = handler.getChromosomesByChromosomeNameAndAssemblyAccession(name, accession.get(), pageRequest);
        }
        return createAppropriateResponseEntity(pagedModel);

    }

}