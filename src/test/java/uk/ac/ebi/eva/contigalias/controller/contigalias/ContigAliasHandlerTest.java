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

package uk.ac.ebi.eva.contigalias.controller.contigalias;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;

public class ContigAliasHandlerTest {

    private ContigAliasHandler handler;

    @Nested
    class AssemblyServiceTests {

        private final AssemblyEntity entity = AssemblyGenerator.generate();

        @BeforeEach
        void setUp() {
            AssemblyService mockAssemblyService = mock(AssemblyService.class);
            Optional<AssemblyEntity> optionalOfEntity = Optional.of(this.entity);
            Mockito.when(mockAssemblyService.getAssemblyByAccession(this.entity.getGenbank()))
                   .thenReturn(optionalOfEntity);
            Mockito.when(mockAssemblyService.getAssemblyByAccession(this.entity.getRefseq()))
                   .thenReturn(optionalOfEntity);
            Mockito.when(mockAssemblyService.getAssemblyByGenbank(this.entity.getGenbank()))
                   .thenReturn(optionalOfEntity);
            Mockito.when(mockAssemblyService.getAssemblyByRefseq(this.entity.getRefseq()))
                   .thenReturn(optionalOfEntity);
            Mockito.when(mockAssemblyService.getAssemblyByRefseq(this.entity.getRefseq()))
                   .thenReturn(optionalOfEntity);

            PagedResourcesAssembler<AssemblyEntity> assembler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = new PagedModel<>(
                    Collections.singletonList(new EntityModel<>(entity)), null);
            Mockito.when(assembler.toModel(any()))
                   .thenReturn(pagedModel);
            handler = new ContigAliasHandler(mockAssemblyService, null, assembler, null);
        }

        @Test
        public void getAssemblyByAccession() {
            testAssemblyEntityPagedResponse(
                    handler.getAssemblyByAccession(entity.getGenbank()));
            testAssemblyEntityPagedResponse(
                    handler.getAssemblyByAccession(entity.getRefseq()));
        }

        @Test
        public void getAssemblyByGenbank() {
            testAssemblyEntityPagedResponse(
                    handler.getAssemblyByGenbank(entity.getGenbank()));
        }

        @Test
        public void getAssemblyByRefseq() {
            testAssemblyEntityPagedResponse(
                    handler.getAssemblyByRefseq(entity.getRefseq()));
        }

        void testAssemblyEntityPagedResponse(PagedModel<EntityModel<AssemblyEntity>> body) {
            assertNotNull(body);
            Collection<EntityModel<AssemblyEntity>> content = body.getContent();
            content.forEach(it -> assertAssemblyIdenticalToEntity(it.getContent()));
        }

        void assertAssemblyIdenticalToEntity(AssemblyEntity assembly) {
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
                   .thenReturn(new PageImpl<>(entities));

            PagedResourcesAssembler<AssemblyEntity> assembler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = PagedModel.wrap(entities, null);
            Mockito.when(assembler.toModel(any()))
                   .thenReturn(pagedModel);
            handler = new ContigAliasHandler(mockAssemblyService, null, assembler, null);
        }

        @Test
        void getAssembliesByTaxid() {

            PagedModel<EntityModel<AssemblyEntity>> body = handler.getAssembliesByTaxid(TAX_ID, DEFAULT_PAGE_REQUEST);
            assertNotNull(body);
            List<EntityModel<AssemblyEntity>> entityList = new LinkedList<>(body.getContent());
            assertNotNull(entityList);
            assertEquals(MAX_CONSECUTIVE_ENTITIES, entityList.size());

            for (int i = 0; i < MAX_CONSECUTIVE_ENTITIES; i++) {
                AssemblyEntity assembly = entityList.get(i).getContent();
                assertNotNull(assembly);
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

            Optional<ChromosomeEntity> entityListAsPage = Optional.of(entity);
            Mockito.when(mockChromosomeService.getChromosomeByGenbank(entity.getGenbank()))
                   .thenReturn(entityListAsPage);
            Mockito.when(mockChromosomeService.getChromosomeByRefseq(entity.getRefseq()))
                   .thenReturn(entityListAsPage);

            PagedResourcesAssembler<ChromosomeEntity> mockChromosomeAssmebler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<ChromosomeEntity>> chromosomePagedModel = new PagedModel<>(
                    Collections.singletonList(new EntityModel<>(entity)), null);
            Mockito.when(mockChromosomeAssmebler.toModel(any()))
                   .thenReturn(chromosomePagedModel);

            handler = new ContigAliasHandler(null, mockChromosomeService, null, mockChromosomeAssmebler);
        }

        @Test
        public void getChromosomeByGenbank() {
            testChromosomeEntityResponse(handler.getChromosomeByGenbank(entity.getGenbank()));
        }

        @Test
        public void getChromosomeByRefseq() {
            testChromosomeEntityResponse(handler.getChromosomeByRefseq(entity.getRefseq()));
        }

        void testChromosomeEntityResponse(PagedModel<EntityModel<ChromosomeEntity>> body) {
            assertNotNull(body);
            Collection<EntityModel<ChromosomeEntity>> content = body.getContent();
            assertTrue(content.size() > 0);
            content.forEach(it -> assertChromosomeIdenticalToEntity(it.getContent()));
        }

        void assertChromosomeIdenticalToEntity(ChromosomeEntity chromosome) {
            assertNotNull(chromosome);
            assertEquals(entity.getName(), chromosome.getName());
            assertEquals(entity.getGenbank(), chromosome.getGenbank());
            assertEquals(entity.getRefseq(), chromosome.getRefseq());
        }

    }

    @Nested
    class ChromosomeServiceTestsWithAssemblies {

        private final AssemblyEntity assemblyEntity = AssemblyGenerator.generate();

        private final List<ChromosomeEntity> chromosomeEntities = new LinkedList<>();

        private final int CHROMOSOME_LIST_SIZE = 5;

        @BeforeEach
        void setup() {
            ChromosomeService mockChromosomeService = mock(ChromosomeService.class);
            for (int i = 0; i < CHROMOSOME_LIST_SIZE; i++) {
                ChromosomeEntity generate = ChromosomeGenerator.generate(i, assemblyEntity);
                chromosomeEntities.add(generate);
                Optional<AssemblyEntity> entityOptional = Optional.of(this.assemblyEntity);
                Mockito.when(mockChromosomeService.getAssemblyByChromosomeGenbank(generate.getGenbank()))
                       .thenReturn(entityOptional);
                Mockito.when(mockChromosomeService.getAssemblyByChromosomeRefseq(generate.getRefseq()))
                       .thenReturn(entityOptional);
            }
            Mockito.when(mockChromosomeService.getChromosomesByAssemblyGenbank(assemblyEntity.getGenbank()))
                   .thenReturn(chromosomeEntities);
            Mockito.when(mockChromosomeService.getChromosomesByAssemblyRefseq(assemblyEntity.getRefseq()))
                   .thenReturn(chromosomeEntities);
            String chrName = chromosomeEntities.get(0)
                                               .getName();
            Long asmTaxid = assemblyEntity.getTaxid();
            Mockito.when(mockChromosomeService.getChromosomesByNameAndAssemblyTaxid(chrName, asmTaxid))
                   .thenReturn(chromosomeEntities
                                       .parallelStream()
                                       .filter(it -> it.getName().equals(chrName) &&
                                               it.getAssembly().getTaxid().equals(asmTaxid))
                                       .collect(Collectors.toList()));
            Mockito.when(mockChromosomeService.getChromosomesByNameAndAssembly(chrName, assemblyEntity))
                   .thenReturn(chromosomeEntities
                                       .parallelStream()
                                       .filter(it -> it.getName().equals(chrName) &&
                                               it.getAssembly().equals(assemblyEntity))
                                       .collect(Collectors.toList()));

            AssemblyService mockAssemblyService = mock(AssemblyService.class);
            Mockito.when(mockAssemblyService.getAssemblyByAccession(assemblyEntity.getGenbank()))
                   .thenReturn(Optional.of(assemblyEntity));

            handler = new ContigAliasHandler(mockAssemblyService, mockChromosomeService, null, null);
        }

        @AfterEach
        void tearDown() {
            chromosomeEntities.clear();
        }

        @Test
        void getAssemblyByChromosomeGenbank() {
            for (ChromosomeEntity chromosomeEntity : chromosomeEntities) {
                testAssemblyEntityResponse(handler.getAssemblyByChromosomeGenbank(chromosomeEntity.getGenbank()));
            }
        }

        @Test
        void getAssemblyByChromosomeRefseq() {
            for (ChromosomeEntity chromosomeEntity : chromosomeEntities) {
                Optional<AssemblyEntity> assembly = handler.getAssemblyByChromosomeRefseq(chromosomeEntity.getRefseq());
                testAssemblyEntityResponse(assembly);
            }
        }

        @Test
        void getChromosomesByAssemblyGenbank() {
            testChromosomeEntityResponses(handler.getChromosomesByAssemblyGenbank(assemblyEntity.getGenbank()));
        }

        @Test
        void getChromosomesByAssemblyRefseq() {
            testChromosomeEntityResponses(handler.getChromosomesByAssemblyRefseq(assemblyEntity.getRefseq()));
        }

        @Test
        void getChromosomesByChromosomeNameAndAssemblyTaxid() {
            String chrName = chromosomeEntities.get(0).getName();
            Long asmTaxid = assemblyEntity.getTaxid();
            List<ChromosomeEntity> entities = handler
                    .getChromosomesByChromosomeNameAndAssemblyTaxid(chrName, asmTaxid);
            assertNotNull(entities);
            for (ChromosomeEntity chx : entities) {
                assertNotNull(chx);
                assertEquals(chrName, chx.getName());
                AssemblyEntity asx = chx.getAssembly();
                assertNotNull(asx);
                assertEquals(asmTaxid, asx.getTaxid());
            }
        }

        @Test
        void getChromosomesByChromosomeNameAndAssemblyAccession() {
            String chrName = chromosomeEntities.get(0).getName();
            List<ChromosomeEntity> entities = handler
                    .getChromosomesByChromosomeNameAndAssemblyAccession(chrName, assemblyEntity.getGenbank());
            assertNotNull(entities);
            for (ChromosomeEntity chx : entities) {
                assertNotNull(chx);
                assertEquals(chrName, chx.getName());
                AssemblyEntity asx = chx.getAssembly();
                assertNotNull(asx);
                assertTrue(asx.equals(assemblyEntity));
            }

        }

        void testAssemblyEntityResponse(Optional<AssemblyEntity> optional) {
            assertNotNull(optional);
            assertTrue(optional.isPresent());
            AssemblyEntity assembly = optional.get();
            assertEquals(this.assemblyEntity.getName(), assembly.getName());
            assertEquals(this.assemblyEntity.getOrganism(), assembly.getOrganism());
            assertEquals(this.assemblyEntity.getGenbank(), assembly.getGenbank());
            assertEquals(this.assemblyEntity.getRefseq(), assembly.getRefseq());
            assertEquals(this.assemblyEntity.getTaxid(), assembly.getTaxid());
            assertEquals(this.assemblyEntity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
        }

        void testChromosomeEntityResponses(List<ChromosomeEntity> entities) {
            assertNotNull(entities);
            assertEquals(chromosomeEntities.size(), entities.size());
            assertTrue(chromosomeEntities.containsAll(entities));
        }

    }
}
