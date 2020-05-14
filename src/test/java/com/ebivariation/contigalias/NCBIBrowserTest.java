package com.ebivariation.contigalias;

import com.ebivariation.contigalias.dus.NCBIBrowser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NCBIBrowserTest {

    @Test
    void connect() throws IOException {
        NCBIBrowser ncbiBrowser = new NCBIBrowser();
        ncbiBrowser.connect();
    }

    @Test
    void navigateToAllGenomesDirectory() throws IOException {
        NCBIBrowser ncbiBrowser = new NCBIBrowser();
        ncbiBrowser.connect();
        ncbiBrowser.navigateToAllGenomesDirectory();
        assertTrue(ncbiBrowser.listFiles().length > 0);
    }

}
