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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.test.TestConfiguration;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_NUMBER;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_SIZE;
import static uk.ac.ebi.eva.contigalias.controller.contigalias.ContigAliasController.AUTHORITY_INSDC;
import static uk.ac.ebi.eva.contigalias.controller.contigalias.ContigAliasController.AUTHORITY_REFSEQ;
import static uk.ac.ebi.eva.contigalias.controller.contigalias.ContigAliasController.NAME_ENA_TYPE;
import static uk.ac.ebi.eva.contigalias.controller.contigalias.ContigAliasController.NAME_GENBANK_TYPE;
import static uk.ac.ebi.eva.contigalias.controller.contigalias.ContigAliasController.NAME_UCSC_TYPE;

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

    private final ChromosomeEntity chromosomeEntity = ChromosomeGenerator.generate();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContigAliasHandler mockHandler;

    @BeforeEach
    void setup() {
        PagedResourcesAssembler<AssemblyEntity> assemblyAssembler = mock(PagedResourcesAssembler.class);
        PagedModel<EntityModel<AssemblyEntity>> assemblyPagedModel = new PagedModel<>(
                Collections.singletonList(new EntityModel<>(assemblyEntity)), null);
        Mockito.when(assemblyAssembler.toModel(any()))
               .thenReturn(assemblyPagedModel);

        PagedResourcesAssembler<SequenceEntity> chromosomeAssembler = mock(PagedResourcesAssembler.class);
        PagedModel<EntityModel<SequenceEntity>> chromosomePagedModel = new PagedModel<>(
                Collections.singletonList(new EntityModel<>(chromosomeEntity)), null);
        Mockito.when(chromosomeAssembler.toModel(any()))
               .thenReturn(chromosomePagedModel);

        when(mockHandler.getAssemblyByAccession(assemblyEntity.getInsdcAccession()))
                .thenReturn(assemblyPagedModel);
        when(mockHandler.getAssemblyByInsdcAccession(assemblyEntity.getInsdcAccession()))
                .thenReturn(assemblyPagedModel);
        when(mockHandler.getAssemblyByRefseq(assemblyEntity.getRefseq()))
                .thenReturn(assemblyPagedModel);

        when(mockHandler.getSequencesByInsdcAccession(chromosomeEntity.getInsdcAccession(), DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);
        when(mockHandler.getSequencesByRefseq(chromosomeEntity.getRefseq(), DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);
        when(mockHandler.getSequencesBySequenceNameAndAssemblyAccession(
                chromosomeEntity.getGenbankSequenceName(), assemblyEntity.getInsdcAccession(), NAME_GENBANK_TYPE, DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);
        when(mockHandler.getSequencesBySequenceNameAndAssemblyTaxid(
                chromosomeEntity.getGenbankSequenceName(), assemblyEntity.getTaxid(), NAME_GENBANK_TYPE, DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);
        when(mockHandler.getSequencesBySequenceNameAndAssemblyAccession(
                chromosomeEntity.getUcscName(), assemblyEntity.getInsdcAccession(), NAME_UCSC_TYPE, DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);
        when(mockHandler.getSequencesBySequenceNameAndAssemblyTaxid(
                chromosomeEntity.getUcscName(), assemblyEntity.getTaxid(), NAME_UCSC_TYPE, DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);
        when(mockHandler.getSequencesBySequenceNameAndAssemblyAccession(
                chromosomeEntity.getEnaSequenceName(), assemblyEntity.getInsdcAccession(), NAME_ENA_TYPE, DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);
        when(mockHandler.getSequencesBySequenceNameAndAssemblyTaxid(
                chromosomeEntity.getEnaSequenceName(), assemblyEntity.getTaxid(), NAME_ENA_TYPE, DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);

        when(mockHandler.getAssembliesBySequenceInsdcAccession(assemblyEntity.getInsdcAccession()))
                .thenReturn(assemblyPagedModel);
        when(mockHandler.getAssembliesBySequenceRefseq(assemblyEntity.getRefseq()))
                .thenReturn(assemblyPagedModel);

        when(mockHandler.getSequencesByAssemblyInsdcAccession(assemblyEntity.getInsdcAccession(), DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);
        when(mockHandler.getSequencesByAssemblyRefseq(assemblyEntity.getRefseq(), DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);
        when(mockHandler.getSequencesByAssemblyAccession(assemblyEntity.getInsdcAccession(), DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);
        when(mockHandler.getSequencesByAssemblyAccession(assemblyEntity.getRefseq(), DEFAULT_PAGE_REQUEST))
                .thenReturn(chromosomePagedModel);

        when(mockHandler.getAssemblyByAccession(assemblyEntity.getInsdcAccession()))
                .thenReturn(assemblyPagedModel);
        when(mockHandler.getAssemblyByInsdcAccession(assemblyEntity.getInsdcAccession()))
                .thenReturn(assemblyPagedModel);
        when(mockHandler.getAssemblyByRefseq(assemblyEntity.getRefseq()))
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

    @Test
    void getAssemblyByAccession() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/assemblies/{accession}", assemblyEntity.getInsdcAccession(), DEFAULT_PAGE_NUMBER,
                    DEFAULT_PAGE_SIZE));
        assertAssemblyPagedModelResponseValid(resultActions);
    }

    @Test
    void getAssemblyByGenbank() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/assemblies/insdc/{insdc}", assemblyEntity.getInsdcAccession()));
        assertAssemblyPagedModelResponseValid(resultActions);
    }

    @Test
    void getAssemblyByRefseq() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/assemblies/refseq/{refseq}", assemblyEntity.getRefseq()));
        assertAssemblyPagedModelResponseValid(resultActions);
    }

    @Test
    void getChromosomeByInsdcAccession() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/chromosomes/genbank/{genbank}", chromosomeEntity.getInsdcAccession()));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getChromosomeByRefseq() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/chromosomes/refseq/{refseq}", chromosomeEntity.getRefseq()));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getSequencesByChromosomeNameAndAssemblyTaxid() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/chromosomes/name/{name}",
                    chromosomeEntity.getGenbankSequenceName()).param("taxid", assemblyEntity.getTaxid().toString()));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getSequencesByChromosomeNameAndAssemblyAccession() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/chromosomes/name/{name}",
                    chromosomeEntity.getGenbankSequenceName()).param("accession", assemblyEntity.getInsdcAccession()));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getSequencesByChromosomeUcscNameAndAssemblyTaxid() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/chromosomes/name/{name}",
                    chromosomeEntity.getUcscName())
                        .param("taxid", assemblyEntity.getTaxid().toString())
                        .param("name", NAME_UCSC_TYPE));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getSequencesByChromosomeUcscNameAndAssemblyAccession() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/chromosomes/name/{name}",
                    chromosomeEntity.getUcscName())
                        .param("accession", assemblyEntity.getInsdcAccession())
                        .param("name", NAME_UCSC_TYPE));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getSequencesByChromosomeEnaNameAndAssemblyTaxid() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/chromosomes/name/{name}",
                    chromosomeEntity.getEnaSequenceName())
                        .param("taxid", assemblyEntity.getTaxid().toString())
                        .param("name", NAME_ENA_TYPE));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getSequencesByChromosomeEnaNameAndAssemblyAccession() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/chromosomes/name/{name}",
                    chromosomeEntity.getEnaSequenceName())
                        .param("accession", assemblyEntity.getInsdcAccession())
                        .param("name", NAME_ENA_TYPE));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getAssemblyByChromosomeGenbank() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/chromosomes/genbank/{genbank}/assemblies", assemblyEntity.getInsdcAccession()));
        assertAssemblyPagedModelResponseValid(resultActions);
    }

    @Test
    void getAssemblyByChromosomeRefseq() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/chromosomes/refseq/{refseq}/assemblies", assemblyEntity.getRefseq()));
        assertAssemblyPagedModelResponseValid(resultActions);
    }

    @Test
    void getSequencesByAssemblyAccessionNoAuthority() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/assemblies/{accession}/chromosomes", assemblyEntity.getInsdcAccession()));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getSequencesByAssemblyInsdcAccession() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/assemblies/genbank/{genbank}/chromosomes", assemblyEntity.getInsdcAccession()));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getSequencesByAssemblyAccessionGenbank() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/assemblies/{accession}/chromosomes", assemblyEntity.getInsdcAccession())
                        .param("authority", AUTHORITY_INSDC));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getSequencesByAssemblyRefseq() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/assemblies/refseq/{refseq}/chromosomes", assemblyEntity.getRefseq()));
        assertChromosomePagedModelResponseValid(resultActions);
    }

    @Test
    void getSequencesByAssemblyAccessionRefseq() throws Exception {
        ResultActions resultActions = mockMvc.perform(
                get("/v1/assemblies/{accession}/chromosomes", assemblyEntity.getRefseq())
                        .param("authority", AUTHORITY_REFSEQ));
        assertChromosomePagedModelResponseValid(resultActions);
    }
}
