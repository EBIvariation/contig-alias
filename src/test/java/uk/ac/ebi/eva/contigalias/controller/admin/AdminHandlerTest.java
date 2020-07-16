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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class AdminHandlerTest {

    private final AssemblyEntity entity = AssemblyGenerator.generate();

    private AdminHandler handler;

    @BeforeEach
    void setup() throws IOException {
        AssemblyService mockAssemblyService = mock(AssemblyService.class);
        Optional<AssemblyEntity> entityAsList = Optional.of(entity);
        Mockito.when(mockAssemblyService.getAssemblyOrFetchByAccession(entity.getGenbank()))
               .thenReturn(entityAsList);
        Mockito.when(mockAssemblyService.getAssemblyOrFetchByAccession(entity.getRefseq()))
               .thenReturn(entityAsList);
        handler = new AdminHandler(mockAssemblyService);
    }

    @Test
    public void getAssemblyOrFetchByAccessionGCA() throws IOException {
        List<AssemblyEntity> assemblyByAccession = handler.getAssemblyOrFetchByAccession(entity.getGenbank());
        assertFirstIdenticalToEntity(assemblyByAccession);
    }

    @Test
    public void getAssemblyOrFetchByAccessionGCF() throws IOException {
        List<AssemblyEntity> assemblyByAccession = handler.getAssemblyOrFetchByAccession(entity.getRefseq());
        assertFirstIdenticalToEntity(assemblyByAccession);
    }

    @Test
    public void test404NotFound() throws IOException {
        List<AssemblyEntity> assemblyByAccession = handler.getAssemblyOrFetchByAccession("##INVALID##");
        assertNotNull(assemblyByAccession);
        assertTrue(assemblyByAccession.isEmpty());
    }

    private void assertFirstIdenticalToEntity(List<AssemblyEntity> list) {
        assertNotNull(list);
        assertTrue(list.size() > 0);
        AssemblyEntity assembly = list.get(0);
        assertEquals(entity.getName(), assembly.getName());
        assertEquals(entity.getOrganism(), assembly.getOrganism());
        assertEquals(entity.getGenbank(), assembly.getGenbank());
        assertEquals(entity.getRefseq(), assembly.getRefseq());
        assertEquals(entity.getTaxid(), assembly.getTaxid());
        assertEquals(entity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
    }

}
