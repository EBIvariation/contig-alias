package com.ebivariation.contigalias;

import com.ebivariation.contigalias.dus.NCBIBrowser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NCBIBrowserTest {

    private NCBIBrowser ncbiBrowser;

    @BeforeEach
    void setUp() throws IOException {
        ncbiBrowser = new NCBIBrowser();
        ncbiBrowser.connect();
    }

    @AfterEach
    void tearDown() throws IOException {
        ncbiBrowser.disconnect();
    }

    @Test
    void connect() throws IOException {
        ncbiBrowser.connect();
    }

    @Test
    void navigateToAllGenomesDirectory() throws IOException {
        ncbiBrowser.navigateToAllGenomesDirectory();
        assertTrue(ncbiBrowser.listFiles().length > 0);
    }

    @Test
    void navigateToSubDirectoryPath() throws IOException {
        ncbiBrowser.navigateToSubDirectoryPath("/genomes/INFLUENZA/");
        assertTrue(ncbiBrowser.listFiles().length > 0);
    }

    @Test
    void getGenomeReportDirectoryGCATest() throws IOException {
        String path = ncbiBrowser.getGenomeReportDirectory("GCA_004051055.1");
        assertEquals("GCA/004/051/055/GCA_004051055.1_ASM405105v1/", path);
    }

    @Test
    void getGenomeReportDirectoryGCFTest() throws IOException {
        String path = ncbiBrowser.getGenomeReportDirectory("GCF_007608995.1");
        assertEquals("GCF/007/608/995/GCF_007608995.1_ASM760899v1/", path);
    }

    @Test
    void getAssemblyReportInputStream() throws IOException {
        InputStream stream = ncbiBrowser.getAssemblyReportInputStream("/genomes/all/GCF/007/608/995/GCF_007608995.1_ASM760899v1/");
        assertTrue(stream.read() != -1);
        stream.close();
    }
}
