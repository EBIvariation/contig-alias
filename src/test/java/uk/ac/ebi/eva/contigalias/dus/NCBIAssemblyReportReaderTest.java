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

package uk.ac.ebi.eva.contigalias.dus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entities.SequenceEntity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class NCBIAssemblyReportReaderTest {

    private static final String ASSEMBLY_NAME = "Bos_taurus_UMD_3.1";

    private static final String ASSEMBLY_ORGANISM_NAME = "Bos taurus (cattle)";

    private static final long ASSEMBLY_TAX_ID = 9913;

    private static final String ASSEMBLY_GENBANK_ACCESSION = "GCA_000003055.3";

    private static final String ASSEMBLY_REFSEQ_ACCESSION = "GCF_000003055.3";

    private static final boolean ASSEMBLY_IS_GENBANK_REFSEQ_IDENTICAL = true;

    private static final String CHROMOSOME_CHR1_SEQUENCE_NAME = "Chr1";

    private static final String CHROMOSOME_CHR1_GENBANK_ACCESSION = "GK000001.2";

    private static final String CHROMOSOME_CHR1_REFSEQ_ACCESSION = "AC_000158.1";

    private ChromosomeEntity scaffoldEntity;

    private InputStreamReader streamReader;

    private InputStream stream;

    @Autowired
    private NCBIAssemblyReportReaderFactory readerFactory;

    private NCBIAssemblyReportReader reader;

    @BeforeEach
    void setup() throws FileNotFoundException {
        stream = new FileInputStream(
                new File("src/test/resources/GCA_000003055.3_Bos_taurus_UMD_3.1_assembly_report.txt"));
        streamReader = new InputStreamReader(stream);
        reader = readerFactory.build(streamReader);
        scaffoldEntity = (ChromosomeEntity) new ChromosomeEntity()
                .setGenbankSequenceName("ChrU_1")
                .setInsdcAccession("GJ057137.1")
                .setRefseq("NW_003097882.1")
                .setUcscName(null);
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
    void verifyAssemblyMetadata() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        assertEquals(ASSEMBLY_NAME, assembly.getName());
        assertEquals(ASSEMBLY_ORGANISM_NAME, assembly.getOrganism());
        assertEquals(ASSEMBLY_TAX_ID, assembly.getTaxid());
        assertEquals(ASSEMBLY_GENBANK_ACCESSION, assembly.getInsdcAccession());
        assertEquals(ASSEMBLY_REFSEQ_ACCESSION, assembly.getRefseq());
        assertEquals(ASSEMBLY_IS_GENBANK_REFSEQ_IDENTICAL, assembly.isGenbankRefseqIdentical());
    }

    @Test
    void verifyAssemblyHasChromosomes() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        List<ChromosomeEntity> chromosomes = assembly.getChromosomes();
        assertNotNull(chromosomes);
        assertEquals(3316, chromosomes.size());
    }

    @Test
    void verifyChromosomeMetadata() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        List<ChromosomeEntity> chromosomes = assembly.getChromosomes();
        ChromosomeEntity chromosome = chromosomes.get(0);
        assertEquals(CHROMOSOME_CHR1_SEQUENCE_NAME, chromosome.getGenbankSequenceName());
        assertEquals(CHROMOSOME_CHR1_GENBANK_ACCESSION, chromosome.getInsdcAccession());
        assertEquals(CHROMOSOME_CHR1_REFSEQ_ACCESSION, chromosome.getRefseq());
        assertNull(chromosome.getUcscName());
    }

    @Test
    void verifyAssemblyHasScaffolds() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        List<ChromosomeEntity> scaffolds = assembly.getChromosomes().stream()
                .filter(e -> e.getContigType().equals(SequenceEntity.ContigType.SCAFFOLD)).collect(Collectors.toList());
        assertNotNull(scaffolds);
        assertEquals(3286, scaffolds.size());
    }

    @Test
    void assertParsedScaffoldValid() throws IOException {
        AssemblyEntity assembly = getAssemblyEntity();
        List<ChromosomeEntity> scaffolds = assembly.getChromosomes().stream()
                .filter(e -> e.getContigType().equals(SequenceEntity.ContigType.SCAFFOLD)).collect(Collectors.toList());
        assertNotNull(scaffolds);
        assertTrue(scaffolds.size() > 0);
        ChromosomeEntity scaffold = scaffolds.get(0);
        assertNotNull(scaffold);
        assertEquals(scaffoldEntity.getGenbankSequenceName(), scaffold.getGenbankSequenceName());
        assertEquals(scaffoldEntity.getInsdcAccession(), scaffold.getInsdcAccession());
        assertEquals(scaffoldEntity.getRefseq(), scaffold.getRefseq());
        assertEquals(scaffoldEntity.getUcscName(), scaffold.getUcscName());
    }

}