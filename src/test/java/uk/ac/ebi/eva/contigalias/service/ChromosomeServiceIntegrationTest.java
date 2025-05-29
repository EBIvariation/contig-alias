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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.repo.ChromosomeRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;

@ActiveProfiles("test")
@SpringBootTest
public class ChromosomeServiceIntegrationTest {

    private final ChromosomeEntity entity = ChromosomeGenerator.generate(AssemblyGenerator.generate());

    @Autowired
    private ChromosomeService service;

    @Autowired
    private ChromosomeRepository chromosomeRepository;

    @BeforeEach
    void setup() {
        chromosomeRepository.deleteAll();
        service.insertChromosome(entity);
    }

    @AfterEach
    void tearDown() {
        chromosomeRepository.deleteAll();
    }

    @Test
    void getChromosomeByGenbank() {
        Page<ChromosomeEntity> page = service.getChromosomesByInsdcAccession(
                entity.getInsdcAccession(), DEFAULT_PAGE_REQUEST);
        assertChromosomePageIdenticalToEntity(page);
    }

    @Test
    void getChromosomeByRefseq() {
        Page<ChromosomeEntity> page = service.getChromosomesByRefseq(
                entity.getRefseq(), DEFAULT_PAGE_REQUEST);
        assertChromosomePageIdenticalToEntity(page);
    }

    @Test
    void putChromosomeChecksumsByAccession() {
        String md5 = "MyCustomMd5ChecksumForTesting";
        String trunc512 = "MyCustomTrunc512ChecksumForTesting";
        service.putChromosomeChecksumsByAccession(entity.getInsdcAccession(), md5, trunc512);
        Page<ChromosomeEntity> page = service.getChromosomesByInsdcAccession(entity.getInsdcAccession(), Pageable.unpaged());
        assertChromosomePageIdenticalToEntity(page);
        page.forEach(chromosomeEntity -> {
            assertEquals(md5, chromosomeEntity.getMd5checksum());
            assertEquals(trunc512, chromosomeEntity.getTrunc512checksum());
        });
    }

    @Test
    void testGetAssemblyWhereChromosomeMd5ChecksumIsNullOrEmpty() {
        List<String> asmList = service.getAssembliesWhereChromosomeMd5ChecksumIsNull();
        assertEquals(entity.getAssembly().getInsdcAccession(), asmList.get(0));
    }

    @Test
    void testGetChromosomesByAssemblyInsdcAccessionWhereMd5ChecksumIsNull() {
        Page<ChromosomeEntity> chrPage = service.getChromosomesByAssemblyInsdcAccessionWhereMd5ChecksumIsNull(entity.getAssembly().getInsdcAccession(), PageRequest.of(0, 100));
        assertChromosomePageIdenticalToEntity(chrPage);
        assertEquals(null, chrPage.getContent().get(0).getMd5checksum());
    }

    @Test
    void testUpdateMD5ChecksumForAllChromosomesInAssembly() {
        String testMD5Checksum = "testmd5checksum";
        entity.setMd5checksum(testMD5Checksum);
        service.updateMd5ChecksumForAllChromosomeInAssembly(entity.getAssembly().getInsdcAccession(),
                Collections.singletonList(entity));

        Page<ChromosomeEntity> chrPage = service.getChromosomesByInsdcAccession(entity.getInsdcAccession(), Pageable.unpaged());
        assertChromosomePageIdenticalToEntity(chrPage);
        assertEquals(testMD5Checksum, chrPage.getContent().get(0).getMd5checksum());

    }

    @Test
    void testGetChromosomesByMD5Checksum() {
        String testMD5Checksum = "test-MD5-checksum";

        AssemblyEntity assemblyEntity = AssemblyGenerator.generate();
        ChromosomeEntity chromosomeWithMD5 = ChromosomeGenerator.generate(assemblyEntity);
        chromosomeWithMD5.setMd5checksum(testMD5Checksum);
        chromosomeRepository.save(chromosomeWithMD5);

        Page<ChromosomeEntity> chrPage = service.getChromosomesByMD5Checksum(testMD5Checksum, Pageable.unpaged());
        List<ChromosomeEntity> chromosomeList = chrPage.getContent();

        assertEquals(1, chromosomeList.size());
        assertChromosomesIdentical(chromosomeWithMD5, chromosomeList.get(0));
        assertEquals(testMD5Checksum, chromosomeList.get(0).getMd5checksum());
        assertEquals(assemblyEntity.getInsdcAccession(), chromosomeList.get(0).getAssembly().getInsdcAccession());
    }

    @Test
    void testGetMultipleChromosomesByMD5ChecksumInDifferentAssemblies() {
        String testMD5Checksum = "test-MD5-checksum";

        AssemblyEntity assemblyEntity1 = AssemblyGenerator.generate();
        assemblyEntity1.setInsdcAccession("assembly1");
        ChromosomeEntity chromosomeWithMD51 = ChromosomeGenerator.generate(assemblyEntity1);
        chromosomeWithMD51.setInsdcAccession("chromosome1");
        chromosomeWithMD51.setMd5checksum(testMD5Checksum);
        chromosomeRepository.save(chromosomeWithMD51);

        AssemblyEntity assemblyEntity2 = AssemblyGenerator.generate();
        assemblyEntity2.setInsdcAccession("assembly2");
        ChromosomeEntity chromosomeWithMD52 = ChromosomeGenerator.generate(assemblyEntity2);
        chromosomeWithMD52.setInsdcAccession("chromosome2");
        chromosomeWithMD52.setMd5checksum(testMD5Checksum);
        chromosomeRepository.save(chromosomeWithMD52);

        // Contig with same INSDC accession, but in a different assembly
        AssemblyEntity assemblyEntity3 = AssemblyGenerator.generate();
        assemblyEntity3.setInsdcAccession("assembly3");
        ChromosomeEntity chromosomeWithMD53 = ChromosomeGenerator.generate(assemblyEntity3);
        chromosomeWithMD53.setInsdcAccession("chromosome1");
        chromosomeWithMD53.setMd5checksum(testMD5Checksum);
        chromosomeRepository.save(chromosomeWithMD53);

        Page<ChromosomeEntity> chrPage = service.getChromosomesByMD5Checksum(testMD5Checksum, Pageable.unpaged());

        List<ChromosomeEntity> chromosomeList = chrPage.getContent();
        assertEquals(2, chromosomeList.size());

        assertEquals(testMD5Checksum, chromosomeList.get(0).getMd5checksum());
        assertChromosomesIdentical(chromosomeWithMD52, chromosomeList.get(0));
        assertEquals("assembly2", chromosomeList.get(0).getAssembly().getInsdcAccession());

        assertEquals(testMD5Checksum, chromosomeList.get(1).getMd5checksum());
        assertChromosomesIdentical(chromosomeWithMD53, chromosomeList.get(1));
        assertEquals("assembly3", chromosomeList.get(1).getAssembly().getInsdcAccession());

        assertEquals(testMD5Checksum, chromosomeList.get(2).getMd5checksum());
        assertChromosomesIdentical(chromosomeWithMD51, chromosomeList.get(2));
        assertEquals("assembly1", chromosomeList.get(2).getAssembly().getInsdcAccession());
    }

    void assertChromosomePageIdenticalToEntity(Page<ChromosomeEntity> page) {
        assertNotNull(page);
        assertTrue(page.getTotalElements() > 0);
        page.forEach(this::assertChromosomeIdenticalToEntity);
    }

    void assertChromosomeIdenticalToEntity(ChromosomeEntity chromosomeEntity) {
        assertEquals(entity.getGenbankSequenceName(), chromosomeEntity.getGenbankSequenceName());
        assertEquals(entity.getInsdcAccession(), chromosomeEntity.getInsdcAccession());
        assertEquals(entity.getRefseq(), chromosomeEntity.getRefseq());
        assertEquals(entity.getUcscName(), chromosomeEntity.getUcscName());
        assertEquals(entity.getEnaSequenceName(), chromosomeEntity.getEnaSequenceName());
    }

    void assertChromosomesIdentical(ChromosomeEntity chromosomeEntity1, ChromosomeEntity chromosomeEntity2) {
        assertEquals(chromosomeEntity1.getGenbankSequenceName(), chromosomeEntity2.getGenbankSequenceName());
        assertEquals(chromosomeEntity1.getInsdcAccession(), chromosomeEntity2.getInsdcAccession());
        assertEquals(chromosomeEntity1.getRefseq(), chromosomeEntity2.getRefseq());
        assertEquals(chromosomeEntity1.getUcscName(), chromosomeEntity2.getUcscName());
        assertEquals(chromosomeEntity1.getEnaSequenceName(), chromosomeEntity2.getEnaSequenceName());
    }

}
