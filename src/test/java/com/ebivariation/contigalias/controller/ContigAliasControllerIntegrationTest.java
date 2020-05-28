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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * See https://spring.io/guides/gs/testing-web/ for an explanation of the particular combination of Spring
 * annotations that were used in this test class.
 *
 * See https://github.com/json-path/JsonPath for the jsonPath syntax.
 */
@WebMvcTest(ContigAliasController.class)
public class ContigAliasControllerIntegrationTest {

    public static final String ASSEMBLY_NAME = "Bos_taurus_UMD_3.1";

    public static final String ASSEMBLY_ORGANISM_NAME = "Bos taurus (cattle)";

    public static final long ASSEMBLY_TAX_ID = 9913;

    public static final String ASSEMBLY_GENBANK_ACCESSION = "GCA_000003055.3";

    public static final String ASSEMBLY_REFSEQ_ACCESSION = "GCF_000003055.3";

    public static final boolean ASSEMBLY_IS_GENBANK_REFSEQ_IDENTICAL = true;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssemblyService mockAssemblyService;

    @BeforeEach
    void setUp() throws IOException {
        AssemblyEntity entity = new AssemblyEntity()
                .setName(ASSEMBLY_NAME)
                .setOrganism(ASSEMBLY_ORGANISM_NAME)
                .setGenbank(ASSEMBLY_GENBANK_ACCESSION)
                .setRefseq(ASSEMBLY_REFSEQ_ACCESSION)
                .setTaxid(ASSEMBLY_TAX_ID)
                .setGenbankRefseqIdentical(ASSEMBLY_IS_GENBANK_REFSEQ_IDENTICAL);

        when(mockAssemblyService.getAssemblyOrFetchByAccession(ASSEMBLY_GENBANK_ACCESSION))
               .thenReturn(Optional.of(entity));
    }

    @Test
    public void getAssemblyByAccessionGCAHavingChromosomes() throws Exception {
        this.mockMvc.perform(get("/contig-alias/assemblies/{accession}", ASSEMBLY_GENBANK_ACCESSION))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name", is(ASSEMBLY_NAME)));
    }

}
