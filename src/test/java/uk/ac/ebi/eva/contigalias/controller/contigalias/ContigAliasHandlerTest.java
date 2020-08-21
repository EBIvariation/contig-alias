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
import uk.ac.ebi.eva.contigalias.entities.ScaffoldEntity;
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ScaffoldGenerator;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;
import uk.ac.ebi.eva.contigalias.service.ScaffoldService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.contigalias.ContigAliasController.NAME_SEQUENCE_TYPE;
import static uk.ac.ebi.eva.contigalias.controller.contigalias.ContigAliasController.NAME_UCSC_TYPE;

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
            handler = new ContigAliasHandler(mockAssemblyService, null, null, assembler, null, null);
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
            handler = new ContigAliasHandler(mockAssemblyService, null, null, assembler, null,
                                             null);
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

            Page<ChromosomeEntity> pageOfEntity = new PageImpl<>(Collections.singletonList(entity));
            Mockito.when(mockChromosomeService.getChromosomesByGenbank(entity.getGenbank(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(pageOfEntity);
            Mockito.when(mockChromosomeService.getChromosomesByRefseq(entity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(pageOfEntity);

            PagedResourcesAssembler<SequenceEntity> mockSequencesAssembler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<SequenceEntity>> sequencePagedModel = new PagedModel<>(
                    Collections.singletonList(new EntityModel<>(entity)), null);
            Mockito.when(mockSequencesAssembler.toModel(any()))
                   .thenReturn(sequencePagedModel);

            handler = new ContigAliasHandler(null, mockChromosomeService, null, null, mockSequencesAssembler,
                                             null);
        }

        @Test
        public void getChromosomeByGenbank() {
            testChromosomeEntityResponse(handler.getChromosomesByGenbank(entity.getGenbank(), DEFAULT_PAGE_REQUEST));
        }

        @Test
        public void getChromosomeByRefseq() {
            testChromosomeEntityResponse(handler.getChromosomesByRefseq(entity.getRefseq(), DEFAULT_PAGE_REQUEST));
        }

        void testChromosomeEntityResponse(PagedModel<EntityModel<SequenceEntity>> body) {
            assertNotNull(body);
            Collection<EntityModel<SequenceEntity>> content = body.getContent();
            assertTrue(content.size() > 0);
            content.forEach(it -> assertChromosomeIdenticalToEntity((ChromosomeEntity) it.getContent()));
        }

        void assertChromosomeIdenticalToEntity(ChromosomeEntity chromosome) {
            assertNotNull(chromosome);
            assertEquals(entity.getName(), chromosome.getName());
            assertEquals(entity.getGenbank(), chromosome.getGenbank());
            assertEquals(entity.getRefseq(), chromosome.getRefseq());
            assertEquals(entity.getUcscName(), chromosome.getUcscName());
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
                List<AssemblyEntity> listOfEntity = Collections.singletonList(this.assemblyEntity);
                Mockito.when(mockChromosomeService.getAssembliesByChromosomeGenbank(generate.getGenbank()))
                       .thenReturn(listOfEntity);
                Mockito.when(mockChromosomeService.getAssembliesByChromosomeRefseq(generate.getRefseq()))
                       .thenReturn(listOfEntity);
            }
            PageImpl<ChromosomeEntity> pageOfChromosomeEntities = new PageImpl<>(chromosomeEntities);
            Mockito.when(mockChromosomeService
                                 .getChromosomesByAssemblyGenbank(assemblyEntity.getGenbank(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(pageOfChromosomeEntities);
            Mockito.when(mockChromosomeService
                                 .getChromosomesByAssemblyRefseq(assemblyEntity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(pageOfChromosomeEntities);

            AssemblyService mockAssemblyService = mock(AssemblyService.class);
            Optional<AssemblyEntity> optionalOfAssemblyEntity = Optional.of(this.assemblyEntity);
            Mockito.when(mockAssemblyService.getAssemblyByAccession(this.assemblyEntity.getGenbank()))
                   .thenReturn(optionalOfAssemblyEntity);
            Mockito.when(mockAssemblyService.getAssemblyByAccession(this.assemblyEntity.getRefseq()))
                   .thenReturn(optionalOfAssemblyEntity);
            String chrName = chromosomeEntities.get(0)
                                               .getName();
            Long asmTaxid = assemblyEntity.getTaxid();
            Mockito.when(
                    mockChromosomeService.getChromosomesByNameAndAssemblyTaxid(chrName, asmTaxid, DEFAULT_PAGE_REQUEST))
                   .thenReturn(new PageImpl<>(
                           chromosomeEntities
                                   .stream()
                                   .filter(it -> it.getName().equals(chrName) &&
                                           it.getAssembly().getTaxid().equals(asmTaxid))
                                   .collect(Collectors.toList())));

            List<ChromosomeEntity> chromosomesByNameAndAssembly = chromosomeEntities
                    .stream()
                    .filter(it -> it.getName().equals(chrName) &&
                            it.getAssembly().equals(assemblyEntity))
                    .collect(Collectors.toList());

            Mockito.when(mockChromosomeService
                                 .getChromosomesByNameAndAssembly(chrName, assemblyEntity, DEFAULT_PAGE_REQUEST))
                   .thenReturn(new PageImpl<>(chromosomesByNameAndAssembly));

            Mockito.when(mockChromosomeService
                                 .getChromosomesByAssemblyAccession(assemblyEntity.getGenbank(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(pageOfChromosomeEntities);


            Mockito.when(mockChromosomeService
                                 .getChromosomesByAssemblyAccession(assemblyEntity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(pageOfChromosomeEntities);

            Mockito.when(mockChromosomeService.getChromosomesByUcscNameAndAssembly(
                    chromosomeEntities.get(0).getUcscName(), assemblyEntity, DEFAULT_PAGE_REQUEST))
                   .thenReturn(pageOfChromosomeEntities);

            Mockito.when(mockChromosomeService.getChromosomesByUcscNameAndAssemblyTaxid(
                    chromosomeEntities.get(0).getUcscName(), assemblyEntity.getTaxid(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(pageOfChromosomeEntities);

            Mockito.when(mockAssemblyService.getAssemblyByAccession(assemblyEntity.getGenbank()))
                   .thenReturn(Optional.of(assemblyEntity));

            PagedResourcesAssembler<AssemblyEntity> mockAssemblyAssembler = mock(PagedResourcesAssembler.class);

            PagedModel<EntityModel<AssemblyEntity>> assemblyPagedModel = new PagedModel(
                    Collections.singleton(new EntityModel<>(assemblyEntity)), null);
            Mockito.when(mockAssemblyAssembler.toModel(any()))
                   .thenReturn(assemblyPagedModel);

            PagedResourcesAssembler<SequenceEntity> mockSequenceAssembler = mock(PagedResourcesAssembler.class);

            PagedModel<EntityModel<SequenceEntity>> sequencePagedModel = PagedModel.wrap(
                    chromosomeEntities.stream().map(SequenceEntity.class::cast).collect(
                            Collectors.toList()), null);
            Mockito.when(mockSequenceAssembler.toModel(any()))
                   .thenReturn(sequencePagedModel);

            ScaffoldEntity scaffoldEntity = ScaffoldGenerator.generate();

            ScaffoldService mockScaffoldService = mock(ScaffoldService.class);

            Page<ScaffoldEntity> pageOfEntity = new PageImpl<>(Collections.singletonList(scaffoldEntity));
            Mockito.when(mockScaffoldService.getScaffoldsByGenbank(scaffoldEntity.getGenbank(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(pageOfEntity);
            Mockito.when(mockScaffoldService.getScaffoldsByRefseq(scaffoldEntity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(pageOfEntity);

            PagedResourcesAssembler<ScaffoldEntity> mockScaffoldAssembler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<ScaffoldEntity>> scaffoldPagedModel = new PagedModel<>(
                    Collections.singletonList(new EntityModel<>(scaffoldEntity)), null);
            Mockito.when(mockScaffoldAssembler.toModel(any()))
                   .thenReturn(scaffoldPagedModel);

            handler = new ContigAliasHandler(mockAssemblyService, mockChromosomeService, mockScaffoldService,
                                             mockAssemblyAssembler,
                                             mockSequenceAssembler, null);
        }

        @AfterEach
        void tearDown() {
            chromosomeEntities.clear();
        }

        @Test
        void getAssemblyByChromosomeGenbank() {
            for (ChromosomeEntity chromosomeEntity : chromosomeEntities) {
                testAssemblyEntityResponse(handler.getAssembliesByChromosomeGenbank(chromosomeEntity.getGenbank()));
            }
        }

        @Test
        void getAssemblyByChromosomeRefseq() {
            for (ChromosomeEntity chromosomeEntity : chromosomeEntities) {
                testAssemblyEntityResponse(handler.getAssembliesByChromosomeRefseq(chromosomeEntity.getRefseq()));
            }
        }

        @Test
        void getChromosomesByAssemblyGenbank() {
            testChromosomeEntityResponses(
                    handler.getChromosomesByAssemblyGenbank(assemblyEntity.getGenbank(), DEFAULT_PAGE_REQUEST));
        }

        @Test
        void getChromosomesByAssemblyRefseq() {
            testChromosomeEntityResponses(
                    handler.getChromosomesByAssemblyRefseq(assemblyEntity.getRefseq(), DEFAULT_PAGE_REQUEST));
        }

        @Test
        void getChromosomesByAssemblyAccessionGenbank() {
            testChromosomeEntityResponses(
                    handler.getChromosomesByAssemblyAccession(assemblyEntity.getGenbank(), DEFAULT_PAGE_REQUEST));
        }

        @Test
        void getChromosomesByAssemblyAccessionRefseq() {
            testChromosomeEntityResponses(
                    handler.getChromosomesByAssemblyAccession(assemblyEntity.getRefseq(), DEFAULT_PAGE_REQUEST));
        }

        void testAssemblyEntityResponse(PagedModel<EntityModel<AssemblyEntity>> pagedModel) {
            assertNotNull(pagedModel);
            Collection<EntityModel<AssemblyEntity>> content = pagedModel.getContent();
            assertNotNull(content);
            assertTrue(content.size() > 0);
            List<AssemblyEntity> assemblyEntities = content
                    .stream()
                    .map(EntityModel::getContent)
                    .collect(Collectors.toList());
            assertNotNull(assemblyEntities);
            assemblyEntities.forEach(this::testAssemblyEntityResponse);
        }

        @Test
        void getChromosomesByChromosomeNameAndAssemblyTaxid() {
            String chrName = chromosomeEntities.get(0).getName();
            Long asmTaxid = assemblyEntity.getTaxid();
            PagedModel<EntityModel<SequenceEntity>> pagedModel = handler
                    .getChromosomesByChromosomeNameAndAssemblyTaxid(
                            chrName, asmTaxid, NAME_SEQUENCE_TYPE, DEFAULT_PAGE_REQUEST);
            assertPagedModelIdenticalToChromosomeEntities(pagedModel);
        }

        @Test
        void getChromosomesByChromosomeNameAndAssemblyAccession() {
            String chrName = chromosomeEntities.get(0).getName();
            PagedModel<EntityModel<SequenceEntity>> pagedModel = handler
                    .getChromosomesByChromosomeNameAndAssemblyAccession(
                            chrName, assemblyEntity.getGenbank(), NAME_SEQUENCE_TYPE, DEFAULT_PAGE_REQUEST);
            assertPagedModelIdenticalToChromosomeEntities(pagedModel);
        }

        @Test
        void getChromosomesByChromosomeUcscNameAndAssemblyTaxid() {
            String chrName = chromosomeEntities.get(0).getUcscName();
            Long asmTaxid = assemblyEntity.getTaxid();
            PagedModel<EntityModel<SequenceEntity>> pagedModel = handler
                    .getChromosomesByChromosomeNameAndAssemblyTaxid(
                            chrName, asmTaxid, NAME_UCSC_TYPE, DEFAULT_PAGE_REQUEST);
            assertPagedModelIdenticalToChromosomeEntities(pagedModel);
        }

        @Test
        void getChromosomesByChromosomeUcscNameAndAssemblyAccession() {
            String chrName = chromosomeEntities.get(0).getUcscName();
            PagedModel<EntityModel<SequenceEntity>> pagedModel = handler
                    .getChromosomesByChromosomeNameAndAssemblyAccession(
                            chrName, assemblyEntity.getGenbank(), NAME_UCSC_TYPE, DEFAULT_PAGE_REQUEST);
            assertPagedModelIdenticalToChromosomeEntities(pagedModel);
        }

        private void assertPagedModelIdenticalToChromosomeEntities(
                PagedModel<EntityModel<SequenceEntity>> pagedModel) {
            assertNotNull(pagedModel);
            Collection<EntityModel<SequenceEntity>> content = pagedModel.getContent();
            assertNotNull(content);
            assertFalse(content.isEmpty());
            List<SequenceEntity> collect = content.stream().map(EntityModel::getContent).collect(Collectors.toList());
            collect.containsAll(chromosomeEntities);
        }

        void testAssemblyEntityResponse(AssemblyEntity assembly) {
            assertNotNull(assembly);
            assertEquals(this.assemblyEntity.getName(), assembly.getName());
            assertEquals(this.assemblyEntity.getOrganism(), assembly.getOrganism());
            assertEquals(this.assemblyEntity.getGenbank(), assembly.getGenbank());
            assertEquals(this.assemblyEntity.getRefseq(), assembly.getRefseq());
            assertEquals(this.assemblyEntity.getTaxid(), assembly.getTaxid());
            assertEquals(this.assemblyEntity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
        }

        void testChromosomeEntityResponses(PagedModel<EntityModel<SequenceEntity>> entityModel) {
            assertNotNull(entityModel);
            Collection<EntityModel<SequenceEntity>> content = entityModel.getContent();
            assertNotNull(content);
            List<EntityModel<SequenceEntity>> list = new ArrayList<>(content);
            assertEquals(chromosomeEntities.size(), list.size());
            List<SequenceEntity> chxList = list.stream().map(EntityModel::getContent).collect(Collectors.toList());
            assertTrue(chromosomeEntities.containsAll(chxList));
        }

    }
}
