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
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.contigalias.entities.ScaffoldEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.ScaffoldGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;

@ActiveProfiles("test")
@SpringBootTest
public class ScaffoldServiceIntegrationTest {

    private final ScaffoldEntity entity = ScaffoldGenerator.generate();

    @Autowired
    private ScaffoldService service;

    @BeforeEach
    void setup() {
        service.insertScaffold(entity);
    }

    @AfterEach
    void tearDown() {
        service.deleteScaffold(entity);
    }

    @Test
    void getScaffoldByGenbank() {
        Page<ScaffoldEntity> page = service.getScaffoldsByGenbank(
                entity.getGenbank(), DEFAULT_PAGE_REQUEST);
        assertScaffoldPageIdenticalToEntity(page);
    }

    @Test
    void getScaffoldByRefseq() {
        Page<ScaffoldEntity> page = service.getScaffoldsByRefseq(
                entity.getRefseq(), DEFAULT_PAGE_REQUEST);
        assertScaffoldPageIdenticalToEntity(page);
    }

    void assertScaffoldPageIdenticalToEntity(Page<ScaffoldEntity> page) {
        assertNotNull(page);
        assertTrue(page.getTotalElements() > 0);
        page.forEach(this::assertScaffoldIdenticalToEntity);
    }

    void assertScaffoldIdenticalToEntity(ScaffoldEntity scaffoldEntity) {
        assertEquals(entity.getName(), scaffoldEntity.getName());
        assertEquals(entity.getGenbank(), scaffoldEntity.getGenbank());
        assertEquals(entity.getRefseq(), scaffoldEntity.getRefseq());
        assertEquals(entity.getUcscName(), scaffoldEntity.getUcscName());
    }

}
