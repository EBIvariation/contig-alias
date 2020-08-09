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

package uk.ac.ebi.eva.contigalias.controller.admin;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;

import java.io.IOException;
import java.util.List;

import static uk.ac.ebi.eva.contigalias.controller.BaseController.HATEOAS_EXPLANATION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.PAGE_NUMBER_DESCRIPTION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.PAGE_SIZE_DESCRIPTION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.PAGINATION_EXPLANATION;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createAppropriateResponseEntity;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.paramsValidForSingleResponseQuery;
import static uk.ac.ebi.eva.contigalias.controller.contigalias.ContigAliasController.linkPagedModelGetChromosomesByAssemblyAccession;

@RequestMapping("/v1/admin")
@RestController
public class AdminController {

    private final AdminHandler handler;

    public AdminController(AdminHandler handler) {
        this.handler = handler;
    }

    @ApiOperation(value = "Get or fetch an assembly using its GenBank or RefSeq accession.",
            notes = "Given an assembly's accession, this endpoint will return an assembly that matches that accession" +
                    ". The accession can be either a GenBank or a RefSeq accession and the software will " +
                    "automatically fetch a result from the database for any assembly having the aforementioned " +
                    "accession as its GenBank or RefSeq accession. This endpoint will first look for the assembly in " +
                    "local database and return the result. If local search fails, it will search for the target " +
                    "assembly at a remote source (NCBI by default). If the desired assembly is found at the remote " +
                    "source, it will fetch and add it to the local database and also return the result to the user. " +
                    "This endpoint will either return a list containing a single result or an HTTP status code of 404. "
                    + PAGINATION_EXPLANATION + " " + HATEOAS_EXPLANATION)
    @GetMapping(value = "assemblies/{accession}", produces = "application/json")
    public ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> getAssemblyOrFetchByAccession(
            @PathVariable @ApiParam(value = "Genbank or Refseq assembly accession. Eg: GCA_000001405.10") String accession,
            @RequestParam(required = false, name = "page") @ApiParam(value = PAGE_NUMBER_DESCRIPTION) Integer pageNumber,
            @RequestParam(required = false, name = "size") @ApiParam(value = PAGE_SIZE_DESCRIPTION) Integer pageSize) throws IOException {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = handler.getAssemblyOrFetchByAccession(accession);
            linkPagedModelGetChromosomesByAssemblyAccession(accession, pageNumber, pageSize, pagedModel, "");
            return createAppropriateResponseEntity(pagedModel);
        } else return new ResponseEntity<>(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @ApiOperation(value = "Fetch an assembly from remote server using its GenBank or RefSeq accession and insert " +
            "into local database.",
            notes = "Given an assembly's accession, this endpoint will fetch and add the assembly that matches that " +
                    "accession into the local database. The accession can be either a GenBank or a RefSeq accession " +
                    "and the endpoint will automatically fetch the correct assembly from remote server. It will first" +
                    " search for the target assembly in the local database as trying to insert an assembly which " +
                    "already exists in the database is prohibited. If such an assembly is not found locally then it " +
                    "will look for it at a remote source (NCBI by default). If the desired assembly is found at the " +
                    "remote source, it will fetch it and add it to the local database. This endpoint does not return " +
                    "any data except an HTTP status code of 400 in case the user tries to insert an assembly that " +
                    "already exists in the local database.")
    @PutMapping(value = "assemblies/{accession}")
    public ResponseEntity<?> fetchAndInsertAssemblyByAccession(
            @PathVariable @ApiParam(value = "GenBank or RefSeq assembly accession. Eg: GCA_000001405.10") String accession) throws IOException {
        try {
            handler.fetchAndInsertAssemblyByAccession(accession);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Fetch assemblies from remote server using their GenBank or RefSeq accessions and insert " +
            "into local database.",
            notes = "Given a list of assembly accessions, for every accession in the list this endpoint will fetch " +
                    "and add the assembly that matches that accession into the local database. The accession can be " +
                    "either a GenBank or RefSeq accession and the endpoint will automatically fetch the correct " +
                    "assembly from remote server. It will first search for the target assembly in the local database " +
                    "as trying to insert an assembly which already exists in the database is prohibited. If such an " +
                    "assembly is not found locally then it will look for it at a remote source (NCBI by default). If " +
                    "desired assembly is found at remote source, it will fetch and add it to the local database. This" +
                    " endpoint does not return any data and processes elements in the given list in an asynchronous " +
                    "parallel manner.")
    @PutMapping(value = "assemblies")
    public ResponseEntity<?> fetchAndInsertAssemblyByAccession(
            @RequestBody(required = false) @ApiParam(value = "A JSON array of GenBank or RefSeq assembly accessions. " +
                    "Eg: [\"GCA_000001405.10\",\"GCA_000001405.11\",\"GCA_000001405.12\"]") List<String> accessions) {
        if (accessions == null || accessions.size() <= 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        handler.fetchAndInsertAssemblyByAccession(accessions);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete an assembly from local database using its GenBank or RefSeq accession.",
            notes = "Given an assembly's accession this endpoint will delete the assembly that matches that " +
                    "accession from the local database. The accession can be either a GenBank or RefSeq accession and" +
                    " the endpoint will automatically deletes the correct assembly from the database. Deleting an " +
                    "assembly also deletes all chromosomes that are associated with that assembly. This endpoint does" +
                    " not return any data.")
    @DeleteMapping(value = "assemblies/{accession}")
    public void deleteAssemblyByAccession(
            @PathVariable @ApiParam(value = "GenBank or RefSeq assembly accession. Eg: GCA_000001405.10") String accession) {
        handler.deleteAssemblyByAccession(accession);
    }

}
