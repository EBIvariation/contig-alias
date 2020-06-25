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
import com.ebivariation.contigalias.entities.ChromosomeEntity;
import com.ebivariation.contigalias.entitygenerator.AssemblyGenerator;
import com.ebivariation.contigalias.entitygenerator.ChromosomeGenerator;
import com.ebivariation.contigalias.service.AssemblyService;
import com.ebivariation.contigalias.service.ChromosomeService;
import com.ebivariation.contigalias.test.TestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * See https://spring.io/guides/gs/testing-web/ for an explanation of the particular combination of Spring
 * annotations that were used in this test class.
 * <p>
 * See https://github.com/json-path/JsonPath for the jsonPath syntax.
 */
@WebMvcTest(ContigAliasController.class)
@Import(TestConfiguration.class)
public class ContigAliasControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssemblyService mockAssemblyService;

    @MockBean
    private ChromosomeService mockChromosomeService;

    @Nested
    class AssemblyServiceTests {

        private final AssemblyEntity entity = AssemblyGenerator.generate();


        @BeforeEach
        void setUp() {
            when(mockAssemblyService.getAssemblyByAccession(entity.getGenbank()))
                    .thenReturn(Optional.of(entity));
            when(mockAssemblyService.getAssemblyByGenbank(entity.getGenbank()))
                    .thenReturn(Optional.of(entity));
            when(mockAssemblyService.getAssemblyByRefseq(entity.getRefseq()))
                    .thenReturn(Optional.of(entity));
        }

        @Test
        void getAssemblyByAccessionGCAHavingChromosomes() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/{accession}", entity.getGenbank()));
            assertAssemblyIdenticalToEntity(resultActions);
        }

        @Test
        void getAssemblyByGenbank() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/genbank/{genbank}", entity.getGenbank()));
            assertAssemblyIdenticalToEntity(resultActions);
        }

        @Test
        void getAssemblyByRefseq() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/refseq/{refseq}", entity.getRefseq()));
            assertAssemblyIdenticalToEntity(resultActions);
        }

        void assertAssemblyIdenticalToEntity(ResultActions actions) throws Exception {
            actions.andExpect(status().isOk())
                   .andExpect(jsonPath("$.id").doesNotExist())
                   .andExpect(jsonPath("$.name", is(entity.getName())))
                   .andExpect(jsonPath("$.organism", is(entity.getOrganism())))
                   .andExpect(jsonPath("$.taxid").value(entity.getTaxid()))
                   .andExpect(jsonPath("$.genbank", is(entity.getGenbank())))
                   .andExpect(jsonPath("$.refseq", is(entity.getRefseq())))
                   .andExpect(jsonPath("$.genbankRefseqIdentical", is(entity.isGenbankRefseqIdentical())));
        }

        @Test
        void test404NotFound() throws Exception {
            mockMvc.perform(get("/contig-alias/v1/assemblies/{accession}", "##INVALID##"))
                   .andExpect(status().isNotFound());
            mockMvc.perform(get("/contig-alias/v1/assemblies/genbank/{genbank}", entity.getRefseq()))
                   .andExpect(status().isNotFound());
            mockMvc.perform(get("/contig-alias/v1/assemblies/refseq/{refseq}", entity.getGenbank()))
                   .andExpect(status().isNotFound());
        }
    }

    @Nested
    class ChromosomeServiceTests {

        private final ChromosomeEntity entity = ChromosomeGenerator.generate();

        @BeforeEach
        void setUp() {
            when(mockChromosomeService.getChromosomeByGenbank(entity.getGenbank()))
                    .thenReturn(Optional.of(entity));
            when(mockChromosomeService.getChromosomeByRefseq(entity.getRefseq()))
                    .thenReturn(Optional.of(entity));
        }

        @Test
        void getChromosomeByGenbank() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/chromosomes/genbank/{genbank}", entity.getGenbank()));
            assertChromosomeIdenticalToEntity(resultActions);
        }

        @Test
        void getChromosomeByRefseq() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/chromosomes/refseq/{refseq}", entity.getRefseq()));
            assertChromosomeIdenticalToEntity(resultActions);
        }

        void assertChromosomeIdenticalToEntity(ResultActions actions) throws Exception {
            actions.andExpect(status().isOk())
                   .andExpect(jsonPath("$.id").doesNotExist())
                   .andExpect(jsonPath("$.name", is(entity.getName())))
                   .andExpect(jsonPath("$.genbank", is(entity.getGenbank())))
                   .andExpect(jsonPath("$.refseq", is(entity.getRefseq())));
        }

        @Test
        void test404NotFound() throws Exception {
            mockMvc.perform(get("/contig-alias/v1/chromosomes/genbank/{genbank}", entity.getRefseq()))
                   .andExpect(status().isNotFound());
            mockMvc.perform(get("/contig-alias/v1/chromosomes/refseq/{refseq}", entity.getGenbank()))
                   .andExpect(status().isNotFound());
        }

    }
}
