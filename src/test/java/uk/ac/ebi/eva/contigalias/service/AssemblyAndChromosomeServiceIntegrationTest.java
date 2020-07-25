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
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;

@ActiveProfiles("test")
@SpringBootTest
public class AssemblyAndChromosomeServiceIntegrationTest {

    @Autowired
    private ChromosomeService service;

    @Autowired
    private AssemblyService assemblyService;

    void assertChromosomeEntityIdentical(ChromosomeEntity expected, ChromosomeEntity actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getGenbank(), actual.getGenbank());
        assertEquals(expected.getRefseq(), actual.getRefseq());
    }

    @Nested
    class SingleAssemblyMultipleChromosomes {

        private final AssemblyEntity assemblyEntity = AssemblyGenerator.generate();

        private final List<ChromosomeEntity> chromosomeEntities = new LinkedList<>();

        private final int CHROMOSOME_LIST_SIZE = 5;

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
            Page<ChromosomeEntity> chromosomes = service.getChromosomesByAssemblyGenbank(assemblyEntity.getGenbank(),
                                                                                         DEFAULT_PAGE_REQUEST);
            assertPageEntitiesIdentical(chromosomes);
        }

        @Test
        void getChromosomesByAssemblyRefseq() {
            Page<ChromosomeEntity> chromosomes = service.getChromosomesByAssemblyRefseq(assemblyEntity.getRefseq(),
                                                                                        DEFAULT_PAGE_REQUEST);
            assertPageEntitiesIdentical(chromosomes);
        }

        private void assertPageEntitiesIdentical(Page<ChromosomeEntity> chromosomes) {
            assertNotNull(chromosomes);
            assertEquals(CHROMOSOME_LIST_SIZE, chromosomes.getTotalElements());
            List<ChromosomeEntity> entityList = chromosomes.get().collect(Collectors.toList());
            for (int i = 0; i < CHROMOSOME_LIST_SIZE; i++) {
                assertChromosomeEntityIdentical(chromosomeEntities.get(i), entityList.get(i));
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
    }

    @Nested
    class MultipleAssembliesAndChromosomes {

        AssemblyEntity[] assemblyEntities;

        ChromosomeEntity[] chromosomeEntities;

        @BeforeEach
        void setup() {
            assemblyEntities = new AssemblyEntity[3];
            chromosomeEntities = new ChromosomeEntity[4];

            // New assembly
            assemblyEntities[0] = AssemblyGenerator.generate();

            // Same taxid as previous
            AssemblyEntity taxidSameAs0th = AssemblyGenerator.generate();
            taxidSameAs0th.setTaxid(assemblyEntities[0].getTaxid());
            assemblyEntities[1] = taxidSameAs0th;

            // Totally different
            assemblyEntities[2] = AssemblyGenerator.generate();

            // New chromosome linked to 0th assembly
            chromosomeEntities[0] = ChromosomeGenerator.generate(assemblyEntities[0]);

            // Same name as previous and linked to 1st assembly
            ChromosomeEntity nameSameAs0thAsm1 = ChromosomeGenerator.generate(assemblyEntities[1]);
            nameSameAs0thAsm1.setName(chromosomeEntities[0].getName());
            chromosomeEntities[1] = nameSameAs0thAsm1;

            // Same name as previous and linked to 2nd assembly
            ChromosomeEntity nameSameAs1stAsm1 = ChromosomeGenerator.generate(assemblyEntities[2]);
            nameSameAs1stAsm1.setName(chromosomeEntities[0].getName());
            chromosomeEntities[2] = nameSameAs1stAsm1;

            // New chromosome linked to 2nd assembly
            chromosomeEntities[3] = ChromosomeGenerator.generate(assemblyEntities[2]);

            for (AssemblyEntity assemblyEntity : assemblyEntities) {
                assemblyService.insertAssembly(assemblyEntity);
            }
        }

        @AfterEach
        void tearDown() {
            for (AssemblyEntity assemblyEntity : assemblyEntities) {
                assemblyService.deleteAssembly(assemblyEntity);
            }
            assemblyEntities = null;
            chromosomeEntities = null;
        }

        /**
         * In this test two chromosomes have non-distinct names and are linked to different assemblies except that those
         * two assemblies have non-distinct Taxonomic IDs.
         */
        @Test
        void getChromosomesByNameAndAssemblySameNameSameTaxid() {
            Page<ChromosomeEntity> page = service.getChromosomesByNameAndAssemblyTaxid(
                    chromosomeEntities[0].getName(), assemblyEntities[0].getTaxid(), DEFAULT_PAGE_REQUEST);
            assertNotNull(page);
            assertEquals(2, page.getTotalElements());
            List<ChromosomeEntity> entityList = page.get().collect(Collectors.toList());
            assertChromosomeEntityIdentical(chromosomeEntities[0], entityList.get(0));
            assertChromosomeEntityIdentical(chromosomeEntities[1], entityList.get(1));
        }

        /**
         * In this test chromosome has a non-distinct name but is linked to an assembly with a distinct Taxonomic ID.
         */
        @Test
        void getChromosomesByNameAndAssemblySameNameDiffTaxid() {
            ChromosomeEntity chromosomeEntity = chromosomeEntities[2];
            Page<ChromosomeEntity> page = service.getChromosomesByNameAndAssemblyTaxid(
                    chromosomeEntity.getName(), assemblyEntities[2].getTaxid(), DEFAULT_PAGE_REQUEST);
            assertNotNull(page);
            assertEquals(1, page.getNumberOfElements());
            assertChromosomePageIdentical(page, chromosomeEntity);
        }

        /**
         * In this test chromosome has a distinct name from other chromosomes but is linked to an assembly
         * with a non-distinct Taxonomic ID.
         */
        @Test
        void getChromosomesByNameAndAssemblyDiffNameSameTaxid() {
            ChromosomeEntity chromosomeEntity = chromosomeEntities[3];
            Page<ChromosomeEntity> page = service.getChromosomesByNameAndAssemblyTaxid(
                    chromosomeEntity.getName(), assemblyEntities[2].getTaxid(), DEFAULT_PAGE_REQUEST);
            assertNotNull(page);
            assertEquals(1, page.getNumberOfElements());
            assertChromosomePageIdentical(page, chromosomeEntity);
        }

        @Test
        void getChromosomeByNameAndAssembly() {
            ChromosomeEntity chromosomeEntity = chromosomeEntities[0];
            Page<ChromosomeEntity> page = service.getChromosomesByNameAndAssembly(
                    chromosomeEntity.getName(), assemblyEntities[0], DEFAULT_PAGE_REQUEST);
            assertNotNull(page);
            assertEquals(1, page.getNumberOfElements());
            assertChromosomePageIdentical(page, chromosomeEntity);
        }

        void assertChromosomePageIdentical(Page<ChromosomeEntity> page, ChromosomeEntity chromosomeEntity) {
            page.get().forEach(it -> assertChromosomeEntityIdentical(chromosomeEntity, it));
        }

    }

}
