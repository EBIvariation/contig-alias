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
import java.util.Optional;

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
                                 .getAssembliesByTaxid(TAX_ID, null, null))
                   .thenReturn(entities);

            controller = new ContigAliasController(mockAssemblyService, null);
        }

        @Test
        void getAssembliesByTaxid() {
            ResponseEntity<List<AssemblyEntity>> response
                    = controller.getAssembliesByTaxid(TAX_ID, Optional.empty(), Optional.empty());
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
            Mockito.when(mockChromosomeService.getChromosomeByGenbank(entity.getGenbank()))
                   .thenReturn(Optional.of(entity));
            Mockito.when(mockChromosomeService.getChromosomeByRefseq(entity.getRefseq()))
                   .thenReturn(Optional.of(entity));
            controller = new ContigAliasController(null, mockChromosomeService);
        }

        @Test
        public void getChromosomeByGenbank() {
            testChromosomeEntityResponse(controller.getChromosomeByGenbank(entity.getGenbank()));
        }

        @Test
        public void getChromosomeByRefseq() {
            testChromosomeEntityResponse(controller.getChromosomeByRefseq(entity.getRefseq()));
        }

        void testChromosomeEntityResponse(ResponseEntity<ChromosomeEntity> response) {
            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertTrue(response.hasBody());
            ChromosomeEntity chromosome = response.getBody();
            assertNotNull(chromosome);
            assertEquals(entity.getName(), chromosome.getName());
            assertEquals(entity.getGenbank(), chromosome.getGenbank());
            assertEquals(entity.getRefseq(), chromosome.getRefseq());
        }

    }
}
