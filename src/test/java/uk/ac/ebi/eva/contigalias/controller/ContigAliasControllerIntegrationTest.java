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

package uk.ac.ebi.eva.contigalias.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;
import uk.ac.ebi.eva.contigalias.test.TestConfiguration;

import java.util.List;
import java.util.Optional;

import static com.ebivariation.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;
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
            List<AssemblyEntity> entityAsList = List.of(this.entity);
            when(mockAssemblyService
                         .getAssemblyByAccession(this.entity.getGenbank()))
                    .thenReturn(entityAsList);
            when(mockAssemblyService.getAssemblyByGenbank(this.entity.getGenbank()))
                    .thenReturn(entityAsList);
            when(mockAssemblyService.getAssemblyByRefseq(this.entity.getRefseq()))
                    .thenReturn(entityAsList);
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
                   .andExpect(jsonPath("$[0]").exists())
                   .andExpect(jsonPath("$[0].id").doesNotExist())
                   .andExpect(jsonPath("$[0].name", is(entity.getName())))
                   .andExpect(jsonPath("$[0].organism", is(entity.getOrganism())))
                   .andExpect(jsonPath("$[0].taxid").value(entity.getTaxid()))
                   .andExpect(jsonPath("$[0].genbank", is(entity.getGenbank())))
                   .andExpect(jsonPath("$[0].refseq", is(entity.getRefseq())))
                   .andExpect(jsonPath("$[0].genbankRefseqIdentical", is(entity.isGenbankRefseqIdentical())));
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
                    .thenReturn(List.of(entity));
            when(mockChromosomeService.getChromosomeByRefseq(entity.getRefseq()))
                    .thenReturn(Optional.of(entity));
        }

        @Test
        void getChromosomeByGenbank() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/chromosomes/genbank/{genbank}", entity.getGenbank()));
            assertChromosomeIdenticalToEntity(resultActions);
        }

        // TODO
//        @Test
//        void getChromosomeByRefseq() throws Exception {
//            ResultActions resultActions = mockMvc.perform(
//                    get("/contig-alias/v1/chromosomes/refseq/{refseq}", entity.getRefseq()));
//            assertChromosomeIdenticalToEntity(resultActions);
//        }

        void assertChromosomeIdenticalToEntity(ResultActions actions) throws Exception {
            actions.andExpect(status().isOk())
                   .andExpect(jsonPath("$[0]").exists())
                   .andExpect(jsonPath("$[0].id").doesNotExist())
                   .andExpect(jsonPath("$[0].name", is(entity.getName())))
                   .andExpect(jsonPath("$[0].genbank", is(entity.getGenbank())))
                   .andExpect(jsonPath("$[0].refseq", is(entity.getRefseq())));
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
