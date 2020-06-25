/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ebivariation.contigalias.controller;

import com.ebivariation.contigalias.entities.AssemblyEntity;
import com.ebivariation.contigalias.entitygenerator.AssemblyGenerator;
import com.ebivariation.contigalias.service.AssemblyService;
import com.ebivariation.contigalias.test.TestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "ADMIN")
@WebMvcTest(AdminController.class)
@Import(TestConfiguration.class)
public class AdminControllerIntegrationTest {

    private final AssemblyEntity entity = AssemblyGenerator.generate();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssemblyService mockAssemblyService;

    @BeforeEach
    void setUp() throws IOException {
        when(mockAssemblyService.getAssemblyOrFetchByAccession(entity.getGenbank()))
                .thenReturn(Optional.of(entity));
        when(mockAssemblyService.getAssemblyOrFetchByAccession(entity.getRefseq()))
                .thenReturn(Optional.of(entity));
    }

    @Test
    public void getAssemblyOrFetchByAccessionGCA() throws Exception {
        ResultActions request = this.mockMvc.perform(
                get("/contig-alias-admin/v1/assemblies/{accession}", entity.getGenbank()));
        assertAssemblyIdenticalToEntity(request);
    }

    @Test
    public void getAssemblyOrFetchByAccessionGCF() throws Exception {
        ResultActions request = this.mockMvc.perform(
                get("/contig-alias-admin/v1/assemblies/{accession}", entity.getRefseq()));
        assertAssemblyIdenticalToEntity(request);
    }

    private void assertAssemblyIdenticalToEntity(ResultActions request) throws Exception {
        request.andExpect(status().isOk())
               .andExpect(jsonPath("$.id").doesNotExist())
               .andExpect(jsonPath("$.name", is(entity.getName())))
               .andExpect(jsonPath("$.organism", is(entity.getOrganism())))
               .andExpect(jsonPath("$.taxid").value(entity.getTaxid()))
               .andExpect(jsonPath("$.genbank", is(entity.getGenbank())))
               .andExpect(jsonPath("$.refseq", is(entity.getRefseq())))
               .andExpect(jsonPath("$.genbankRefseqIdentical", is(entity.isGenbankRefseqIdentical())));
    }

    @Test
    public void test404NotFound() throws Exception {
        this.mockMvc.perform(get("/contig-alias-admin/v1/assemblies/{accession}", "##INVALID##"))
                    .andExpect(status().isNotFound());
    }
}
