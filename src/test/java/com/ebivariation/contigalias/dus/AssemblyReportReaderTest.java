package com.ebivariation.contigalias.dus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AssemblyReportReaderTest {

    InputStreamReader streamReader;

    private InputStream stream;

    @BeforeEach
    void setup() throws FileNotFoundException {
        stream = new FileInputStream(
                new File("src/test/resources/GCF_000003055.4_Bos_taurus_UMD_3.1_assembly_report.txt"));
        streamReader = new InputStreamReader(stream);
    }

    @AfterEach
    void tearDown() throws IOException {
        stream.close();
        streamReader.close();
    }

    @Test
    void getAssemblyReportReader() {
        AssemblyReportReader reader = new AssemblyReportReader(streamReader);
        assertTrue(reader.lines().count() > 0);
    }

}