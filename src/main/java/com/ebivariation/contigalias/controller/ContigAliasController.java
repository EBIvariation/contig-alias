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
import com.ebivariation.contigalias.service.AssemblyService;
import com.ebivariation.contigalias.service.ChromosomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequestMapping("contig-alias")
@RestController
public class ContigAliasController {

    private final AssemblyService assemblyService;

    private final ChromosomeService chromosomeService;

    @Autowired
    public ContigAliasController(AssemblyService assemblyService,
                                 ChromosomeService chromosomeService) {
        this.assemblyService = assemblyService;
        this.chromosomeService = chromosomeService;
    }

    @GetMapping(value = "assemblies/{accession}")
    public ResponseEntity<Optional<AssemblyEntity>> getAssemblyByAccession(
            @PathVariable String accession) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByAccession(accession);
        if (entity.isPresent()) {
            return new ResponseEntity<>(entity, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(entity, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "assemblies/genbank/{genbank}")
    public ResponseEntity<AssemblyEntity> getAssemblyByGenbank(@PathVariable String genbank) {
        Optional<AssemblyEntity> entity = assemblyService.getAssemblyByGenbank(genbank);
        return entity.map(assemblyEntity -> new ResponseEntity<>(assemblyEntity, HttpStatus.OK))
                     .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}
