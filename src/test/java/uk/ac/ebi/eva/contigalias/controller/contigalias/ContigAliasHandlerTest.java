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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.service.AliasService;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

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
            PageImpl<AssemblyEntity> page = new PageImpl<>(Collections.singletonList(this.entity));
            Mockito.when(mockAssemblyService
                                 .getAssemblyByAccession(this.entity.getGenbank(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(page);
            Mockito.when(mockAssemblyService
                                 .getAssemblyByAccession(this.entity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(page);
            Mockito.when(mockAssemblyService.getAssemblyByGenbank(this.entity.getGenbank(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(page);
            Mockito.when(mockAssemblyService.getAssemblyByRefseq(this.entity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(page);
            Mockito.when(mockAssemblyService.getAssemblyByRefseq(this.entity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(page);

            PagedResourcesAssembler<AssemblyEntity> assembler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = new PagedModel<>(
                    Collections.singletonList(new EntityModel<>(entity)), null);
            Mockito.when(assembler.toModel(any()))
                   .thenReturn(pagedModel);
            handler = new ContigAliasHandler(mockAssemblyService, null, null, assembler, null);
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
            handler = new ContigAliasHandler(mockAssemblyService, null, null, assembler, null);
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

            Page<ChromosomeEntity> entityListAsPage = new PageImpl<>(
                    Collections.singletonList(entity));
            Mockito.when(mockChromosomeService.getChromosomeByGenbank(entity.getGenbank(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(entityListAsPage);
            Mockito.when(mockChromosomeService.getChromosomeByRefseq(entity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(entityListAsPage);

            PagedResourcesAssembler<ChromosomeEntity> mockChromosomeAssmebler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<ChromosomeEntity>> chromosomePagedModel = new PagedModel<>(
                    Collections.singletonList(new EntityModel<>(entity)), null);
            Mockito.when(mockChromosomeAssmebler.toModel(any()))
                   .thenReturn(chromosomePagedModel);

            handler = new ContigAliasHandler(null, mockChromosomeService, null, null, mockChromosomeAssmebler);
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
    class AliasServiceTests {

        private final AssemblyEntity assemblyEntity = AssemblyGenerator.generate();

        private final List<ChromosomeEntity> chromosomeEntities = new LinkedList<>();

        private final int CHROMOSOME_LIST_SIZE = 5;

        @BeforeEach
        void setup() {
            AliasService mockAliasService = mock(AliasService.class);
            for (int i = 0; i < CHROMOSOME_LIST_SIZE; i++) {
                ChromosomeEntity generate = ChromosomeGenerator.generate(i, assemblyEntity);
                chromosomeEntities.add(generate);
                Optional<AssemblyEntity> entityOptional = Optional.of(this.assemblyEntity);
                Mockito.when(mockAliasService.getAssemblyByChromosomeGenbank(generate.getGenbank()))
                       .thenReturn(entityOptional);
                Mockito.when(mockAliasService.getAssemblyByChromosomeRefseq(generate.getRefseq()))
                       .thenReturn(entityOptional);
            }
            Mockito.when(mockAliasService.getChromosomesByAssemblyGenbank(assemblyEntity.getGenbank()))
                   .thenReturn(chromosomeEntities);
            Mockito.when(mockAliasService.getChromosomesByAssemblyRefseq(assemblyEntity.getRefseq()))
                   .thenReturn(chromosomeEntities);
            handler = new ContigAliasHandler(null, null, mockAliasService, null, null);
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

        void testAssemblyEntityResponse(Optional<AssemblyEntity> assembly) {
            assertNotNull(assembly);
            assertTrue(assembly.isPresent());
            testAssemblyEntityResponse(assembly.get());
        }

        void testAssemblyEntityResponse(AssemblyEntity assembly) {
            assertNotNull(assembly);
            assertEquals(assemblyEntity.getName(), assembly.getName());
            assertEquals(assemblyEntity.getOrganism(), assembly.getOrganism());
            assertEquals(assemblyEntity.getGenbank(), assembly.getGenbank());
            assertEquals(assemblyEntity.getRefseq(), assembly.getRefseq());
            assertEquals(assemblyEntity.getTaxid(), assembly.getTaxid());
            assertEquals(assemblyEntity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
        }

        void testChromosomeEntityResponses(List<ChromosomeEntity> entities) {
            assertNotNull(entities);
            assertEquals(chromosomeEntities.size(), entities.size());
            assertTrue(chromosomeEntities.containsAll(entities));
        }

    }
}
