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


import com.ebivariation.contigalias.entities.AssemblyEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class AssemblyServiceTest {

    private static final String ASSEMBLY_NAME = "Fakus Animulus";

    private static final String ASSEMBLY_ORGANISM = "Fakus_Animulus_(Cattle)";

    private static final String ASSEMBLY_GENBANK = "GCA648945645.7";

    private static final String ASSEMBLY_REFSEQ = "GCF915656489";

    private static final long ASSEMBLY_TAXID = 9834;

    private static final boolean ASSEMBLY_GENBANK_REFSEQ_IDENTICAL = false;

    @Autowired
    private AssemblyService service;

    private AssemblyEntity entity;

    @BeforeEach
    void setup() {
        entity = new AssemblyEntity();
        entity.setName(ASSEMBLY_NAME)
              .setOrganism(ASSEMBLY_ORGANISM)
              .setGenbank(ASSEMBLY_GENBANK)
              .setRefseq(ASSEMBLY_REFSEQ)
              .setTaxid(ASSEMBLY_TAXID)
              .setGenbankRefseqIdentical(ASSEMBLY_GENBANK_REFSEQ_IDENTICAL);
        service.insertAssembly(entity);
    }

    @AfterEach
    void tearDown() {
        service.deleteAssembly(entity);
    }

    @Test
    void getAssemblyByAccessionGCA() {
        Optional<AssemblyEntity> accession = service.getAssemblyByAccession(ASSEMBLY_GENBANK);
        assertTrue(accession.isPresent());
    }

    @Test
    void getAssemblyOrFetchByAccession() {
        Optional<AssemblyEntity> entity = service.getAssemblyOrFetchByAccession("GCF_006125015.1");
        assertTrue(entity.isPresent());
    }
}
