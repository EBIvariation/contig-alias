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

package uk.ac.ebi.eva.contigalias.controller.admin;

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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.test.TestConfiguration;

import java.io.IOException;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
    private AdminHandler mockHandler;

    @BeforeEach
    void setUp() throws IOException {
        PagedResourcesAssembler<AssemblyEntity> mockAssemblyAssembler = mock(PagedResourcesAssembler.class);

        PagedModel<EntityModel<AssemblyEntity>> assemblyPagedModel = new PagedModel(
                Collections.singleton(new EntityModel<>(entity)), null);
        Mockito.when(mockAssemblyAssembler.toModel(any()))
               .thenReturn(assemblyPagedModel);

        when(mockHandler.getAssemblyOrFetchByAccession(entity.getGenbank()))
                .thenReturn(assemblyPagedModel);
        when(mockHandler.getAssemblyOrFetchByAccession(entity.getRefseq()))
                .thenReturn(assemblyPagedModel);
    }

    @Test
    public void getAssemblyOrFetchByAccessionGCA() throws Exception {
        ResultActions request = this.mockMvc.perform(
                get("/v1/admin/assemblies/{accession}", entity.getGenbank()));
        assertAssemblyPagedModelResponseValid(request);
    }

    @Test
    public void getAssemblyOrFetchByAccessionGCF() throws Exception {
        ResultActions request = this.mockMvc.perform(
                get("/v1/admin/assemblies/{accession}", entity.getRefseq()));
        assertAssemblyPagedModelResponseValid(request);
    }

    private void assertAssemblyPagedModelResponseValid(ResultActions request) throws Exception {
        String path = "$._embedded.assemblyEntities[0]";
        request.andExpect(status().isOk())
               .andExpect(jsonPath(path).exists())
               .andExpect(jsonPath(path + ".id").doesNotExist());
    }

}
