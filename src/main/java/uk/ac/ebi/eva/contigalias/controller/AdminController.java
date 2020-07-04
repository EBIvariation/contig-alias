/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.contigalias.controller;

import io.swagger.annotations.ApiOperation;
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
import uk.ac.ebi.eva.contigalias.service.AssemblyService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static uk.ac.ebi.eva.contigalias.controller.BaseController.createAppropriateResponseEntity;

@RequestMapping("contig-alias-admin")
@RestController
public class AdminController {

    private final AssemblyService service;

    public AdminController(AssemblyService service) {
        this.service = service;
    }

    @ApiOperation(value = "Get or fetch an assembly using its Genbank or Refseq accession.",
            notes = "Given an assembly's accession this endpoint will return an assembly that matches that accession." +
                    " The accession can be either a genbank or refseq accession and the software will automatically " +
                    "fetch a result from the database for any assembly having the accession as it's genbank or refseq" +
                    " accession. This endpoint will first look for the assembly in local database and return the " +
                    "result. If local search fails, it search for the target assembly at a remote source (NCBI by " +
                    "default). If desired assembly is found at remote source, it will fetch and add it to the local " +
                    "database and also return the result to you. This endpoint will either return an empty list when " +
                    "no result is found or any number of assemblies.")
    @GetMapping(value = "v1/assemblies/{accession}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssemblyOrFetchByAccession(
            @PathVariable String accession,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize) throws IOException {
        List<AssemblyEntity> entities;
        try {
            entities = service.getAssemblyOrFetchByAccession(accession);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return createAppropriateResponseEntity(entities);
    }

    @ApiOperation(value = "Fetch an assembly from remote server using its Genbank or Refseq accession and insert " +
            "into local database.",
            notes = "Given an assembly's accession this endpoint will fetch and add the assembly that matches that " +
                    "accession into the local database. The accession can be either a genbank or refseq accession and" +
                    " the endpoint will automatically fetch the correct assembly from remote server. It will first " +
                    "search for the target assembly in the local database as trying to insert an assembly which " +
                    "already exists in the database is prohibited. If such an assembly is not found locally then it " +
                    "will look for it at a remote source (NCBI by default). If desired assembly is found at remote " +
                    "source, it will fetch and add it to the local database. This endpoint does not return any data.")
    @PutMapping(value = "v1/assemblies/{accession}")
    public ResponseEntity<?> fetchAndInsertAssemblyByAccession(@PathVariable String accession) throws IOException {
        try {
            service.fetchAndInsertAssembly(accession);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Fetch assemblies from remote server using their Genbank or Refseq accessions and insert " +
            "into local database.",
            notes = "Given a list of assembly accessions, for every accession in the list this endpoint will fetch " +
                    "and add the assembly that matches that accession into the local database. The accession can be " +
                    "either a genbank or refseq accession and the endpoint will automatically fetch the correct " +
                    "assembly from remote server. It will first search for the target assembly in the local database " +
                    "as trying to insert an assembly which already exists in the database is prohibited. If such an " +
                    "assembly is not found locally then it will look for it at a remote source (NCBI by default). If " +
                    "desired assembly is found at remote source, it will fetch and add it to the local database. This" +
                    " endpoint does not return any data and processes elements in the given list in an asynchronous " +
                    "parallel manner.")
    @PutMapping(value = "v1/assemblies")
    public ResponseEntity<?> fetchAndInsertAssemblyByAccession(@RequestBody Optional<List<String>> accessions) {
        if (accessions.isPresent()) {
            List<String> list = accessions.get();
            if (list.size() > 0) {
                service.fetchAndInsertAssembly(list);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation(value = "Delete an assembly from local database using its Genbank or Refseq accession.",
            notes = "Given an assembly's accession this endpoint will delete the assembly that matches that " +
                    "accession from the local database. The accession can be either a genbank or refseq accession and" +
                    " the endpoint will automatically deletes the correct assembly from the database. Deleting an " +
                    "assembly also deletes all chromosomes that are associated with that assembly. This endpoint does" +
                    " not return any data.")
    @DeleteMapping(value = "v1/assemblies/{accession}")
    public void deleteAssemblyByAccession(@PathVariable String accession) {
        service.deleteAssemblyByAccession(accession);
    }

}
