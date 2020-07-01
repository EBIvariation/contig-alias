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
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.List;
import java.util.Optional;

@RequestMapping("contig-alias")
@RestController
public class ContigAliasController extends BaseController {

    private final AssemblyService assemblyService;

    private final ChromosomeService chromosomeService;

    @Autowired
    public ContigAliasController(AssemblyService assemblyService, ChromosomeService chromosomeService) {
        this.assemblyService = assemblyService;
        this.chromosomeService = chromosomeService;
    }

    @ApiOperation(value = "Get an assembly using its Genbank or Refseq accession.")
    @GetMapping(value = "v1/assemblies/{accession}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssemblyByAccession(@PathVariable String accession,
                                                                       @RequestParam(required = false) Integer page,
                                                                       @RequestParam(required = false) Integer size) {
        if (paramsValidForSingleResponseQuery(page, size)) {
            List<AssemblyEntity> entities = assemblyService.getAssemblyByAccession(accession);
            return createAppropriateResponseEntity(entities);
        } else return BAD_ASSEMBLY_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its Genbank accession.")
    @GetMapping(value = "v1/assemblies/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssemblyByGenbank(@PathVariable String genbank,
                                                                     @RequestParam(required = false) Integer page,
                                                                     @RequestParam(required = false) Integer size) {
        if (paramsValidForSingleResponseQuery(page, size)) {
            List<AssemblyEntity> entities = assemblyService.getAssemblyByGenbank(genbank);
            return createAppropriateResponseEntity(entities);
        } else return BAD_ASSEMBLY_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its Refseq accession.")
    @GetMapping(value = "v1/assemblies/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssemblyByRefseq(@PathVariable String refseq,
                                                                    @RequestParam(required = false) Integer page,
                                                                    @RequestParam(required = false) Integer size) {
        if (paramsValidForSingleResponseQuery(page, size)) {
            List<AssemblyEntity> entities = assemblyService.getAssemblyByRefseq(refseq);
            return createAppropriateResponseEntity(entities);
        } else return BAD_ASSEMBLY_REQUEST;
    }

    @ApiOperation(value = "Get an assembly using its Taxonomic ID.")
    @GetMapping(value = "v1/assemblies/taxid/{taxid}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssembliesByTaxid(@PathVariable long taxid,
                                                                     @RequestParam(required = false) Integer page,
                                                                     @RequestParam(required = false) Integer size) {
        List<AssemblyEntity> entities = assemblyService.getAssembliesByTaxid(taxid, createPageRequest(page, size));
        return createAppropriateResponseEntity(entities);
    }

    @ApiOperation(value = "Get an chromosome using its Genbank accession.")
    @GetMapping(value = "v1/chromosomes/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<List<ChromosomeEntity>> getChromosomeByGenbank(@PathVariable String genbank,
                                                                         @RequestParam(required = false) Integer page,
                                                                         @RequestParam(required = false) Integer size) {
        if (paramsValidForSingleResponseQuery(page, size)) {
            List<ChromosomeEntity> entities = chromosomeService.getChromosomeByGenbank(genbank);
            return createAppropriateResponseEntity(entities);
        } else return BAD_CHROMOSOME_REQUEST;
    }

    @ApiOperation(value = "Get an chromosome using its Refseq accession.")
    @GetMapping(value = "v1/chromosomes/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<ChromosomeEntity> getChromosomeByRefseq(@PathVariable String refseq) {
        Optional<ChromosomeEntity> entity = chromosomeService.getChromosomeByRefseq(refseq);
        return entity.map(chromosomeEntity -> new ResponseEntity<>(chromosomeEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
