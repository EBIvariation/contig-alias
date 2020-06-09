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

package com.ebivariation.contigalias.service;

import com.ebivariation.contigalias.datasource.AssemblyDataSource;
import com.ebivariation.contigalias.entities.AssemblyEntity;
import com.ebivariation.contigalias.entities.ChromosomeEntity;
import com.ebivariation.contigalias.entitygenerator.AssemblyGenerator;
import com.ebivariation.contigalias.repo.AssemblyRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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
        Optional<AssemblyEntity> entity = service.getAssemblyOrFetchByAccession(entities[0].getGenbank());
        assertTrue(entity.isPresent());
        service.deleteAssembly(entity.get());
    }

    @Test
    void cacheLimitTest() throws IOException {
        int cacheSize = service.getCacheSize();
        service.setCacheSize(10);

        String targetGenbank = entities[0].getGenbank();
        service.fetchAndInsertAssembly(targetGenbank);
        Optional<AssemblyEntity> assembly = service.getAssemblyByAccession(targetGenbank);
        assertTrue(assembly.isPresent());
        List<ChromosomeEntity> chromosomes = assembly.get().getChromosomes();
        assertNotNull(chromosomes);

        for (int i = 1; i < TEST_ENTITIES_NUMBERS; i++) {
            String genbank = entities[i].getGenbank();
            service.fetchAndInsertAssembly(genbank);
            Optional<AssemblyEntity> entity = service.getAssemblyByAccession(genbank);
            assertTrue(entity.isPresent());
        }

        Optional<AssemblyEntity> targetGenbankEntity = service.getAssemblyByAccession(targetGenbank);
        assertFalse(targetGenbankEntity.isPresent());

        for (int i = 1; i < TEST_ENTITIES_NUMBERS; i++) {
            String genbank = entities[i].getGenbank();
            Optional<AssemblyEntity> accession = service.getAssemblyByAccession(genbank);
            assertTrue(accession.isPresent());
            service.deleteAssembly(accession.get());
        }

        service.setCacheSize(cacheSize);
    }

    @Nested
    class NoDataSource {

        private final AssemblyEntity entity = AssemblyGenerator.generate();

        @BeforeEach
        void setup() {
            service.insertAssembly(entity);
        }

        @AfterEach
        void tearDown() {
            service.deleteAssembly(entity);
        }

        @Test
        void getAssemblyByAccession() {
            Optional<AssemblyEntity> accession = service.getAssemblyByAccession(entity.getGenbank());
            assertTrue(accession.isPresent());
            AssemblyEntity entity = accession.get();
            assertEquals(entity.getName(), entity.getName());
            assertEquals(entity.getOrganism(), entity.getOrganism());
            assertEquals(entity.getGenbank(), entity.getGenbank());
            assertEquals(entity.getRefseq(), entity.getRefseq());
            assertEquals(entity.getTaxid(), entity.getTaxid());
            assertEquals(entity.isGenbankRefseqIdentical(), entity.isGenbankRefseqIdentical());
        }

    }
}
