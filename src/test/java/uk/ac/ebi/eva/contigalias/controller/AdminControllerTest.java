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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;

import java.io.IOException;
import java.util.List;

import static com.ebivariation.contigalias.controller.BaseController.DEFAULT_PAGE_NUMBER;
import static com.ebivariation.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;
import static com.ebivariation.contigalias.controller.BaseController.DEFAULT_PAGE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class AdminControllerTest {

    private final AssemblyEntity entity = AssemblyGenerator.generate();

    private AdminController controller;

    @BeforeEach
    void setup() throws IOException {
        AssemblyService mockAssemblyService = mock(AssemblyService.class);
        Mockito.when(mockAssemblyService.getAssemblyOrFetchByAccession(entity.getGenbank()))
               .thenReturn(List.of(entity));
        Mockito.when(mockAssemblyService
                             .getAssemblyOrFetchByAccession(entity.getRefseq()))
               .thenReturn(List.of(entity));
        controller = new AdminController(mockAssemblyService);
    }

    @Test
    public void getAssemblyOrFetchByAccessionGCA() throws IOException {
        ResponseEntity<List<AssemblyEntity>> assemblyByAccession =
                controller.getAssemblyOrFetchByAccession(entity.getGenbank(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
        testAssemblyEntityResponse(assemblyByAccession);
    }

    @Test
    public void getAssemblyOrFetchByAccessionGCF() throws IOException {
        ResponseEntity<List<AssemblyEntity>> assemblyByAccession =
                controller.getAssemblyOrFetchByAccession(entity.getRefseq(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
        testAssemblyEntityResponse(assemblyByAccession);
    }

    @Test
    public void test404NotFound() throws IOException {
        ResponseEntity<List<AssemblyEntity>> assemblyByAccession =
                controller.getAssemblyOrFetchByAccession("##INVALID##", DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
        assertEquals(assemblyByAccession.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    private void testAssemblyEntityResponse(ResponseEntity<List<AssemblyEntity>> response) {
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertTrue(response.hasBody());
        AssemblyEntity assembly = getFirstFromList(response.getBody());
        assertEquals(entity.getName(), assembly.getName());
        assertEquals(entity.getOrganism(), assembly.getOrganism());
        assertEquals(entity.getGenbank(), assembly.getGenbank());
        assertEquals(entity.getRefseq(), assembly.getRefseq());
        assertEquals(entity.getTaxid(), assembly.getTaxid());
        assertEquals(entity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
    }

    private AssemblyEntity getFirstFromList(List<AssemblyEntity> list) {
        assertNotNull(list);
        assertTrue(list.size() > 0);
        return list.get(0);
    }

}
