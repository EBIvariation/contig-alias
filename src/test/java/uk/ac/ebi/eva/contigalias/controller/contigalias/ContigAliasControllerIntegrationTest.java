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

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.test.TestConfiguration;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_NUMBER;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_SIZE;
import static uk.ac.ebi.eva.contigalias.controller.contigalias.ContigAliasController.AUTHORITY_GENBANK;
import static uk.ac.ebi.eva.contigalias.controller.contigalias.ContigAliasController.AUTHORITY_REFSEQ;

/**
 * See https://spring.io/guides/gs/testing-web/ for an explanation of the particular combination of Spring
 * annotations that were used in this test class.
 * <p>
 * See https://github.com/json-path/JsonPath for the jsonPath syntax.
 */
@WebMvcTest(ContigAliasController.class)
@Import(TestConfiguration.class)
public class ContigAliasControllerIntegrationTest {

    private final AssemblyEntity assemblyEntity = AssemblyGenerator.generate();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContigAliasHandler mockHandler;

    private PagedResourcesAssembler<AssemblyEntity> assemblyAssembler;

    private PagedModel<EntityModel<AssemblyEntity>> assemblyPagedModel;

    @BeforeEach
    void setup() {
        assemblyAssembler = mock(PagedResourcesAssembler.class);
        assemblyPagedModel = new PagedModel<>(Collections.singletonList(new EntityModel<>(assemblyEntity)), null);
        Mockito.when(assemblyAssembler.toModel(any()))
               .thenReturn(assemblyPagedModel);
    }

    void assertAssemblyPagedModelResponseValid(ResultActions actions) throws Exception {
        String path = "$._embedded.assemblyEntities[0]";
        assertPagedModelResponseValid(actions, path);
    }

    void assertChromosomePagedModelResponseValid(ResultActions actions) throws Exception {
        String path = "$._embedded.chromosomeEntities[0]";
        assertPagedModelResponseValid(actions, path);
    }

    void assertPagedModelResponseValid(ResultActions actions, String path) throws Exception {
        actions.andExpect(status().isOk())
               .andExpect(jsonPath(path).exists())
               .andExpect(jsonPath(path + ".id").doesNotExist());
    }

    @Nested
    class AssemblyServiceTests {

        @BeforeEach
        void setup() {
            PageImpl<AssemblyEntity> page = new PageImpl<>(Collections.singletonList(assemblyEntity));

            PagedModel<EntityModel<AssemblyEntity>> assembledModel = assemblyAssembler.toModel(page);

            when(mockHandler.getAssemblyByAccession(assemblyEntity.getGenbank()))
                    .thenReturn(assembledModel);
            when(mockHandler.getAssemblyByGenbank(assemblyEntity.getGenbank()))
                    .thenReturn(assembledModel);
            when(mockHandler.getAssemblyByRefseq(assemblyEntity.getRefseq()))
                    .thenReturn(assembledModel);

        }

        @Test
        void getAssemblyByAccession() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/{accession}", assemblyEntity.getGenbank(), DEFAULT_PAGE_NUMBER,
                        DEFAULT_PAGE_SIZE));
            assertAssemblyPagedModelResponseValid(resultActions);
        }

        @Test
        void getAssemblyByGenbank() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/genbank/{genbank}", assemblyEntity.getGenbank()));
            assertAssemblyPagedModelResponseValid(resultActions);
        }

        @Test
        void getAssemblyByRefseq() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/refseq/{refseq}", assemblyEntity.getRefseq()));
            assertAssemblyPagedModelResponseValid(resultActions);
        }

    }

    @Nested
    class ChromosomeServiceTests {

        private ChromosomeEntity chromosomeEntity;

        private List<ChromosomeEntity> chxList;

        @BeforeEach
        void setUp() {
            chromosomeEntity = ChromosomeGenerator.generate(assemblyEntity);
            chxList = assemblyEntity.getChromosomes();
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

        @AfterEach
        void tearDown() {
            assemblyEntity.setChromosomes(chxList);
        }

        @Test
        void getChromosomeByGenbank() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/chromosomes/genbank/{genbank}", chromosomeEntity.getGenbank()));
            assertChromosomePagedModelResponseValid(resultActions);
        }

        @Test
        void getChromosomeByRefseq() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/chromosomes/refseq/{refseq}", chromosomeEntity.getRefseq()));
            assertChromosomePagedModelResponseValid(resultActions);
        }

        @Test
        void getChromosomesByChromosomeNameAndAssemblyTaxid() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/chromosomes/{name}",
                        chromosomeEntity.getName()).param("taxid", assemblyEntity.getTaxid().toString()));
            ContigAliasControllerIntegrationTest.this.assertChromosomePagedModelResponseValid(resultActions);
        }

        @Test
        void getChromosomesByChromosomeNameAndAssemblyAccession() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/chromosomes/{name}",
                        chromosomeEntity.getName()).param("accession", assemblyEntity.getGenbank()));
            ContigAliasControllerIntegrationTest.this.assertChromosomePagedModelResponseValid(resultActions);
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
                when(mockHandler.getAssemblyByChromosomeGenbank(generate.getGenbank()))
                        .thenReturn(assemblyPagedModel);
                when(mockHandler.getAssemblyByChromosomeRefseq(generate.getRefseq()))
                        .thenReturn(assemblyPagedModel);
            }
            assemblyEntity.setChromosomes(null);

            PagedResourcesAssembler<ChromosomeEntity> chromosomeAssembler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<ChromosomeEntity>> chromosomePagedModel = PagedModel.wrap(chromosomeEntities, null);
            Mockito.when(chromosomeAssembler.toModel(any()))
                   .thenReturn(chromosomePagedModel);

            when(mockHandler.getChromosomesByAssemblyGenbank(assemblyEntity.getGenbank(), DEFAULT_PAGE_REQUEST))
                    .thenReturn(chromosomePagedModel);
            when(mockHandler.getChromosomesByAssemblyRefseq(assemblyEntity.getRefseq(), DEFAULT_PAGE_REQUEST))
                    .thenReturn(chromosomePagedModel);
            when(mockHandler.getChromosomesByAssemblyAccession(assemblyEntity.getGenbank()))
                    .thenReturn(chromosomePagedModel);
            when(mockHandler.getChromosomesByAssemblyAccession(assemblyEntity.getRefseq()))
                    .thenReturn(chromosomePagedModel);
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
                assertAssemblyPagedModelResponseValid(resultActions);
            }
        }

        @Test
        void getAssemblyByChromosomeRefseq() throws Exception {
            for (ChromosomeEntity e : chromosomeEntities) {
                ResultActions resultActions = mockMvc.perform(
                        get("/contig-alias/v1/assemblies/chromosome/refseq/{refseq}", e.getRefseq()));
                assertAssemblyPagedModelResponseValid(resultActions);
            }
        }

        @Test
        void getChromosomesByAssemblyAccessionNoAuthority() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/{accession}/chromosomes", assemblyEntity.getGenbank()));
            assertChromosomesListPagedModelResponseValid(resultActions);
        }

        @Test
        void getChromosomesByAssemblyGenbank() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/genbank/{genbank}/chromosomes", assemblyEntity.getGenbank()));
            assertChromosomesListPagedModelResponseValid(resultActions);
        }

        @Test
        void getChromosomesByAssemblyAccessionGenbank() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/{accession}/chromosomes", assemblyEntity.getGenbank())
                            .param("authority", AUTHORITY_GENBANK));
            assertChromosomesListPagedModelResponseValid(resultActions);
        }

        @Test
        void getChromosomesByAssemblyRefseq() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/refseq/{refseq}/chromosomes", assemblyEntity.getRefseq()));
            assertChromosomesListPagedModelResponseValid(resultActions);
        }

        @Test
        void getChromosomesByAssemblyAccessionRefseq() throws Exception {
            ResultActions resultActions = mockMvc.perform(
                    get("/contig-alias/v1/assemblies/{accession}/chromosomes", assemblyEntity.getRefseq())
                            .param("authority", AUTHORITY_REFSEQ));
            assertChromosomesListPagedModelResponseValid(resultActions);
        }

        void assertChromosomesListPagedModelResponseValid(ResultActions actions) throws Exception {
            for (int i = 0; i < CHROMOSOME_LIST_SIZE; i++) {
                String path = "$._embedded.chromosomeEntities[" + i + "]";
                assertPagedModelResponseValid(actions, path);
            }

        }

    }
}
