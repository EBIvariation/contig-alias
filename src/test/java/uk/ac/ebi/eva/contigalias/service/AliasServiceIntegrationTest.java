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

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class AliasServiceIntegrationTest {

    private final AssemblyEntity assemblyEntity = AssemblyGenerator.generate();

    private final List<ChromosomeEntity> chromosomeEntities = new LinkedList<>();

    private final int CHROMOSOME_LIST_SIZE = 5;

    @Autowired
    private AliasService service;

    @Autowired
    private AssemblyService assemblyService;

    @BeforeEach
    void setup() {
        for (int i = 0; i < CHROMOSOME_LIST_SIZE; i++) {
            chromosomeEntities.add(ChromosomeGenerator.generate(i, assemblyEntity));
        }
        assemblyService.insertAssembly(assemblyEntity);
    }

    @AfterEach
    void tearDown() {
        chromosomeEntities.clear();
        assemblyService.deleteAssembly(assemblyEntity);
    }

    @Test
    void getAssemblyByChromosomeGenbank() {
        for (ChromosomeEntity chromosomeEntity : chromosomeEntities) {
            Optional<AssemblyEntity> entity = service.getAssemblyByChromosomeGenbank(
                    chromosomeEntity.getGenbank());
            testAssemblyIdenticalToEntity(entity);
        }
    }

    @Test
    void getAssemblyByChromosomeRefseq() {
        for (ChromosomeEntity chromosomeEntity : chromosomeEntities) {
            Optional<AssemblyEntity> entity = service.getAssemblyByChromosomeRefseq(
                    chromosomeEntity.getRefseq());
            testAssemblyIdenticalToEntity(entity);
        }
    }

    @Test
    void getChromosomesByAssemblyGenbank() {
        List<ChromosomeEntity> chromosomes = service.getChromosomesByAssemblyGenbank(assemblyEntity.getGenbank());
        assertNotNull(chromosomes);
        assertEquals(CHROMOSOME_LIST_SIZE, chromosomes.size());
        for (int i = 0; i < CHROMOSOME_LIST_SIZE; i++) {
            testChromosomeEntityEquals(chromosomeEntities.get(i), chromosomes.get(i));
        }
    }

    @Test
    void getChromosomesByAssemblyRefseq() {
        List<ChromosomeEntity> chromosomes = service.getChromosomesByAssemblyRefseq(assemblyEntity.getRefseq());
        assertNotNull(chromosomes);
        assertEquals(CHROMOSOME_LIST_SIZE, chromosomes.size());
        for (int i = 0; i < CHROMOSOME_LIST_SIZE; i++) {
            testChromosomeEntityEquals(chromosomeEntities.get(i), chromosomes.get(i));
        }
    }

    void testAssemblyIdenticalToEntity(Optional<AssemblyEntity> optional) {
        assertTrue(optional.isPresent());
        AssemblyEntity assembly = optional.get();
        assertEquals(assemblyEntity.getName(), assembly.getName());
        assertEquals(assemblyEntity.getOrganism(), assembly.getOrganism());
        assertEquals(assemblyEntity.getGenbank(), assembly.getGenbank());
        assertEquals(assemblyEntity.getRefseq(), assembly.getRefseq());
        assertEquals(assemblyEntity.getTaxid(), assembly.getTaxid());
        assertEquals(assemblyEntity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
    }

    void testChromosomeEntityEquals(ChromosomeEntity expected, ChromosomeEntity actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getGenbank(), actual.getGenbank());
        assertEquals(expected.getRefseq(), actual.getRefseq());
    }

}
