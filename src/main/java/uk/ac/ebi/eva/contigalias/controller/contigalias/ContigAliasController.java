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

    public static final String NAME_SEQUENCE_TYPE = "sequence";

    public static final String NAME_UCSC_TYPE = "ucsc";

    private final ContigAliasHandler handler;

    @Autowired
    public ContigAliasController(ContigAliasHandler handler) {
        this.handler = handler;
    }

    public static void linkPagedModelGetChromosomesByAssemblyAccession(
            String accession, Integer pageNumber, Integer pageSize, PagedModel<EntityModel<AssemblyEntity>> pagedModel,
            String authority) {
        if (pageNumber == null) {
            pageNumber = DEFAULT_PAGE_NUMBER;
        }
        if (pageSize == null) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        pagedModel.add(
                linkTo(methodOn(ContigAliasController.class)
                               .getChromosomesByAssemblyAccession(accession, authority, pageNumber, pageSize))
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
            linkPagedModelGetChromosomesByAssemblyAccession(asmAccession, pageNumber, pageSize, pagedModel, "");
            return createAppropriateResponseEntity(pagedModel);
        } else return new ResponseEntity<>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @ApiOperation(value = "Get an assembly using its INSDC accession.",
            notes = "Given an assembly's INSDC accession, this endpoint will return an assembly that matches that " +
                    "accession. This endpoint will either return a list containing a single result or an HTTP status " +
                    "code of 404. ")
    @GetMapping(value = "assemblies/insdc/{insdc}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyByGenbank(
            @PathVariable(name = "insdc") @ApiParam(value = "INSDC assembly accession. Eg: GCA_000001405.10") String asmGenbank,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssemblyByGenbank(asmGenbank);
            linkPagedModelGetChromosomesByAssemblyAccession(
                    asmGenbank, pageNumber, pageSize, pagedModel, AUTHORITY_INSDC);
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
            linkPagedModelGetChromosomesByAssemblyAccession(
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

    @ApiOperation(value = "Get a list of assemblies using the INSDC accession of one of the chromosomes they have " +
            "in common.",
            notes = "Given a chromosome's INSDC accession, this endpoint will return a list of assemblies that are " +
                    "associated with a chromosome having the same INSDC accession as the one provided. This " +
                    "endpoint returns a list containing one or more assemblies. It also accepts two additional " +
                    "parameters (page and size) to control pagination of results. If the page number and/or page size" +
                    " are invalid then an HTTP status code of 416 is returned by this endpoint. ")
    @GetMapping(value = "chromosomes/insdc/{insdc}/assemblies")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssembliesByChromosomeGenbank(
            @PathVariable(name = "insdc") @ApiParam(value = "INSDC accession of the chromosomes. Eg: CM000663.2") String chrGenbank,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssembliesByChromosomeGenbank(chrGenbank);
            return createAppropriateResponseEntity(pagedModel);
        } else return new ResponseEntity<>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @ApiOperation(value = "Get a list of assemblies using the RefSeq accession of one of the chromosomes they have " +
            "in common.",
            notes = "Given a chromosome's RefSeq accession, this endpoint will return a list of assemblies that are " +
                    "associated with a chromosome having the same RefSeq accession as the one provided. This " +
                    "endpoint returns a list containing one or more assemblies. It also accepts two additional " +
                    "parameters (page and size) to control pagination of results. If the page number and/or page size" +
                    " are invalid then an HTTP status code of 416 is returned by this endpoint. ")
    @GetMapping(value = "chromosomes/refseq/{refseq}/assemblies")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssembliesByChromosomeRefseq(
            @PathVariable(name = "refseq") @ApiParam(value = "RefSeq accession of the chromosomes. Eg: NC_000001.11") String chrRefseq,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssembliesByChromosomeRefseq(chrRefseq);
            return createAppropriateResponseEntity(pagedModel);
        } else return new ResponseEntity<>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @ApiOperation(value = "Get a list of chromosomes using their common INSDC accession.",
            notes = "Given a chromosome's INSDC accession, this endpoint will return a list of all chromosomes that" +
                    " match that accession. This endpoint will either return a list of chromosomes. ")
    @GetMapping(value = "chromosomes/insdc/{insdc}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomesByGenbank(
            @PathVariable(name = "insdc") @ApiParam(value = "INSDC accession of the chromosomes. Eg: CM000663.2") String chrGenbank,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<ChromosomeEntity>> pagedModel = handler.getChromosomesByGenbank(chrGenbank, pageRequest);
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get a list of chromosomes using their common RefSeq accession.",
            notes = "Given a chromosome's RefSeq accession, this endpoint will return a list of all chromosomes that " +
                    "match that accession. This endpoint will either return a list of chromosomes. ")
    @GetMapping(value = "chromosomes/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomesByRefseq(
            @PathVariable(name = "refseq") @ApiParam(value = "RefSeq accession of the chromosomes. Eg: NC_000001.11") String chrRefseq,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<ChromosomeEntity>> pagedModel = handler.getChromosomesByRefseq(chrRefseq, pageRequest);
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get chromosomes using the accession of its parent assembly.",
            notes = "Given an assembly's INSDC or RefSeq accession, this endpoint will return a list of all the " +
                    "chromosomes that are associated with the assembly uniquely identified by the given accession. ")
    @GetMapping(value = "assemblies/{accession}/chromosomes", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomesByAssemblyAccession(
            @PathVariable(name = "accession") @ApiParam(value = "INSDC or Refseq assembly accession. Eg: " +
                    "GCA_000001405.10") String asmAccession,
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
        PagedModel<EntityModel<ChromosomeEntity>> pagedModel;
        if (asmAuthority != null && !asmAuthority.isEmpty()) {
            if (asmAuthority.toLowerCase().equals(AUTHORITY_INSDC)) {
                pagedModel = handler.getChromosomesByAssemblyGenbank(asmAccession, pageRequest);
                linkPagedModelGetAssemblyByAuthority(asmAccession, AUTHORITY_INSDC, pagedModel);
            } else if (asmAuthority.toLowerCase().equals(AUTHORITY_REFSEQ)) {
                pagedModel = handler.getChromosomesByAssemblyRefseq(asmAccession, pageRequest);
                linkPagedModelGetAssemblyByAuthority(asmAccession, AUTHORITY_REFSEQ, pagedModel);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            pagedModel = handler.getChromosomesByAssemblyAccession(asmAccession, pageRequest);
            linkPagedModelGetAssemblyByAuthority(asmAccession, AUTHORITY_NONE, pagedModel);
        }
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get chromosomes using the insdc accession of its parent assembly.",
            notes = "Given an assembly's INSDC accession, this endpoint will return a list of all the " +
                    "chromosomes that are associated with the assembly uniquely identified by the given accession. ")
    @GetMapping(value = "assemblies/insdc/{insdc}/chromosomes", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomesByAssemblyGenbank(
            @PathVariable(name = "insdc") @ApiParam(value = "INSDC assembly accession. Eg: GCA_000001405.10") String asmGenbank,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<ChromosomeEntity>> pagedModel
                = handler.getChromosomesByAssemblyGenbank(asmGenbank, pageRequest);
        linkPagedModelGetAssemblyByAuthority(asmGenbank, AUTHORITY_INSDC, pagedModel);
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get chromosomes using the refseq accession of its parent assembly.",
            notes = "Given an assembly's RefSeq accession, this endpoint will return a list of all the " +
                    "chromosomes that are associated with the assembly uniquely identified by the given accession. ")
    @GetMapping(value = "assemblies/refseq/{refseq}/chromosomes", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomesByAssemblyRefseq(
            @PathVariable(name = "refseq") @ApiParam(value = "Refseq assembly accession. Eg: GCF_000001405.26") String asmRefseq,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<ChromosomeEntity>> pagedModel
                = handler.getChromosomesByAssemblyRefseq(asmRefseq, pageRequest);
        linkPagedModelGetAssemblyByAuthority(asmRefseq, AUTHORITY_REFSEQ, pagedModel);
        return createAppropriateResponseEntity(pagedModel);
    }

    @ApiOperation(value = "Get chromosomes using a combination of their own name and the Taxonomic ID's of their " +
            "parent assemblies.",
            notes = "Given a chromosome's name and the Taxonomic ID or the GenBank/RefSeq accession of the assembly " +
                    "that it belongs to, this endpoint will return a non-emtpy list of chromosomes that satisfy the " +
                    "given parameters. If no Taxonomic ID or accession are provided then the endpoint returns a list " +
                    "of chromosomes which have the given name. Each chromosome will also have its parent assembly " +
                    "nested inside it. The endpoint will either return a list of chromosomes or it will return an " +
                    "HTTP error code 400 if invalid parameters are found.")
    @GetMapping(value = "chromosomes/name/{name}")
    public ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> getChromosomesByChromosomeNameAndAssemblyTaxidOrAccession(
            @PathVariable @ApiParam(value = "Sequence name or UCSC style name of chromosome. Eg: HSCHR1_RANDOM_CTG5") String name,
            @RequestParam(required = false) @ApiParam(value = "Taxonomic ID of a group of accessions. Eg: 9606") Optional<Long> taxid,
            @RequestParam(required = false, name = "accession") @ApiParam(value = "Genbank or Refseq assembly " +
                    "accession. Eg: GCA_000001405.10") Optional<String> asmAccession,
            @RequestParam(required = false, name = "name") @ApiParam(value = "Specify if the provided name is a " +
                    "sequence name or a UCSC style name. The acceptable param values are " + NAME_SEQUENCE_TYPE + " " +
                    "and " + NAME_UCSC_TYPE + " respectively. If this parameter is omitted then the name is assumed " +
                    "to be a " + NAME_SEQUENCE_TYPE + " name by default.") Optional<String> nameTypeOpt,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        boolean isNameValid = name != null && !name.isEmpty();
        boolean isTaxidValid = taxid.isPresent();
        boolean isAccessionValid = asmAccession.isPresent() && !asmAccession.get().isEmpty();
        if (!isNameValid || (isTaxidValid && isAccessionValid)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<ChromosomeEntity>> pagedModel;
        String nameType = nameTypeOpt.orElse(NAME_SEQUENCE_TYPE);
        if (!isTaxidValid && !isAccessionValid) {
            pagedModel = handler.getChromosomesByName(name, nameType, pageRequest);
        } else if (isTaxidValid) {
            pagedModel = handler.getChromosomesByChromosomeNameAndAssemblyTaxid(
                    name, taxid.get(), nameType, pageRequest);
        } else {
            pagedModel = handler.getChromosomesByChromosomeNameAndAssemblyAccession(
                    name, asmAccession.get(), nameType, pageRequest);
        }
        return createAppropriateResponseEntity(pagedModel);

    }

    private void linkPagedModelGetAssemblyByAuthority(
            String accession, String authority, PagedModel pagedModel) {
        ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> method;
        if (authority.equals(AUTHORITY_INSDC)) {
            method = methodOn(ContigAliasController.class)
                    .getAssemblyByGenbank(accession, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
        } else if (authority.equals(AUTHORITY_REFSEQ)) {
            method = methodOn(ContigAliasController.class)
                    .getAssemblyByRefseq(accession, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
        } else {
            return;
        }
        pagedModel.add(linkTo(method).withRel(REL_ASSEMBLY));
    }

    @ApiOperation(value = "Get scaffolds using a combination of their own name and the Taxonomic ID's of their " +
            "parent assemblies.",
            notes = "Given a scaffold's name and the Taxonomic ID or the INSDC/RefSeq accession of the assembly " +
                    "that it belongs to, this endpoint will return a non-emtpy list of scaffolds that satisfy the " +
                    "given parameters. If no Taxonomic ID or accession are provided then the endpoint returns a list " +
                    "of scaffolds which have the given name. Each scaffold will also have its parent assembly " +
                    "nested inside it. The endpoint will either return a list of scaffolds or it will return an " +
                    "HTTP error code 400 if invalid parameters are found.")
    @GetMapping(value = "scaffolds/name/{name}")
    public ResponseEntity<PagedModel<EntityModel<ScaffoldEntity>>> getScaffoldsByScaffoldNameAndAssemblyTaxidOrAccession(
            @PathVariable @ApiParam(value = "Sequence name or UCSC style name of scaffold. Eg: HSCHR1_RANDOM_CTG5") String name,
            @RequestParam(required = false) @ApiParam(value = "Taxonomic ID of a group of accessions. Eg: 9606") Optional<Long> taxid,
            @RequestParam(required = false, name = "accession") @ApiParam(value = "Genbank or Refseq assembly " +
                    "accession. Eg: GCA_000001405.10") Optional<String> asmAccession,
            @RequestParam(required = false, name = "name") @ApiParam(value = "Specify if the provided name is a " +
                    "sequence name or a UCSC style name. The acceptable param values are " + NAME_SEQUENCE_TYPE + " " +
                    "and " + NAME_UCSC_TYPE + " respectively. If this parameter is omitted then the name is assumed " +
                    "to be a " + NAME_SEQUENCE_TYPE + " name by default.") Optional<String> nameTypeOpt,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) {
        boolean isNameValid = chrName != null && !chrName.isEmpty();
        boolean isTaxidValid = asmTaxid.isPresent();
        boolean isAccessionValid = asmAccession.isPresent() && !asmAccession.get().isEmpty();
        if (!isNameValid || (isTaxidValid && isAccessionValid)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        PageRequest pageRequest = createPageRequest(pageNumber, pageSize);
        PagedModel<EntityModel<ScaffoldEntity>> pagedModel;
        String nameType = nameTypeOpt.orElse(NAME_SEQUENCE_TYPE);
        if (!isTaxidValid && !isAccessionValid) {
            pagedModel = handler.getScaffoldsByName(name, nameType, pageRequest);
        } else if (isTaxidValid) {
            pagedModel = handler.getScaffoldsByScaffoldNameAndAssemblyTaxid(
                    name, taxid.get(), nameType, pageRequest);
        } else {
            pagedModel = handler.getScaffoldsByScaffoldNameAndAssemblyAccession(
                    name, asmAccession.get(), nameType, pageRequest);
        }
        return createAppropriateResponseEntity(pagedModel);

    }

}
