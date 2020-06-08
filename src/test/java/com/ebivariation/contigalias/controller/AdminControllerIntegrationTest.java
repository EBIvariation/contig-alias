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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
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
        MockHttpServletRequestBuilder requestBuilder = get("/contig-alias-admin/assemblies/{accession}",
                                                           entity.getGenbank());
        this.mockMvc.perform(requestBuilder)
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.name", is(entity.getName())))
                    .andExpect(jsonPath("$.organism", is(entity.getOrganism())))
                    .andExpect(jsonPath("$.taxid").value(entity.getTaxid()))
                    .andExpect(jsonPath("$.genbank", is(entity.getGenbank())))
                    .andExpect(jsonPath("$.refseq", is(entity.getRefseq())))
                    .andExpect(jsonPath("$.genbankRefseqIdentical", is(entity.isGenbankRefseqIdentical())));
    }

    @Test
    public void getAssemblyOrFetchByAccessionGCF() throws Exception {
        this.mockMvc.perform(get("/contig-alias-admin/assemblies/{accession}", entity.getRefseq()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").isNotEmpty())
                    .andExpect(jsonPath("$.name", is(entity.getName())))
                    .andExpect(jsonPath("$.organism", is(entity.getOrganism())))
                    .andExpect(jsonPath("$.taxid").value(entity.getTaxid()))
                    .andExpect(jsonPath("$.genbank", is(entity.getGenbank())))
                    .andExpect(jsonPath("$.refseq", is(entity.getRefseq())))
                    .andExpect(jsonPath("$.genbankRefseqIdentical", is(entity.isGenbankRefseqIdentical())));
    }

    @Test
    public void test404NotFound() throws Exception {
        this.mockMvc.perform(get("/contig-alias-admin/assemblies/{accession}", "##INVALID##"))
                    .andExpect(status().isNotFound());
    }
}
