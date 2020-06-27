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

package uk.ac.ebi.eva.contigalias.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.test.TestConfiguration;

import java.io.IOException;
import java.util.List;
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

    private final Optional<Integer> DEFAULT_PAGE_NUMBER = Optional.of(0);

    private final Optional<Integer> DEFAULT_PAGE_SIZE = Optional.of(10);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AssemblyService mockAssemblyService;

    @BeforeEach
    void setUp() throws IOException {
        when(mockAssemblyService
                     .getAssemblyOrFetchByAccession(entity.getGenbank(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE))
                .thenReturn(List.of(entity));
        when(mockAssemblyService
                     .getAssemblyOrFetchByAccession(entity.getRefseq(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE))
                .thenReturn(List.of(entity));
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
    public void test404NotFound() throws Exception {
        this.mockMvc.perform(get("/contig-alias-admin/v1/assemblies/{accession}", "##INVALID##"))
                    .andExpect(status().isNotFound());
    }
}
