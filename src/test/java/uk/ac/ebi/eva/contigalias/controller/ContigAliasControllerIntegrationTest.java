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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.service.AliasService;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;
import uk.ac.ebi.eva.contigalias.test.TestConfiguration;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;

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

    @MockBean
    private AliasService mockAliasService;

    @Nested
    class AssemblyServiceTests {

        private final AssemblyEntity entity = AssemblyGenerator.generate();

        @BeforeEach
        void setUp() {
            Page<AssemblyEntity> entityListAsPage = new PageImpl<>(Collections.singletonList(this.entity));
            when(mockAssemblyService
                         .getAssemblyByAccession(this.entity.getGenbank(), DEFAULT_PAGE_REQUEST))
                    .thenReturn(entityListAsPage);
            when(mockAssemblyService.getAssemblyByGenbank(this.entity.getGenbank(), DEFAULT_PAGE_REQUEST))
                    .thenReturn(entityListAsPage);
            when(mockAssemblyService.getAssemblyByRefseq(this.entity.getRefseq(), DEFAULT_PAGE_REQUEST))
                    .thenReturn(entityListAsPage);
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
            String path = "$._embedded.assemblyEntityList[0]";
            actions.andExpect(status().isOk())
                   .andExpect(jsonPath(path).exists())
                   .andExpect(jsonPath(path + ".id").doesNotExist())
                   .andExpect(jsonPath(path + ".name", is(entity.getName())))
                   .andExpect(jsonPath(path + ".organism", is(entity.getOrganism())))
                   .andExpect(jsonPath(path + ".taxid").value(entity.getTaxid()))
                   .andExpect(jsonPath(path + ".genbank", is(entity.getGenbank())))
                   .andExpect(jsonPath(path + ".refseq", is(entity.getRefseq())))
                   .andExpect(jsonPath(path + ".genbankRefseqIdentical", is(entity.isGenbankRefseqIdentical())));
        }

    }

    @Nested
    class ChromosomeServiceTests {

        private final ChromosomeEntity entity = ChromosomeGenerator.generate();

        @BeforeEach
        void setUp() {
            Page<ChromosomeEntity> entityListAsPage = new PageImpl<>(Collections.singletonList(this.entity));
            when(mockChromosomeService.getChromosomeByGenbank(this.entity.getGenbank(), DEFAULT_PAGE_REQUEST))
                    .thenReturn(entityListAsPage);
            when(mockChromosomeService.getChromosomeByRefseq(this.entity.getRefseq(), DEFAULT_PAGE_REQUEST))
                    .thenReturn(entityListAsPage);
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
            String path = "$._embedded.chromosomeEntityList[0]";
            actions.andExpect(status().isOk())
                   .andExpect(jsonPath(path).exists())
                   .andExpect(jsonPath(path + ".id").doesNotExist())
                   .andExpect(jsonPath(path + ".name", is(entity.getName())))
                   .andExpect(jsonPath(path + ".genbank", is(entity.getGenbank())))
                   .andExpect(jsonPath(path + ".refseq", is(entity.getRefseq())));
        }

    }

    @Nested
    class AliasServiceTests {

        private final AssemblyEntity assemblyEntity = AssemblyGenerator.generate();

        private final List<ChromosomeEntity> chromosomeEntities = new LinkedList<>();

        private final int CHROMOSOME_LIST_SIZE = 5;

        @BeforeEach
        void setup() {
            for (int i = 0; i < CHROMOSOME_LIST_SIZE; i++) {
                ChromosomeEntity generate = ChromosomeGenerator.generate(i, assemblyEntity);
                chromosomeEntities.add(generate);
                Optional<AssemblyEntity> assemblyEntityAsOptional = Optional.of(this.assemblyEntity);
                when(mockAliasService.getAssemblyByChromosomeGenbank(generate.getGenbank()))
                        .thenReturn(assemblyEntityAsOptional);
                when(mockAliasService.getAssemblyByChromosomeRefseq(generate.getRefseq()))
                        .thenReturn(assemblyEntityAsOptional);
            }
            assemblyEntity.setChromosomes(null);
            when(mockAliasService.getChromosomesByAssemblyGenbank(assemblyEntity.getGenbank()))
                    .thenReturn(chromosomeEntities);
            when(mockAliasService.getChromosomesByAssemblyRefseq(assemblyEntity.getRefseq()))
                    .thenReturn(chromosomeEntities);
        }

        @AfterEach
        void tearDown() {
            chromosomeEntities.clear();
        }

        @Test
        void getAssemblyByChromosomeGenbank() throws Exception {
            for (ChromosomeEntity e : chromosomeEntities) {
                ResultActions resultActions = mockMvc.perform(
                        get("/contig-alias/v1/assemblies/chromosome/genbank/{genbank}", e.getGenbank()));
                assertAssemblyIdenticalToEntity(resultActions);
            }
        }

        @Test
        void getAssemblyByChromosomeRefseq() throws Exception {
            for (ChromosomeEntity e : chromosomeEntities) {
                ResultActions resultActions = mockMvc.perform(
                        get("/contig-alias/v1/assemblies/chromosome/refseq/{refseq}", e.getRefseq()));
                assertAssemblyIdenticalToEntity(resultActions);
            }
        }

        void assertAssemblyIdenticalToEntity(ResultActions actions) throws Exception {
            actions.andExpect(status().isOk())
                   .andExpect(jsonPath("$.id").doesNotExist())
                   .andExpect(jsonPath("$.name", is(assemblyEntity.getName())))
                   .andExpect(jsonPath("$.organism", is(assemblyEntity.getOrganism())))
                   .andExpect(jsonPath("$.taxid").value(assemblyEntity.getTaxid()))
                   .andExpect(jsonPath("$.genbank", is(assemblyEntity.getGenbank())))
                   .andExpect(jsonPath("$.refseq", is(assemblyEntity.getRefseq())))
                   .andExpect(jsonPath("$.genbankRefseqIdentical", is(assemblyEntity.isGenbankRefseqIdentical())));
        }

        @Test
        void getChromosomesByAssemblyGenbank() throws Exception {
            for (ChromosomeEntity e : chromosomeEntities) {
                ResultActions resultActions = mockMvc.perform(
                        get("/contig-alias/v1/assemblies/genbank/{genbank}/chromosomes", assemblyEntity.getGenbank()));
                assertChromosomesEqualToEntities(resultActions);
            }
        }

        @Test
        void getChromosomesByAssemblyRefseq() throws Exception {
            for (ChromosomeEntity e : chromosomeEntities) {
                ResultActions resultActions = mockMvc.perform(
                        get("/contig-alias/v1/assemblies/refseq/{refseq}/chromosomes", assemblyEntity.getRefseq()));
                assertChromosomesEqualToEntities(resultActions);
            }
        }

        void assertChromosomesEqualToEntities(ResultActions actions) throws Exception {
            actions.andExpect(status().isOk());
            for (int i = 0; i < CHROMOSOME_LIST_SIZE; i++) {
                ChromosomeEntity entity = chromosomeEntities.get(i);
                actions.andExpect(jsonPath("$[" + i + "].id").doesNotExist())
                       .andExpect(jsonPath("$[" + i + "].name", is(entity.getName())))
                       .andExpect(jsonPath("$[" + i + "].genbank", is(entity.getGenbank())))
                       .andExpect(jsonPath("$[" + i + "].refseq", is(entity.getRefseq())));
            }

        }

    }
}
