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

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

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

    private static final Path assemblyReportPath = Paths.get("src/test/resources/GCA_000003055.3_sequence_report.txt");

    List<ChromosomeEntity> getChromosomes() throws IOException {
        List<String> lines = Files.lines(assemblyReportPath).collect(Collectors.toList());
        return ENAAssemblyReportReader.getChromosomeEntity(lines);
    }

    @Test
    void verifyAssemblyHasChromosomes() throws IOException {
        List<ChromosomeEntity> chromosomes = getChromosomes();
        assertNotNull(chromosomes);
        assertEquals(3316, chromosomes.size());
    }

    @Test
    void verifyChromosomeMetadata() throws IOException {
        List<ChromosomeEntity> chromosomes = getChromosomes();
        ChromosomeEntity chromosome = chromosomes.get(0);
        assertEquals(CHROMOSOME_ENA_SEQUENCE_NAME, chromosome.getEnaSequenceName());
        assertEquals(CHROMOSOME_GENBANK_ACCESSION, chromosome.getInsdcAccession());
        assertNull(chromosome.getUcscName());
    }

    @Test
    void verifyAssemblyHasScaffolds() throws IOException {
        List<ChromosomeEntity> scaffolds = getChromosomes().stream()
                .filter(e -> e.getContigType().equals(SequenceEntity.ContigType.SCAFFOLD)).collect(Collectors.toList());
        assertNotNull(scaffolds);
        assertEquals(3286, scaffolds.size());
    }

    @Test
    void assertParsedScaffoldValid() throws IOException {
        List<ChromosomeEntity> scaffolds = getChromosomes().stream()
                .filter(e -> e.getContigType().equals(SequenceEntity.ContigType.SCAFFOLD)).collect(Collectors.toList());
        assertNotNull(scaffolds);
        assertTrue(scaffolds.size() > 0);
        ChromosomeEntity scaffold = scaffolds.get(0);
        assertNotNull(scaffold);
        assertEquals(SCAFFOLD_SEQUENCE_NAME, scaffold.getEnaSequenceName());
        assertEquals(SCAFFOLD_GENBANK_ACCESSION, scaffold.getInsdcAccession());
    }

}