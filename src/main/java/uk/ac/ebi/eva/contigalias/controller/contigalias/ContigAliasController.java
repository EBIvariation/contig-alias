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
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_NUMBER;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_SIZE;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.PAGE_NUMBER_DESCRIPTION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.PAGE_SIZE_DESCRIPTION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.REL_ASSEMBLY;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.REL_CHROMOSOMES;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createAppropriateResponseEntity;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createPageRequest;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.paramsValidForSingleResponseQuery;

@RequestMapping("/v1")
@RestController
public class ContigAliasController {

    public static final String AUTHORITY_INSDC = "insdc";

    public static final String AUTHORITY_REFSEQ = "refseq";

    public static final String AUTHORITY_NONE = "none";

    public static final String NAME_GENBANK_TYPE = "genbank";

    public static final String NAME_ENA_TYPE = "ena";

    public static final String NAME_UCSC_TYPE = "ucsc";

    private final ContigAliasHandler handler;

    @Autowired
    public ContigAliasController(ContigAliasHandler handler) {
        this.handler = handler;
    }

    public static void linkPagedModelGetSequencesByAssemblyAccession(
            String accession, Integer pageNumber, Integer pageSize, PagedModel<EntityModel<AssemblyEntity>> pagedModel,
            String authority) {
        if (pageNumber == null) {
            pageNumber = DEFAULT_PAGE_NUMBER;
        }
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        pagedModel.add(linkTo(methodOn(ContigAliasController.class)
                                      .getSequencesByAssemblyAccession(
                                              accession, authority, pageNumber, pageSize))
                               .withRel(REL_CHROMOSOMES));
    }

    @ApiOperation(value = "Get an assembly using its INSDC or RefSeq accession. ",
            notes = "Given an assembly's accession, this endpoint will return an assembly that matches that accession" +
                    ". The accession can be either a INSDC or RefSeq accession and the endpoint will automatically " +
                    "fetch a result from the database for any assembly having the accession as its INSDC or RefSeq " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404. ")
    @GetMapping(value = "assemblies/{accession}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByAccession(
            @PathVariable(name = "accession") @ApiParam(value = "INSDC or Refseq assembly accession. Eg: " +
                    "GCA_000001405.10") String asmAccession,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssemblyByAccession(asmAccession);
            linkPagedModelGetSequencesByAssemblyAccession(asmAccession, pageNumber, pageSize, pagedModel, "");
            return createAppropriateResponseEntity(pagedModel);
        } else return new ResponseEntity<>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @ApiOperation(value = "Get an assembly using its INSDC accession.",
            notes = "Given an assembly's INSDC accession, this endpoint will return an assembly that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404. ")
    @GetMapping(value = "assemblies/insdc/{insdc}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByInsdcAccession(
            @PathVariable(name = "insdc") @ApiParam(value = "INSDC assembly accession. Eg: GCA_000001405.10") String asmInsdcAccession,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssemblyByInsdcAccession(asmInsdcAccession);
            linkPagedModelGetSequencesByAssemblyAccession(
                    asmInsdcAccession, pageNumber, pageSize, pagedModel, AUTHORITY_INSDC);
            return createAppropriateResponseEntity(pagedModel);
        } else return new ResponseEntity<>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @ApiOperation(value = "Get an assembly using its RefSeq accession.",
            notes = "Given an assembly's RefSeq accession, this endpoint will return an assembly that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404. ")
    @GetMapping(value = "assemblies/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByRefseq(
            @PathVariable(name = "refseq") @ApiParam(value = "Refseq assembly accession. Eg: GCF_000001405.26") String asmRefseq,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssemblyByRefseq(asmRefseq);
            linkPagedModelGetSequencesByAssemblyAccession(
                    asmRefseq, pageNumber, pageSize, pagedModel, AUTHORITY_REFSEQ);
            return createAppropriateResponseEntity(pagedModel);
        } else return new ResponseEntity<>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @ApiOperation(value = "Get an assembly using its Taxonomic ID.",
            notes = "Given an assembly's Taxonomic ID, this endpoint will return all assemblies that match the given " +
                    "Taxonomic ID. This endpoint will either return a list containing one or more assemblies or an " +
                    "HTTP status code of 404. ")
    @GetMapping(value = "assemblies/taxid/{taxid}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssembliesByTaxid(
            @PathVariable(name = "taxid") @ApiParam(value = "Taxonomic ID of a group of accessions. Eg: 9606") long asmTaxid,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssembliesByTaxid(asmTaxid, pageRequest);
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get a list of assemblies using the GenBank accession of one of the chromosomes they have " +
            "in common.",
            notes = "Given a chromosome's GenBank accession, this endpoint will return a list of assemblies that are " +
                    "associated with a chromosome having the same GenBank accession as the one provided. This " +
                    "endpoint returns a list containing one or more assemblies. It also accepts two additional " +
                    "parameters (page and size) to control pagination of results. If the page number and/or page size" +
                    " are invalid then an HTTP status code of 416 is returned by this endpoint.")
    @GetMapping(value = "chromosomes/genbank/{genbank}/assemblies")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssembliesBySequenceGenbank
            (@PathVariable @ApiParam(value = "GenBank accession of the chromosomes.") String genbank,
             @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
             @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssembliesBySequenceInsdcAccession(genbank);
            return createAppropriateResponseEntity(pagedModel);
        } else return new ResponseEntity<>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @ApiOperation(value = "Get a list of assemblies using the RefSeq accession of one of the chromosomes they have " +
            "in common.",
            notes = "Given a chromosome's RefSeq accession, this endpoint will return a list of assemblies that are " +
                    "associated with a chromosome having the same RefSeq accession as the one provided. This " +
                    "endpoint returns a list containing one or more assemblies. It also accepts two additional " +
                    "parameters (page and size) to control pagination of results. If the page number and/or page size" +
                    " are invalid then an HTTP status code of 416 is returned by this endpoint.")
    @GetMapping(value = "chromosomes/refseq/{refseq}/assemblies")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssembliesBySequenceRefseq(
            @PathVariable @ApiParam(value = "RefSeq accession of the chromosomes.") String refseq,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssembliesBySequenceRefseq(refseq);
            return createAppropriateResponseEntity(pagedModel);
        } else return new ResponseEntity<>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @ApiOperation(value = "Get a list of chromosomes using their common GenBank accession.",
            notes = "Given a chromosome's GenBank accession, this endpoint will return a list of all chromosomes that" +
                    " match that accession. This endpoint will either return a list of chromosomes.")
    @GetMapping(value = "chromosomes/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<SequenceEntity>>> getSequencesByGenbank(
            @PathVariable @ApiParam(value = "Genbank chromosome accession. Eg: CM000663.2") String genbank,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<SequenceEntity>> pagedModel = handler.getSequencesByInsdcAccession(genbank, pageRequest);
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get a list of chromosomes using their common RefSeq accession.",
            notes = "Given a chromosome's RefSeq accession, this endpoint will return a list of all chromosomes that " +
                    "match that accession. This endpoint will either return a list of chromosomes.")
    @GetMapping(value = "chromosomes/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<SequenceEntity>>> getSequencesByRefseq(
            @PathVariable @ApiParam(value = "Refseq chromosome accession. Eg: NC_000001.11") String refseq,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<SequenceEntity>> pagedModel = handler.getSequencesByRefseq(refseq, pageRequest);
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get chromosomes using the accession of its parent assembly.",
            notes = "Given an assembly's INSDC or RefSeq accession, this endpoint will return a list of all the " +
                    "chromosomes that are associated with the assembly uniquely identified by the given accession. ")
    @GetMapping(value = "assemblies/{accession}/chromosomes", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<SequenceEntity>>> getSequencesByAssemblyAccession(
            @PathVariable(name = "accession") String asmAccession,
            @RequestParam(required = false, name = "authority") @ApiParam("Specify if the provided accession is a " +
                    "INSDC or a RefSeq accession. The acceptable param values are " + AUTHORITY_INSDC + " " +
                    "and " + AUTHORITY_REFSEQ + " respectively. If this parameter is omitted then the results having " +
                    "the given accession as either their INSDC or RefSeq accession are returned. This includes " +
                    "cases where the INSDC and RefSeq accessions are the same.") String asmAuthority,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (asmAccession == null || asmAccession.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<SequenceEntity>> pagedModel;
        if (asmAuthority != null && !asmAuthority.isEmpty()) {
            if (asmAuthority.toLowerCase().equals(AUTHORITY_INSDC)) {
                pagedModel = handler.getSequencesByAssemblyInsdcAccession(asmAccession, pageRequest);
                linkPagedModelGetAssemblyByAuthority(asmAccession, AUTHORITY_INSDC, pagedModel);
            } else if (asmAuthority.toLowerCase().equals(AUTHORITY_REFSEQ)) {
                pagedModel = handler.getSequencesByAssemblyRefseq(asmAccession, pageRequest);
                linkPagedModelGetAssemblyByAuthority(asmAccession, AUTHORITY_REFSEQ, pagedModel);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            pagedModel = handler.getSequencesByAssemblyAccession(asmAccession, pageRequest);
            linkPagedModelGetAssemblyByAuthority(asmAccession, AUTHORITY_NONE, pagedModel);
        }
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get chromosomes using the insdc accession of its parent assembly.",
            notes = "Given an assembly's INSDC accession, this endpoint will return a list of all the " +
                    "chromosomes that are associated with the assembly uniquely identified by the given accession. ")
    @GetMapping(value = "assemblies/genbank/{genbank}/chromosomes", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<SequenceEntity>>> getSequencesByAssemblyGenbank(
            @PathVariable String genbank,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<SequenceEntity>> pagedModel
                = handler.getSequencesByAssemblyInsdcAccession(genbank, pageRequest);
        linkPagedModelGetAssemblyByAuthority(genbank, AUTHORITY_INSDC, pagedModel);
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get chromosomes using the refseq accession of its parent assembly.",
            notes = "Given an assembly's RefSeq accession, this endpoint will return a list of all the " +
                    "chromosomes that are associated with the assembly uniquely identified by the given accession. ")
    @GetMapping(value = "assemblies/refseq/{refseq}/chromosomes", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<SequenceEntity>>> getSequencesByAssemblyRefseq(
            @PathVariable String refseq,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<SequenceEntity>> pagedModel
                = handler.getSequencesByAssemblyRefseq(refseq, pageRequest);
        linkPagedModelGetAssemblyByAuthority(refseq, AUTHORITY_REFSEQ, pagedModel);
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get chromosomes using a combination of their own name and the Taxonomic ID's of their " +
            "parent assemblies.",
            notes = "Given a chromosome's name and the Taxonomic ID or the GenBank/RefSeq accession of the assembly " +
                    "that it belongs to, this endpoint will return a non-empty list of chromosomes that satisfy the " +
                    "given parameters. If no Taxonomic ID or accession are provided then the endpoint returns a list " +
                    "of chromosomes which have the given name. Each chromosome will also have its parent assembly " +
                    "nested inside it. The endpoint will either return a list of chromosomes or it will return an " +
                    "HTTP error code 400 if invalid parameters are found.")
    @GetMapping(value = "chromosomes/name/{name}")
    public ResponseEntity<PagedModel<EntityModel<SequenceEntity>>> getSequencesBySequenceNameAndAssemblyTaxidOrAccession(
            @PathVariable @ApiParam(value = "Sequence name or UCSC style name of chromosome. Eg: HSCHR1_RANDOM_CTG5") String name,
            @RequestParam(required = false) @ApiParam(value = "Taxonomic ID of a group of accessions. Eg: 9606") Optional<Long> taxid,
            @RequestParam(required = false, name = "accession") @ApiParam(value = "Genbank or Refseq assembly " +
                    "accession. Eg: GCA_000001405.10") Optional<String> asmAccession,
            @RequestParam(required = false, name = "name") @ApiParam(value = "Specify if the provided name is an " +
                    "GenBank chromosome name, ENA name, or a UCSC style name. The acceptable param values are " +
                    NAME_GENBANK_TYPE + ", " + NAME_ENA_TYPE + ", and " +
                    NAME_UCSC_TYPE + " respectively. If this parameter is omitted then the name is assumed " +
                    "to be a " + NAME_GENBANK_TYPE + " name by default.") Optional<String> nameTypeOpt,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        boolean isNameValid = name != null && !name.isEmpty();
        boolean isTaxidValid = taxid.isPresent();
        boolean isAccessionValid = asmAccession.isPresent() && !asmAccession.get().isEmpty();
        if (!isNameValid || (isTaxidValid && isAccessionValid)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<SequenceEntity>> pagedModel;
        String nameType = nameTypeOpt.orElse(NAME_GENBANK_TYPE);
        if (!isTaxidValid && !isAccessionValid) {
            pagedModel = handler.getSequencesByName(name, nameType, pageRequest);
        } else if (isTaxidValid) {
            pagedModel = handler.getSequencesBySequenceNameAndAssemblyTaxid(
                    name, taxid.get(), nameType, pageRequest);
        } else {
            pagedModel = handler.getSequencesBySequenceNameAndAssemblyAccession(
                    name, asmAccession.get(), nameType, pageRequest);
        }
        return createAppropriateResponseEntity(pagedModel);

    }

    @ApiOperation(value = "Get a list of chromosomes using their MD5 checksum. ",
            notes = "Given a chromosome's MD5 checksum, this endpoint will return a list of chromosomes that are " +
                    "associated with the MD5 checksum provided. This endpoint returns a list containing one or more chromosomes. " +
                    "It also accepts two additional parameters (page and size) to control pagination of results. " +
                    "If the page number and/or page size are invalid then an HTTP status code of 416 is returned by this endpoint.")
    @GetMapping(value = "chromosomes/md5checksum/{md5Checksum}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<SequenceEntity>>> getSequencesByMD5Checksum(
            @PathVariable @ApiParam(value = "MD5 Checksum of chromosome Eg: 7b6e06758e53927330346e9e7cc00cce") String md5Checksum,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<SequenceEntity>> pagedModel = handler.getSequencesByMD5Checksum(md5Checksum, pageRequest);
        return createAppropriateResponseEntity(pagedModel);
    }

    private void linkPagedModelGetAssemblyByAuthority(
            String accession, String authority, PagedModel pagedModel) {
        ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> method;
        if (authority.equals(AUTHORITY_INSDC)) {
            method = methodOn(ContigAliasController.class)
                    .getAssemblyByInsdcAccession(accession, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
        } else if (authority.equals(AUTHORITY_REFSEQ)) {
            method = methodOn(ContigAliasController.class)
                    .getAssemblyByRefseq(accession, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
        } else {
            return;
        }
        pagedModel.add(linkTo(method).withRel(REL_ASSEMBLY));
    }

    @ApiOperation(value = "Search chromosome using its name", notes = "Given a chromosome's name/accession, " +
            "this endpoint will return a list of all chromosomes that match that name. " +
            "If provided, filtering will be done based on naming convention and assembly")
    @GetMapping(value = "search/chromosome/{name}",
            produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<SequenceEntity>>> searchChromosomeByName(
            @PathVariable @ApiParam(value = "Chromosome name. Eg: CM000663.2") String name,
            @RequestParam(required = false, name = "namingConvention")
            @ApiParam(value = "Chromosome naming convention. Eg: refseq") String namingConvention,
            @RequestParam(required = false, name = "assemblyAccession")
            @ApiParam(value = "Assembly accession. Eg: GCA_000001405.10") String assemblyAccession,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<SequenceEntity>> pagedModel = handler.searchChromosomeByName(name, namingConvention,
                assemblyAccession, pageRequest);
        return createAppropriateResponseEntity(pagedModel);
    }

}
