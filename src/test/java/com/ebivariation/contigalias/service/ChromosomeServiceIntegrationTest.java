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

import com.ebivariation.contigalias.entities.ChromosomeEntity;
import com.ebivariation.contigalias.entitygenerator.ChromosomeGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Optional<ChromosomeEntity> accession = service.getChromosomeByGenbank(entity.getGenbank());
        assertTrue(accession.isPresent());
        testChromosomeIdenticalToEntity(accession.get());
    }

    @Test
    void getChromosomeByRefseq() {
        Optional<ChromosomeEntity> accession = service.getChromosomeByRefseq(entity.getRefseq());
        assertTrue(accession.isPresent());
        testChromosomeIdenticalToEntity(accession.get());
    }

    void testChromosomeIdenticalToEntity(ChromosomeEntity chromosomeEntity) {
        assertEquals(entity.getName(), chromosomeEntity.getName());
        assertEquals(entity.getGenbank(), chromosomeEntity.getGenbank());
        assertEquals(entity.getRefseq(), chromosomeEntity.getRefseq());
    }


}
