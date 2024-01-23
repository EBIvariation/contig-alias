/*
 * Copyright 2021 EMBL - European Bioinformatics Institute
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

package uk.ac.ebi.eva.contigalias.datasource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class ENAAssemblyDataSourceTest {

    private static final String GCA_ACCESSION_HAVING_CHROMOSOMES = "GCA_000003055.5";

    @Autowired
    private ENAAssemblyDataSource enaDataSource;

    @Autowired
    private NCBIAssemblyDataSource ncbiDataSource;

    @Test
    public void getAssemblyByAccessionGCAHavingChromosomes() throws IOException {
        Optional<AssemblyEntity> accession = enaDataSource.getAssemblyByAccession(GCA_ACCESSION_HAVING_CHROMOSOMES);
        assertTrue(accession.isPresent());
        List<ChromosomeEntity> chromosomes = accession.get().getChromosomes();
        assertNotNull(chromosomes);
        assertFalse(chromosomes.isEmpty());
    }

    @Test
    public void getENASequenceNamesForAssembly() throws IOException {
        Optional<AssemblyEntity> assembly = ncbiDataSource.getAssemblyByAccession(GCA_ACCESSION_HAVING_CHROMOSOMES);
        enaDataSource.addENASequenceNamesToAssembly(assembly.get());
        assertTrue(assembly.isPresent());
        assertTrue(enaDataSource.hasAllEnaSequenceNames(assembly.get()));
    }

}
