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
import org.springframework.beans.factory.annotation.Autowired;
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
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createAppropriateResponseEntity;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.createPageRequest;
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

    @ApiOperation(value = "Get an assembly using its Genbank or Refseq accession.",
            notes = "Given an accession this endpoint will return an assembly that matches that accession." +
                    "The accession can be either a genbank or refseq accession and the software will automatically " +
                    "fetch a result from the database for any assembly having the accession as it's " +
                    "genbank or refseq accession. " +
                    "This endpoint will either return an empty list when no result is found or at most " +
                    "a list containing a single result.")
    @GetMapping(value = "v1/assemblies/{accession}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssemblyByAccession(@PathVariable String accession,
                                                                       @RequestParam(required = false) Integer pageNumber,
                                                                       @RequestParam(required = false) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            List<AssemblyEntity> entities = assemblyService.getAssemblyByAccession(accession);
            return createAppropriateResponseEntity(entities);
        } else return BaseController.BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its Genbank accession.")
    @GetMapping(value = "v1/assemblies/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssemblyByGenbank(@PathVariable String genbank,
                                                                     @RequestParam(required = false) Integer pageNumber,
                                                                     @RequestParam(required = false) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            List<AssemblyEntity> entities = assemblyService.getAssemblyByGenbank(genbank);
            return createAppropriateResponseEntity(entities);
        } else return BaseController.BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its Refseq accession.")
    @GetMapping(value = "v1/assemblies/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssemblyByRefseq(@PathVariable String refseq,
                                                                    @RequestParam(required = false) Integer pageNumber,
                                                                    @RequestParam(required = false) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            List<AssemblyEntity> entities = assemblyService.getAssemblyByRefseq(refseq);
            return createAppropriateResponseEntity(entities);
        } else return BaseController.BAD_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its Taxonomic ID.")
    @GetMapping(value = "v1/assemblies/taxid/{taxid}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssembliesByTaxid(@PathVariable long taxid,
                                                                     @RequestParam(required = false) Integer pageNumber,
                                                                     @RequestParam(required = false) Integer pageSize) {
        List<AssemblyEntity> entities = assemblyService.getAssembliesByTaxid(taxid, createPageRequest(pageNumber, pageSize));
        return createAppropriateResponseEntity(entities);
    }

    @ApiOperation(value = "Get an assembly using the genbank accession of one of its chromosomes.")
    @GetMapping(value = "v1/assemblies/chromosome/genbank/{genbank}")
    public ResponseEntity<AssemblyEntity> getAssemblyByChromosomeGenbank(@PathVariable String genbank) {
        Optional<AssemblyEntity> entity = aliasService.getAssemblyByChromosomeGenbank(genbank);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Get an assembly using the refseq accession of one of its chromosomes.")
    @GetMapping(value = "v1/assemblies/chromosome/refseq/{refseq}")
    public ResponseEntity<AssemblyEntity> getAssemblyByChromosomeRefseq(@PathVariable String refseq) {
        Optional<AssemblyEntity> entity = aliasService.getAssemblyByChromosomeRefseq(refseq);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Get an chromosome using its Genbank accession.")
    @GetMapping(value = "v1/chromosomes/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<ChromosomeEntity> getChromosomeByGenbank(@PathVariable String genbank,
                                                                         @RequestParam(required = false) Integer pageNumber,
                                                                         @RequestParam(required = false) Integer pageSize) {
        if (paramsValidForSingleResponseQuery(pageNumber, pageSize)) {
            Optional<ChromosomeEntity> entities = chromosomeService.getChromosomeByGenbank(genbank);
            return createAppropriateResponseEntity(entities);
        } else return BAD_REQUEST;
    }

    @ApiOperation(value = "Get an chromosome using its Refseq accession.")
    @GetMapping(value = "v1/chromosomes/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<ChromosomeEntity> getChromosomeByRefseq(@PathVariable String refseq,
                                                                        @RequestParam(required = false) Integer pageNumber,
                                                                        @RequestParam(required = false) Integer pageSize) {
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

}
