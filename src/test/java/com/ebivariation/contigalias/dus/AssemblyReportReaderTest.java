package com.ebivariation.contigalias.dus;

import com.ebivariation.contigalias.entities.AssemblyEntity;
import com.ebivariation.contigalias.entities.ChromosomeEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssemblyReportReaderTest {

    private static final String ASSEMBLY_NAME = "Bos_taurus_UMD_3.1";

    private static final String ASSEMBLY_ORGANISM_NAME = "Bos taurus (cattle)";

    private static final long ASSEMBLY_TAX_ID = 9913;

    private static final String ASSEMBLY_GENBANK_ACCESSION = "GCA_000003055.3";

    private static final String ASSEMBLY_REFSEQ_ACCESSION = "GCF_000003055.4";

    private static final String CHROMOSOME_CHR1_SEQUENCE_NAME = "Chr1";

    private static final String CHROMOSOME_CHR1_GENBANK_ACCESSION = "GK000001.2";

    private static final String CHROMOSOME_CHR1_REFSEQ_ACCESSION = "AC_000158.1";

    private InputStreamReader streamReader;

    private InputStream stream;

    private AssemblyReportReader reader;

    @BeforeEach
    void setup() throws FileNotFoundException {
        stream = new FileInputStream(
                new File("src/test/resources/GCF_000003055.4_Bos_taurus_UMD_3.1_assembly_report.txt"));
        streamReader = new InputStreamReader(stream);
        reader = new AssemblyReportReader(streamReader);
    }

    @AfterEach
    void tearDown() throws IOException {
        stream.close();
        streamReader.close();
    }

    @Test
    void getAssemblyReportReader() {
        assertTrue(reader.getLineCount() > 0);
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
        assertEquals(ASSEMBLY_GENBANK_ACCESSION, assembly.getGenbank());
        assertEquals(ASSEMBLY_REFSEQ_ACCESSION, assembly.getRefseq());
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
        assertEquals(CHROMOSOME_CHR1_SEQUENCE_NAME, chromosome.getName());
        assertEquals(CHROMOSOME_CHR1_GENBANK_ACCESSION, chromosome.getGenbank());
        assertEquals(CHROMOSOME_CHR1_REFSEQ_ACCESSION, chromosome.getRefseq());
    }

}