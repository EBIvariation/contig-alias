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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ebi.eva.contigalias.datasource.ENAAssemblyDataSource;
import uk.ac.ebi.eva.contigalias.datasource.NCBIAssemblyDataSource;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.repo.AssemblyRepository;
import uk.ac.ebi.eva.contigalias.repo.ChromosomeRepository;
import uk.ac.ebi.eva.contigalias.scheduler.ChromosomeUpdater;

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

    @Autowired
    ChromosomeRepository chromosomeRepository;

    @Autowired
    ChromosomeService chromosomeService;

    @Autowired
    private AssemblyService service;

    @BeforeEach
    void setup() {
        NCBIAssemblyDataSource mockNcbiDataSource = mock(NCBIAssemblyDataSource.class);
        ENAAssemblyDataSource mockEnaDataSource = mock(ENAAssemblyDataSource.class);
        ChromosomeUpdater chromosomeUpdater = mock(ChromosomeUpdater.class);
        for (int i = 0; i < entities.length; i++) {
            AssemblyEntity generate = AssemblyGenerator.generate(i);
            entities[i] = generate;
        }
        service = new AssemblyService(chromosomeService, repository, chromosomeRepository, mockNcbiDataSource,
                mockEnaDataSource, chromosomeUpdater);
    }

    @AfterEach
    void tearDown() {
        for (AssemblyEntity entity : entities) {
            service.deleteEntriesForAssembly(entity.getInsdcAccession());
        }
    }

    void assertOptionalValid(Optional<AssemblyEntity> optional) {
        assertNotNull(optional);
        assertTrue(optional.isPresent());
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
            service.deleteEntriesForAssembly(entity.getInsdcAccession());
        }

        @Test
        void getAssemblyByAccession() {
            Optional<AssemblyEntity> page = service.getAssemblyByAccession(entity.getInsdcAccession());
            assertAssemblyOptionalIdenticalToEntity(page);
        }

        @Test
        void getAssemblyByInsdcAccession() {
            Optional<AssemblyEntity> page = service.getAssemblyByInsdcAccession(entity.getInsdcAccession());
            assertAssemblyOptionalIdenticalToEntity(page);
        }

        @Test
        void getAssemblyByRefseq() {
            Optional<AssemblyEntity> page = service.getAssemblyByRefseq(entity.getRefseq());
            assertAssemblyOptionalIdenticalToEntity(page);
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
            assertNotNull(page);
            assertTrue(page.getTotalElements() > 0);
            List<AssemblyEntity> entityList = page.get().collect(Collectors.toList());
            assertEquals(MAX_CONSECUTIVE_ENTITIES, entityList.size());

            for (int i = 0; i < MAX_CONSECUTIVE_ENTITIES; i++) {
                assertAssemblyEntityIdenticalToEntity(entities[i], entityList.get(i));
            }

            for (AssemblyEntity assemblyEntity : entities) {
                service.deleteEntriesForAssembly(assemblyEntity.getInsdcAccession());
            }
        }

        @Test
        void putAssemblyChecksumsByAccession() {
            String md5 = "MyCustomMd5ChecksumForTesting";
            String trunc512 = "MyCustomTrunc512ChecksumForTesting";
            service.putAssemblyChecksumsByAccession(
                    entity.getInsdcAccession(), md5, trunc512);
            Optional<AssemblyEntity> accession = service.getAssemblyByAccession(entity.getInsdcAccession());
            assertAssemblyOptionalIdenticalToEntity(accession);
            AssemblyEntity assemblyEntity = accession.get();
            assertEquals(md5, assemblyEntity.getMd5checksum());
            assertEquals(trunc512, assemblyEntity.getTrunc512checksum());
        }

        void assertAssemblyOptionalIdenticalToEntity(Optional<AssemblyEntity> optional) {
            assertOptionalValid(optional);
            assertAssemblyEntityIdenticalToEntity(optional.get());
        }

        void assertAssemblyEntityIdenticalToEntity(AssemblyEntity actual) {
            assertAssemblyEntityIdenticalToEntity(this.entity, actual);
        }

        void assertAssemblyEntityIdenticalToEntity(AssemblyEntity expect, AssemblyEntity actual) {
            assertEquals(expect.getName(), actual.getName());
            assertEquals(expect.getOrganism(), actual.getOrganism());
            assertEquals(expect.getInsdcAccession(), actual.getInsdcAccession());
            assertEquals(expect.getRefseq(), actual.getRefseq());
            assertEquals(expect.getTaxid(), actual.getTaxid());
            assertEquals(expect.isGenbankRefseqIdentical(), actual.isGenbankRefseqIdentical());
        }
    }

}
