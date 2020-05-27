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
import com.ebivariation.contigalias.entities.ChromosomeEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@SpringBootTest
public class AssemblyServiceTest {

    @Autowired
    private AssemblyService service;

    @Test
    void cacheLimitTest() throws IOException {

        String ACCESSION_BOS_TAURUS = "GCA_000003055.3";

        service.fetchAndInsertAssembly(ACCESSION_BOS_TAURUS);
        Optional<AssemblyEntity> assembly = service.getAssemblyByAccession(ACCESSION_BOS_TAURUS);
        assertTrue(assembly.isPresent());
        List<ChromosomeEntity> chromosomes = assembly.get().getChromosomes();
        assertNotNull(chromosomes);
        assertTrue(chromosomes.size() > 0);

        String[] accessions = new String[]{
                "GCA_006125015.1",
                "GCA_007007145.1",
                "GCA_007003565.1",
                "GCA_002041205.1",
                "GCA_002021185.1",
                "GCA_007608995.1",
                "GCA_011100115.1",
                "GCA_004051055.1",
                "GCA_004051015.1",
                "GCA_004031555.1"
        };

        for (String accession : accessions) {
            service.fetchAndInsertAssembly(accession);
        }

        Optional<AssemblyEntity> assembly1 = service.getAssemblyByAccession(ACCESSION_BOS_TAURUS);
        assertFalse(assembly1.isPresent());
    }

    @Nested
    class WithFakeData {

        private static final String ASSEMBLY_NAME = "Fakus Animulus";

        private static final String ASSEMBLY_ORGANISM = "Fakus_Animulus_(Cattle)";

        private static final String ASSEMBLY_GENBANK = "GCA_648945645.7";

        private static final String ASSEMBLY_REFSEQ = "GCF_915656489.3";

        private static final long ASSEMBLY_TAXID = 9834;

        private static final boolean ASSEMBLY_GENBANK_REFSEQ_IDENTICAL = false;

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
        void getAssemblyOrFetchByAccession() throws IOException {
            Optional<AssemblyEntity> entity = service.getAssemblyOrFetchByAccession("GCF_006125015.1");
            assertTrue(entity.isPresent());
        }
    }
}
