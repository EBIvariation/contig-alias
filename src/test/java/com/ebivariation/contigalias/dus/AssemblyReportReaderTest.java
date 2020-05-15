package com.ebivariation.contigalias.dus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AssemblyReportReaderTest {

    InputStreamReader streamReader;

    private NCBIBrowser ncbiBrowser;

    private InputStream stream;

    @BeforeEach
    void setUp() throws IOException {
        ncbiBrowser = new NCBIBrowser();
        ncbiBrowser.connect();
        stream = ncbiBrowser.getAssemblyReportInputStream(
                "/genomes/all/GCF/007/608/995/GCF_007608995.1_ASM760899v1/");
        streamReader = new InputStreamReader(stream);
    }

    @AfterEach
    void tearDown() throws IOException {
        stream.close();
        streamReader.close();
        ncbiBrowser.disconnect();
    }

    @Test
    void getAssemblyReportReader() throws IOException {
        AssemblyReportReader reader = new AssemblyReportReader(streamReader);
        assertTrue(reader.lines().count() > 0);
    }

}