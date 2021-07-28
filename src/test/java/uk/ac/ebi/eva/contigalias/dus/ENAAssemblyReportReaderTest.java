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

package uk.ac.ebi.eva.contigalias.dus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entities.ScaffoldEntity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class ENAAssemblyReportReaderTest {

    private static final String CHROMOSOME_ENA_SEQUENCE_NAME = "1";

    private static final String CHROMOSOME_GENBANK_ACCESSION = "GK000001.2";

    private static final String SCAFFOLD_SEQUENCE_NAME = "ChrU_1";

    private static final String SCAFFOLD_GENBANK_ACCESSION = "GJ057137.1";

    private InputStreamReader streamReader;

    private InputStream stream;

    @Autowired
    private ENAAssemblyReportReaderFactory readerFactory;

    private ENAAssemblyReportReader reader;

    @BeforeEach
    void setup() throws FileNotFoundException {
        stream = new FileInputStream("src/test/resources/GCA_000003055.3_sequence_report.txt");
        streamReader = new InputStreamReader(stream);
        reader = readerFactory.build(streamReader);
    }

    @AfterEach
    void tearDown() throws IOException {
        stream.close();
        streamReader.close();
    }

    @Test
    void getAssemblyReportReader() throws IOException {
        assertTrue(reader.ready());
    }

    AssemblyEntity getAssemblyEntity() throws IOException {
        return reader.getAssemblyEntity();
    }

    @Test
    void verifyAssemblyHasChromosomes() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        List<ChromosomeEntity> chromosomes = assembly.getChromosomes();
        assertNotNull(chromosomes);
        assertEquals(30, chromosomes.size());
    }

    @Test
    void verifyChromosomeMetadata() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        List<ChromosomeEntity> chromosomes = assembly.getChromosomes();
        ChromosomeEntity chromosome = chromosomes.get(0);
        assertEquals(CHROMOSOME_ENA_SEQUENCE_NAME, chromosome.getEnaSequenceName());
        assertEquals(CHROMOSOME_GENBANK_ACCESSION, chromosome.getGenbank());
        assertNull(chromosome.getUcscName());
    }

    @Test
    void verifyAssemblyHasScaffolds() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        List<ScaffoldEntity> scaffolds = assembly.getScaffolds();
        assertNotNull(scaffolds);
        assertEquals(3286, scaffolds.size());
    }

    @Test
    void assertParsedScaffoldValid() throws IOException {
        List<ScaffoldEntity> scaffolds = getAssemblyEntity().getScaffolds();
        assertNotNull(scaffolds);
        assertTrue(scaffolds.size() > 0);
        ScaffoldEntity scaffold = scaffolds.get(0);
        assertNotNull(scaffold);
        assertEquals(SCAFFOLD_SEQUENCE_NAME, scaffold.getEnaSequenceName());
        assertEquals(SCAFFOLD_GENBANK_ACCESSION, scaffold.getGenbank());
    }

}