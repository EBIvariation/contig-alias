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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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
    void setUp() {
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
    public void getAssemblyByAccession() {
        ResponseEntity<Optional<AssemblyEntity>> assemblyByAccession = controller.getAssemblyByAccession(
                ASSEMBLY_GENBANK_ACCESSION);
        assertEquals(assemblyByAccession.getStatusCode(), HttpStatus.OK);
        assertTrue(assemblyByAccession.hasBody());
        AssemblyEntity entity = assemblyByAccession.getBody().get();
        assertEquals(entity.getName(), ASSEMBLY_NAME);
        assertEquals(entity.getOrganism(), ASSEMBLY_ORGANISM_NAME);
        assertEquals(entity.getGenbank(), ASSEMBLY_GENBANK_ACCESSION);
        assertEquals(entity.getRefseq(), ASSEMBLY_REFSEQ_ACCESSION);
        assertEquals(entity.getTaxid(), ASSEMBLY_TAX_ID);
        assertEquals(entity.isGenbankRefseqIdentical(), ASSEMBLY_IS_GENBANK_REFSEQ_IDENTICAL);

    }
}
