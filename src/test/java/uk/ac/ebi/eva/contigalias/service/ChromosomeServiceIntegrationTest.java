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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class ChromosomeServiceIntegrationTest {

    private final ChromosomeEntity entity = ChromosomeGenerator.generate();

    @Autowired
    private ChromosomeService service;

    @BeforeEach
    void setup() {
        service.insertChromosome(entity);
    }

    @AfterEach
    void tearDown() {
        service.deleteChromosome(entity);
    }

    @Test
    void getChromosomeByGenbank() {
        Optional<ChromosomeEntity> chromosomes = service.getChromosomeByGenbank(entity.getGenbank());
        assertChromosomePageIdenticalToEntity(chromosomes);
    }

    @Test
    void getChromosomeByRefseq() {
        Optional<ChromosomeEntity> chromosomes = service.getChromosomeByRefseq(entity.getRefseq());
        assertChromosomePageIdenticalToEntity(chromosomes);
    }

    void assertChromosomePageIdenticalToEntity(Optional<ChromosomeEntity> entity) {
        assertNotNull(entity);
        assertTrue(entity.isPresent());
        assertChromosomeIdenticalToEntity(entity.get());
    }

    void assertChromosomeIdenticalToEntity(ChromosomeEntity chromosomeEntity) {
        assertEquals(entity.getName(), chromosomeEntity.getName());
        assertEquals(entity.getGenbank(), chromosomeEntity.getGenbank());
        assertEquals(entity.getRefseq(), chromosomeEntity.getRefseq());
    }

}
