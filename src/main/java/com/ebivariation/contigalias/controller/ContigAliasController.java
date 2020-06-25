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

package com.ebivariation.contigalias.controller;

import com.ebivariation.contigalias.entities.AssemblyEntity;
import com.ebivariation.contigalias.entities.ChromosomeEntity;
import com.ebivariation.contigalias.service.AssemblyService;
import com.ebivariation.contigalias.service.ChromosomeService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RequestMapping("contig-alias")
@RestController
public class ContigAliasController {

    private final AssemblyService assemblyService;

    private final ChromosomeService chromosomeService;

    @Autowired
    public ContigAliasController(AssemblyService assemblyService, ChromosomeService chromosomeService) {
        this.assemblyService = assemblyService;
        this.chromosomeService = chromosomeService;
    }

    @ApiOperation(value = "Get an assembly using its Genbank or Refseq accession.")
    @GetMapping(value = "v1/assemblies/{accession}", produces = "application/json")
    public ResponseEntity<AssemblyEntity> getAssemblyByAccession(@PathVariable String accession) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByAccession(accession);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Get an assembly using its Genbank accession.")
    @GetMapping(value = "v1/assemblies/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<AssemblyEntity> getAssemblyByGenbank(@PathVariable String genbank) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByGenbank(genbank);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Get an assembly using its Refseq accession.")
    @GetMapping(value = "v1/assemblies/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<AssemblyEntity> getAssemblyByRefseq(@PathVariable String refseq) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByRefseq(refseq);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Get an assembly using its Taxonomic ID.")
    @GetMapping(value = "v1/assemblies/taxid/{taxid}", produces = "application/json")
    public ResponseEntity<List<AssemblyEntity>> getAssembliesByTaxid(@PathVariable long taxid) {
        List<AssemblyEntity> entities = assemblyService.getAssembliesByTaxid(taxid);
        if (entities != null && !entities.isEmpty()) {
            return new ResponseEntity<>(entities, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Get an chromosome using its Genbank accession.")
    @GetMapping(value = "v1/chromosomes/genbank/{genbank}", produces = "application/json")
    public ResponseEntity<ChromosomeEntity> getChromosomeByGenbank(@PathVariable String genbank) {
        Optional<ChromosomeEntity> entity = chromosomeService.getChromosomeByGenbank(genbank);
        return entity.map(chromosomeEntity -> new ResponseEntity<>(chromosomeEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @ApiOperation(value = "Get an chromosome using its Refseq accession.")
    @GetMapping(value = "v1/chromosomes/refseq/{refseq}", produces = "application/json")
    public ResponseEntity<ChromosomeEntity> getChromosomeByRefseq(@PathVariable String refseq) {
        Optional<ChromosomeEntity> entity = chromosomeService.getChromosomeByRefseq(refseq);
        return entity.map(chromosomeEntity -> new ResponseEntity<>(chromosomeEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
