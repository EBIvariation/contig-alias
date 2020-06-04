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

package com.ebivariation.contigalias.controller;

import com.ebivariation.contigalias.entities.AssemblyEntity;
import com.ebivariation.contigalias.entitygenerator.AssemblyGenerator;
import com.ebivariation.contigalias.service.AssemblyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class AdminControllerTest {

    private final AssemblyEntity entity = AssemblyGenerator.generate(new Random().nextLong());

    private AdminController controller;

    @BeforeEach
    void setup() throws IOException {
        AssemblyService mockAssemblyService = mock(AssemblyService.class);
        Mockito.when(mockAssemblyService.getAssemblyOrFetchByAccession(entity.getGenbank()))
               .thenReturn(Optional.of(entity));
        Mockito.when(mockAssemblyService.getAssemblyOrFetchByAccession(entity.getRefseq()))
               .thenReturn(Optional.of(entity));
        controller = new AdminController(mockAssemblyService);
    }

    @Test
    public void getAssemblyOrFetchByAccessionGCA() throws IOException {
        ResponseEntity<Optional<AssemblyEntity>> assemblyByAccession =
                controller.getAssemblyOrFetchByAccession(entity.getGenbank());
        assertEquals(assemblyByAccession.getStatusCode(), HttpStatus.OK);
        assertTrue(assemblyByAccession.hasBody());
        AssemblyEntity assembly = assemblyByAccession.getBody().get();
        assertEquals(assembly.getName(), entity.getName());
        assertEquals(assembly.getOrganism(), entity.getOrganism());
        assertEquals(assembly.getGenbank(), entity.getGenbank());
        assertEquals(assembly.getRefseq(), entity.getRefseq());
        assertEquals(assembly.getTaxid(), entity.getTaxid());
        assertEquals(assembly.isGenbankRefseqIdentical(), entity.isGenbankRefseqIdentical());
    }

    @Test
    public void getAssemblyOrFetchByAccessionGCF() throws IOException {
        ResponseEntity<Optional<AssemblyEntity>> assemblyByAccession =
                controller.getAssemblyOrFetchByAccession(entity.getRefseq());
        assertEquals(assemblyByAccession.getStatusCode(), HttpStatus.OK);
        assertTrue(assemblyByAccession.hasBody());
        AssemblyEntity assembly = assemblyByAccession.getBody().get();
        assertEquals(assembly.getName(), entity.getName());
        assertEquals(assembly.getOrganism(), entity.getOrganism());
        assertEquals(assembly.getGenbank(), entity.getGenbank());
        assertEquals(assembly.getRefseq(), entity.getRefseq());
        assertEquals(assembly.getTaxid(), entity.getTaxid());
        assertEquals(assembly.isGenbankRefseqIdentical(), entity.isGenbankRefseqIdentical());
    }

    @Test
    public void test404NotFound() throws IOException {
        ResponseEntity<Optional<AssemblyEntity>> assemblyByAccession =
                controller.getAssemblyOrFetchByAccession("##INVALID##");
        assertEquals(assemblyByAccession.getStatusCode(), HttpStatus.NOT_FOUND);
    }

}
