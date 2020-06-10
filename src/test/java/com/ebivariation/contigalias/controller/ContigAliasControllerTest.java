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
import com.ebivariation.contigalias.entitygenerator.AssemblyGenerator;
import com.ebivariation.contigalias.service.AssemblyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class ContigAliasControllerTest {

    AssemblyEntity entity = AssemblyGenerator.generate();

    private ContigAliasController controller;

    @BeforeEach
    void setUp() {
        AssemblyService mockAssemblyService = mock(AssemblyService.class);
        Mockito.when(mockAssemblyService.getAssemblyByAccession(entity.getGenbank()))
               .thenReturn(Optional.of(entity));
        Mockito.when(mockAssemblyService.getAssemblyByAccession(entity.getRefseq()))
               .thenReturn(Optional.of(entity));
        Mockito.when(mockAssemblyService.getAssemblyByGenbank(entity.getGenbank()))
               .thenReturn(Optional.of(entity));
        Mockito.when(mockAssemblyService.getAssemblyByRefseq(entity.getRefseq()))
               .thenReturn(Optional.of(entity));
        controller = new ContigAliasController(mockAssemblyService, null);
    }

    @Test
    public void getAssemblyByAccession() {
        testAssemblyEntityResponse(controller.getAssemblyByAccession(entity.getGenbank()));
        testAssemblyEntityResponse(controller.getAssemblyByAccession(entity.getRefseq()));
    }

    @Test
    public void getAssemblyByGenbank() {
        testAssemblyEntityResponse(controller.getAssemblyByGenbank(entity.getGenbank()));
    }

    @Test
    public void getAssemblyByRefseq() {
        testAssemblyEntityResponse(controller.getAssemblyByRefseq(entity.getRefseq()));
    }

    void testAssemblyEntityResponse(ResponseEntity<AssemblyEntity> response) {
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertTrue(response.hasBody());
        AssemblyEntity assembly = response.getBody();
        assertNotNull(assembly);
        assertEquals(entity.getName(), assembly.getName());
        assertEquals(entity.getOrganism(), assembly.getOrganism());
        assertEquals(entity.getGenbank(), assembly.getGenbank());
        assertEquals(entity.getRefseq(), assembly.getRefseq());
        assertEquals(entity.getTaxid(), assembly.getTaxid());
        assertEquals(entity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
    }
}
