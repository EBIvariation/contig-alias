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

package uk.ac.ebi.eva.contigalias.controller.contigalias;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.test.TestConfiguration;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_NUMBER;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_SIZE;

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
    private ContigAliasHandler mockHandler;

    @Nested
    class AssemblyServiceTests {

        private final AssemblyEntity entity = AssemblyGenerator.generate();

        @BeforeEach
        void setup() {
            PageImpl<AssemblyEntity> page = new PageImpl<>(Collections.singletonList(this.entity));

            PagedResourcesAssembler<AssemblyEntity> assembler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = new PagedModel<>(
                    Collections.singletonList(new EntityModel<>(entity)), null);
            Mockito.when(assembler.toModel(any()))
                   .thenReturn(pagedModel);

            PagedModel<EntityModel<AssemblyEntity>> assembledModel = assembler.toModel(page);

            when(mockHandler.getAssemblyByAccession(this.entity.getGenbank()))
                    .thenReturn(assembledModel);
            when(mockHandler.getAssemblyByGenbank(this.entity.getGenbank()))
                    .thenReturn(assembledModel);
            when(mockHandler.getAssemblyByRefseq(this.entity.getRefseq()))
                    .thenReturn(assembledModel);

        }

        @Test
        void getAssemblyByAccession() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/{accession}", this.entity.getGenbank(), DEFAULT_PAGE_NUMBER,
                        DEFAULT_PAGE_SIZE));
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
            String path = "$._embedded.assemblyEntities[0]";
            actions.andDo(MockMvcResultHandlers.print());
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

        private final AssemblyEntity assemblyEntity = AssemblyGenerator.generate();

        private ChromosomeEntity chromosomeEntity;

        @BeforeEach
        void setUp() {
            chromosomeEntity = ChromosomeGenerator.generate(assemblyEntity);
            assemblyEntity.setChromosomes(null);

            PageImpl<ChromosomeEntity> page = new PageImpl<>(Collections.singletonList(this.chromosomeEntity));

            PagedResourcesAssembler<ChromosomeEntity> assembler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<ChromosomeEntity>> pagedModel = new PagedModel<>(
                    Collections.singletonList(new EntityModel<>(chromosomeEntity)), null);
            Mockito.when(assembler.toModel(any()))
                   .thenReturn(pagedModel);

            PagedModel<EntityModel<ChromosomeEntity>> assembledModel = assembler.toModel(page);

            when(mockHandler.getChromosomeByGenbank(this.chromosomeEntity.getGenbank()))
                    .thenReturn(assembledModel);
            when(mockHandler.getChromosomeByRefseq(this.chromosomeEntity.getRefseq()))
                    .thenReturn(assembledModel);
            when(mockHandler.getChromosomesByChromosomeNameAndAssemblyAccession(chromosomeEntity.getName(),
                                                                                assemblyEntity.getGenbank(),
                                                                                DEFAULT_PAGE_REQUEST))
                    .thenReturn(assembledModel);
            when(mockHandler.getChromosomesByChromosomeNameAndAssemblyTaxid(chromosomeEntity.getName(),
                                                                            assemblyEntity.getTaxid(),
                                                                            DEFAULT_PAGE_REQUEST))
                    .thenReturn(assembledModel);
        }

        @Test
        void getChromosomeByGenbank() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/chromosomes/genbank/{genbank}", chromosomeEntity.getGenbank()));
            assertChromosomeIdenticalToEntity(resultActions);
        }

        @Test
        void getChromosomeByRefseq() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/chromosomes/refseq/{refseq}", chromosomeEntity.getRefseq()));
            assertChromosomeIdenticalToEntity(resultActions);
        }

        @Test
        void getChromosomesByChromosomeNameAndAssemblyTaxid() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/chromosomes/{name}",
                        chromosomeEntity.getName()).param("taxid", assemblyEntity.getTaxid().toString()));
            assertBasicResponseValid(resultActions);
        }

        @Test
        void getChromosomesByChromosomeNameAndAssemblyAccession() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/chromosomes/{name}",
                        chromosomeEntity.getName()).param("accession",assemblyEntity.getGenbank()));
            assertBasicResponseValid(resultActions);
        }

        void assertBasicResponseValid(ResultActions actions) throws Exception {
            String path = "$._embedded.chromosomeEntities[0]";
            actions.andExpect(status().isOk())
                   .andExpect(jsonPath(path).exists())
                   .andExpect(jsonPath(path + ".id").doesNotExist());
        }

        void assertChromosomeIdenticalToEntity(ResultActions actions) throws Exception {
            String path = "$._embedded.chromosomeEntities[0]";
            actions.andExpect(status().isOk())
                   .andExpect(jsonPath(path).exists())
                   .andExpect(jsonPath(path + ".id").doesNotExist())
                   .andExpect(jsonPath(path + ".name", is(chromosomeEntity.getName())))
                   .andExpect(jsonPath(path + ".genbank", is(chromosomeEntity.getGenbank())))
                   .andExpect(jsonPath(path + ".refseq", is(chromosomeEntity.getRefseq())));
        }

    }


    @Nested
    class ChromosomeServiceTests2 {

        private final AssemblyEntity assemblyEntity = AssemblyGenerator.generate();

        private final List<ChromosomeEntity> chromosomeEntities = new LinkedList<>();

        private final int CHROMOSOME_LIST_SIZE = 5;

        @BeforeEach
        void setup() {
            for (int i = 0; i < CHROMOSOME_LIST_SIZE; i++) {
                ChromosomeEntity generate = ChromosomeGenerator.generate(i, assemblyEntity);
                chromosomeEntities.add(generate);
                Optional<AssemblyEntity> assemblyEntityAsOptional = Optional.of(this.assemblyEntity);
                when(mockHandler.getAssemblyByChromosomeGenbank(generate.getGenbank()))
                        .thenReturn(assemblyEntityAsOptional);
                when(mockHandler.getAssemblyByChromosomeRefseq(generate.getRefseq()))
                        .thenReturn(assemblyEntityAsOptional);
            }
            assemblyEntity.setChromosomes(null);
            when(mockHandler.getChromosomesByAssemblyGenbank(assemblyEntity.getGenbank()))
                    .thenReturn(chromosomeEntities);
            when(mockHandler.getChromosomesByAssemblyRefseq(assemblyEntity.getRefseq()))
                    .thenReturn(chromosomeEntities);
            when(mockHandler.getChromosomesByAssemblyAccession(assemblyEntity.getGenbank()))
                    .thenReturn(chromosomeEntities);
            when(mockHandler.getChromosomesByAssemblyAccession(assemblyEntity.getRefseq()))
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
        void getChromosomesByAssemblyAccessionNoAuthority() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/{accession}/chromosomes", assemblyEntity.getGenbank()));
            assertChromosomesEqualToEntities(resultActions);
        }

        @Test
        void getChromosomesByAssemblyGenbank() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/genbank/{genbank}/chromosomes", assemblyEntity.getGenbank()));
            assertChromosomesEqualToEntities(resultActions);
        }

        @Test
        void getChromosomesByAssemblyAccessionGenbank() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/{accession}/chromosomes", assemblyEntity.getGenbank())
                            .param("authority", "genbank"));
            assertChromosomesEqualToEntities(resultActions);
        }

        @Test
        void getChromosomesByAssemblyRefseq() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/refseq/{refseq}/chromosomes", assemblyEntity.getRefseq()));
            assertChromosomesEqualToEntities(resultActions);
        }

        @Test
        void getChromosomesByAssemblyAccessionRefseq() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/{accession}/chromosomes", assemblyEntity.getRefseq())
                            .param("authority", "refseq"));
            assertChromosomesEqualToEntities(resultActions);
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
