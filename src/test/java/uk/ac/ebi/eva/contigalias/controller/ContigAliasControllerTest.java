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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.LinkedList;
import java.util.List;

import static com.ebivariation.contigalias.controller.BaseController.DEFAULT_PAGE_NUMBER;
import static com.ebivariation.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;
import static com.ebivariation.contigalias.controller.BaseController.DEFAULT_PAGE_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class ContigAliasControllerTest {

    private ContigAliasController controller;

    @Nested
    class AssemblyServiceTests {

        private final AssemblyEntity entity = AssemblyGenerator.generate();

        @BeforeEach
        void setUp() {
            AssemblyService mockAssemblyService = mock(AssemblyService.class);
            List<AssemblyEntity> entityAsList = List.of(this.entity);
            Mockito.when(mockAssemblyService
                                 .getAssemblyByAccession(this.entity.getGenbank()))
                   .thenReturn(entityAsList);
            Mockito.when(mockAssemblyService
                                 .getAssemblyByAccession(this.entity.getRefseq()))
                   .thenReturn(entityAsList);
            Mockito.when(mockAssemblyService.getAssemblyByGenbank(this.entity.getGenbank()))
                   .thenReturn(entityAsList);
            Mockito.when(mockAssemblyService.getAssemblyByRefseq(this.entity.getRefseq()))
                   .thenReturn(entityAsList);

            controller = new ContigAliasController(mockAssemblyService, null);
        }

        @Test
        public void getAssemblyByAccession() {
            testAssemblyEntityResponse(
                    controller.getAssemblyByAccession(entity.getGenbank(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
            testAssemblyEntityResponse(
                    controller.getAssemblyByAccession(entity.getRefseq(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
        }

        @Test
        public void getAssemblyByGenbank() {
            testAssemblyEntityResponse(
                    controller.getAssemblyByGenbank(entity.getGenbank(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
        }

        @Test
        public void getAssemblyByRefseq() {
            testAssemblyEntityResponse(
                    controller.getAssemblyByRefseq(entity.getRefseq(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
        }

        void testAssemblyEntityResponse(ResponseEntity<List<AssemblyEntity>> response) {
            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertTrue(response.hasBody());
            List<AssemblyEntity> body = response.getBody();
            assertNotNull(body);
            assertTrue(body.size() > 0);
            AssemblyEntity assembly = body.get(0);
            assertNotNull(assembly);
            assertEquals(entity.getName(), assembly.getName());
            assertEquals(entity.getOrganism(), assembly.getOrganism());
            assertEquals(entity.getGenbank(), assembly.getGenbank());
            assertEquals(entity.getRefseq(), assembly.getRefseq());
            assertEquals(entity.getTaxid(), assembly.getTaxid());
            assertEquals(entity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
        }

    }

    @Nested
    class AssemblyServiceGetByTaxidTest {

        private final int MAX_CONSECUTIVE_ENTITIES = 5;

        private final long TAX_ID = 342043L;

        private final List<AssemblyEntity> entities = new LinkedList<>();

        @BeforeEach
        void setup() {
            for (int i = 0; i < MAX_CONSECUTIVE_ENTITIES; i++) {
                AssemblyEntity assemblyEntity = AssemblyGenerator.generate(i).setTaxid(TAX_ID);
                entities.add(assemblyEntity);
            }
            AssemblyService mockAssemblyService = mock(AssemblyService.class);
            Mockito.when(mockAssemblyService
                                 .getAssembliesByTaxid(TAX_ID, DEFAULT_PAGE_REQUEST))
                   .thenReturn(entities);

            controller = new ContigAliasController(mockAssemblyService, null);
        }

        @Test
        void getAssembliesByTaxid() {
            ResponseEntity<List<AssemblyEntity>> response
                    = controller.getAssembliesByTaxid(TAX_ID, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertTrue(response.hasBody());
            List<AssemblyEntity> entityList = response.getBody();
            assertNotNull(entityList);
            assertEquals(MAX_CONSECUTIVE_ENTITIES, entityList.size());

            for (int i = 0; i < MAX_CONSECUTIVE_ENTITIES; i++) {
                AssemblyEntity assembly = entityList.get(i);
                AssemblyEntity entity = entities.get(i);
                assertEquals(entity.getName(), assembly.getName());
                assertEquals(entity.getOrganism(), assembly.getOrganism());
                assertEquals(entity.getGenbank(), assembly.getGenbank());
                assertEquals(entity.getRefseq(), assembly.getRefseq());
                assertEquals(TAX_ID, assembly.getTaxid());
                assertEquals(entity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
            }
        }

    }

    @Nested
    class ChromosomeServiceTests {

        ChromosomeEntity entity = ChromosomeGenerator.generate();

        @BeforeEach
        void setUp() {
            ChromosomeService mockChromosomeService = mock(ChromosomeService.class);
            List<ChromosomeEntity> entityAsList = List.of(this.entity);
            Mockito.when(mockChromosomeService.getChromosomeByGenbank(this.entity.getGenbank()))
                   .thenReturn(entityAsList);
            Mockito.when(mockChromosomeService.getChromosomeByRefseq(this.entity.getRefseq()))
                   .thenReturn(entityAsList);
            controller = new ContigAliasController(null, mockChromosomeService);
        }

        @Test
        public void getChromosomeByGenbank() {
            testChromosomeEntityResponse(
                    controller.getChromosomeByGenbank(entity.getGenbank(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
        }

        @Test
        public void getChromosomeByRefseq() {
            testChromosomeEntityResponse(
                    controller.getChromosomeByRefseq(entity.getRefseq(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
        }

        void testChromosomeEntityResponse(ResponseEntity<List<ChromosomeEntity>> response) {
            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertTrue(response.hasBody());
            List<ChromosomeEntity> body = response.getBody();
            assertNotNull(body);
            assertTrue(body.size() > 0);
            ChromosomeEntity chromosome = body.get(0);
            assertNotNull(chromosome);
            assertEquals(entity.getName(), chromosome.getName());
            assertEquals(entity.getGenbank(), chromosome.getGenbank());
            assertEquals(entity.getRefseq(), chromosome.getRefseq());
        }

    }
}
