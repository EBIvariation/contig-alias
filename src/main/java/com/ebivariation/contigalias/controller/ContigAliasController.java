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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RequestMapping("contig-alias")
@RestController
public class ContigAliasController {

    private final AssemblyService service;

    @Autowired
    public ContigAliasController(AssemblyService service) {
        this.service = service;
    }

    @GetMapping(value = "assemblies/{accession}")
    public ResponseEntity<Optional<AssemblyEntity>> getAssemblyByAccession(
            @PathVariable String accession) {
        Optional<AssemblyEntity> entity = service.getAssemblyByAccession(accession);
        if (entity.isPresent()) {
            return new ResponseEntity<>(entity, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(entity, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "assemblies")
    public ResponseEntity<List<AssemblyEntity>> getAssembliesResolveAlias(
            @RequestParam(required = false) Optional<String> name,
            @RequestParam(required = false) Optional<Long> taxid,
            @RequestParam(required = false) Optional<String> genbank,
            @RequestParam(required = false) Optional<String> refseq) {
        AssemblyEntity e = new AssemblyEntity();
        name.ifPresent(e::setName);
        taxid.ifPresent(e::setTaxid);
        genbank.ifPresent(e::setGenbank);
        refseq.ifPresent(e::setRefseq);
        List<AssemblyEntity> assemblyByExample = service.getAssembliesResolveAlias(e);
        if (assemblyByExample != null && assemblyByExample.size() > 0) {
            return new ResponseEntity<>(assemblyByExample, HttpStatus.OK);
        } else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
