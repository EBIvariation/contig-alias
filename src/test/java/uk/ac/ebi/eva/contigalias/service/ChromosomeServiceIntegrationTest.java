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

import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;

import java.util.Collections;
import java.util.List;

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
        service.updateMd5ChecksumForAll(Collections.singletonList(entity));

        Page<ChromosomeEntity> chrPage = service.getChromosomesByInsdcAccession(entity.getInsdcAccession(), Pageable.unpaged());
        assertChromosomePageIdenticalToEntity(chrPage);
        assertEquals(testMD5Checksum, chrPage.getContent().get(0).getMd5checksum());

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

}
