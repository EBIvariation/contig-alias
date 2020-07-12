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

package uk.ac.ebi.eva.contigalias.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.contigalias.datasource.AssemblyDataSource;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.repo.AssemblyRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest
public class AssemblyServiceIntegrationTest {

    private static final int TEST_ENTITIES_NUMBERS = 11;

    private final AssemblyEntity[] entities = new AssemblyEntity[TEST_ENTITIES_NUMBERS];

    @Autowired
    AssemblyRepository repository;

    private AssemblyService service;

    @BeforeEach
    void setup() throws IOException {
        AssemblyDataSource mockDataSource = mock(AssemblyDataSource.class);
        for (int i = 0; i < entities.length; i++) {
            AssemblyEntity generate = AssemblyGenerator.generate(i);
            entities[i] = generate;
            Mockito.when(mockDataSource.getAssemblyByAccession(generate.getGenbank()))
                   .thenReturn(Optional.of(generate));
            Mockito.when(mockDataSource.getAssemblyByAccession(generate.getRefseq()))
                   .thenReturn(Optional.of(generate));
        }
        service = new AssemblyService(repository, mockDataSource);
    }

    @Test
    void getAssemblyOrFetchByAccession() throws IOException {
        List<AssemblyEntity> entities = service.getAssemblyOrFetchByAccession(this.entities[0].getGenbank());
        assertNotNull(entities);
        assertTrue(entities.size() > 0);
        entities.forEach(service::deleteAssembly);
    }

    @Test
    void cacheLimitTest() throws IOException {
        int cacheSize = service.getCacheSize();
        service.setCacheSize(10);

        String targetGenbank = entities[0].getGenbank();
        service.fetchAndInsertAssembly(targetGenbank);
        List<AssemblyEntity> assemblyEntities = service.getAssemblyByAccession(targetGenbank);
        List<ChromosomeEntity> chromosomes = getFirstFromList(assemblyEntities).getChromosomes();
        assertNotNull(chromosomes);

        for (int i = 1; i < TEST_ENTITIES_NUMBERS; i++) {
            String genbank = entities[i].getGenbank();
            service.fetchAndInsertAssembly(genbank);
            List<AssemblyEntity> entity = service.getAssemblyByAccession(genbank);
            assertNotNull(entity);
            assertEquals(1, entity.size());
        }

        List<AssemblyEntity> targetGenbankEntity = service.getAssemblyByAccession(targetGenbank);
        assertTrue(targetGenbankEntity == null || targetGenbankEntity.isEmpty());

        for (int i = 1; i < TEST_ENTITIES_NUMBERS; i++) {
            String genbank = entities[i].getGenbank();
            List<AssemblyEntity> accession = service.getAssemblyByAccession(genbank);
            assertNotNull(accession);
            assertTrue(accession.size() > 0);
            accession.forEach(service::deleteAssembly);
        }

        service.setCacheSize(cacheSize);
    }

    private AssemblyEntity getFirstFromList(List<AssemblyEntity> list) {
        assertNotNull(list);
        assertTrue(list.size() > 0);
        return list.get(0);
    }

    @Nested
    class NoDataSource {

        private final int MAX_CONSECUTIVE_ENTITIES = 5;

        private final AssemblyEntity entity = AssemblyGenerator.generate(MAX_CONSECUTIVE_ENTITIES + 1);

        @BeforeEach
        void setup() {
            service.insertAssembly(entity);
        }

        @AfterEach
        void tearDown() {
            service.deleteAssemblyByRefseq(entity.getRefseq());
        }

        @Test
        void getAssemblyByAccession() {
            List<AssemblyEntity> accession = service.getAssemblyByAccession(entity.getGenbank());
            assertNotNull(accession);
            assertTrue(accession.size() > 0);
            accession.forEach(this::assertAssemblyEntityIdenticalToEntity);
        }

        @Test
        void getAssemblyByGenbank() {
            List<AssemblyEntity> entities = service.getAssemblyByGenbank(entity.getGenbank());
            assertAssemblyEntityIdenticalToEntity(getFirstFromList(entities));
        }

        @Test
        void getAssemblyByRefseq() {
            Page<AssemblyEntity> page = service.getAssemblyByRefseq(entity.getRefseq(),
                                                                    DEFAULT_PAGE_REQUEST);
            assertPageValid(page);
            page.get().forEach(this::assertAssemblyEntityIdenticalToEntity);
        }

        @Test
        void getAssembliesByTaxid() {

            long TAX_ID = 8493L;

            AssemblyEntity[] entities = new AssemblyEntity[MAX_CONSECUTIVE_ENTITIES];

            for (int i = 0; i < MAX_CONSECUTIVE_ENTITIES; i++) {
                AssemblyEntity assemblyEntity = AssemblyGenerator.generate(i).setTaxid(TAX_ID);
                entities[i] = assemblyEntity;
                service.insertAssembly(assemblyEntity);
            }

            Page<AssemblyEntity> page = service.getAssembliesByTaxid(TAX_ID, DEFAULT_PAGE_REQUEST);
            assertPageValid(page);
            List<AssemblyEntity> entityList = page.get().collect(Collectors.toList());
            assertEquals(MAX_CONSECUTIVE_ENTITIES, entityList.size());

            for (int i = 0; i < MAX_CONSECUTIVE_ENTITIES; i++) {
                assertAssemblyEntityIdenticalToEntity(entities[i], entityList.get(i));
            }

            for (AssemblyEntity assemblyEntity : entities) {
                service.deleteAssemblyByGenbank(assemblyEntity.getGenbank());
            }
        }

        void assertPageValid(Page<AssemblyEntity> page) {
            assertNotNull(page);
            assertTrue(page.getTotalElements() > 0);
        }

        void assertAssemblyEntityIdenticalToEntity(AssemblyEntity actual) {
            assertAssemblyEntityIdenticalToEntity(this.entity, actual);
        }

        void assertAssemblyEntityIdenticalToEntity(AssemblyEntity expect, AssemblyEntity actual) {
            assertEquals(expect.getName(), actual.getName());
            assertEquals(expect.getOrganism(), actual.getOrganism());
            assertEquals(expect.getGenbank(), actual.getGenbank());
            assertEquals(expect.getRefseq(), actual.getRefseq());
            assertEquals(expect.getTaxid(), actual.getTaxid());
            assertEquals(expect.isGenbankRefseqIdentical(), actual.isGenbankRefseqIdentical());
        }
    }

}
