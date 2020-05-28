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

package com.ebivariation.contigalias.controller;

import com.ebivariation.contigalias.entities.AssemblyEntity;
import com.ebivariation.contigalias.service.AssemblyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class ContigAliasControllerTest {

    public static final String ASSEMBLY_NAME = "Bos_taurus_UMD_3.1";

    public static final String ASSEMBLY_ORGANISM_NAME = "Bos taurus (cattle)";

    public static final long ASSEMBLY_TAX_ID = 9913;

    public static final String ASSEMBLY_GENBANK_ACCESSION = "GCA_000003055.3";

    public static final String ASSEMBLY_REFSEQ_ACCESSION = "GCF_000003055.3";

    public static final boolean ASSEMBLY_IS_GENBANK_REFSEQ_IDENTICAL = true;

    private ContigAliasController controller;

    @BeforeEach
    void setUp() throws IOException {
        AssemblyEntity entity = new AssemblyEntity()
                .setName(ASSEMBLY_NAME)
                .setOrganism(ASSEMBLY_ORGANISM_NAME)
                .setGenbank(ASSEMBLY_GENBANK_ACCESSION)
                .setRefseq(ASSEMBLY_REFSEQ_ACCESSION)
                .setTaxid(ASSEMBLY_TAX_ID)
                .setGenbankRefseqIdentical(ASSEMBLY_IS_GENBANK_REFSEQ_IDENTICAL);

        AssemblyService mockAssemblyService = mock(AssemblyService.class);
        Mockito.when(mockAssemblyService.getAssemblyByAccession(ASSEMBLY_GENBANK_ACCESSION))
               .thenReturn(Optional.of(entity));

        controller = new ContigAliasController(mockAssemblyService);
    }

    @Test
    public void getAssemblyByAccession() throws Exception {
        Optional<AssemblyEntity> assemblyByAccession = controller.getAssemblyByAccession(ASSEMBLY_GENBANK_ACCESSION);
        assertTrue(assemblyByAccession.isPresent());
        assertEquals(ASSEMBLY_NAME, assemblyByAccession.get().getName());
    }
}
