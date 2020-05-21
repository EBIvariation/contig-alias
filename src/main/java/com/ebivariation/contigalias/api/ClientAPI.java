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

package com.ebivariation.contigalias.api;

import com.ebivariation.contigalias.entities.AssemblyEntity;
import com.ebivariation.contigalias.service.AssemblyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("contig-alias")
@RestController
public class ClientAPI {

    private final AssemblyService service;

    @Autowired
    public ClientAPI(AssemblyService service) {
        this.service = service;
    }

    @GetMapping(value = "assemblies/{accession}")
    public AssemblyEntity getAssemblyByAccession(@PathVariable String accession) throws IOException {
        AssemblyEntity assemblyByAccession = service.getAssemblyByAccession(accession);
        return assemblyByAccession;
    }

}
