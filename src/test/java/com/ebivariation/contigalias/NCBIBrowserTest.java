package com.ebivariation.contigalias;

import com.ebivariation.contigalias.dus.NCBIBrowser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NCBIBrowserTest {

    @Test
    void connect() throws IOException {
        NCBIBrowser ncbiBrowser = new NCBIBrowser();
        try{
        ncbiBrowser.connect();
        }finally {
            ncbiBrowser.disconnect();
        }
    }

    @Test
    void navigateToAllGenomesDirectory() throws IOException {
        NCBIBrowser ncbiBrowser = new NCBIBrowser();
        try {
            ncbiBrowser.connect();
            ncbiBrowser.navigateToAllGenomesDirectory();
            assertTrue(ncbiBrowser.listFiles().length > 0);
        } finally {
            ncbiBrowser.disconnect();
        }
    }

    @Test
    void navigateToSubDirectoryPath() throws IOException {
        NCBIBrowser ncbiBrowser = new NCBIBrowser();
        try {
            ncbiBrowser.connect();
            ncbiBrowser.navigateToSubDirectoryPath("/genomes/INFLUENZA/");
            assertTrue(ncbiBrowser.listFiles().length > 0);
        } finally {
            ncbiBrowser.disconnect();
        }
    }

    @Test
    void getGenomeReportDirectory_GCATest() throws IOException {
        NCBIBrowser ncbiBrowser = new NCBIBrowser();
        try {
            ncbiBrowser.connect();
            String path = ncbiBrowser.getGenomeReportDirectory("GCA_004051055.1");
            assertEquals("GCA/004/051/055/GCA_004051055.1_ASM405105v1/", path);
        } finally {
            ncbiBrowser.disconnect();
        }
    }

    @Test
    void getGenomeReportDirectory_GCFTest() throws IOException {
        NCBIBrowser ncbiBrowser = new NCBIBrowser();
        try {
            ncbiBrowser.connect();
            String path = ncbiBrowser.getGenomeReportDirectory("GCF_007608995.1");
            assertEquals("GCF/007/608/995/GCF_007608995.1_ASM760899v1/", path);
        } finally {
            ncbiBrowser.disconnect();
        }
    }

}
