package com.ebivariation.contigalias;

import com.ebivariation.contigalias.dus.FTPBrowser;
import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FTPBrowserTest {

    @Test
    void connectToServer() throws IOException {
        FTPBrowser ftpBrowser = new FTPBrowser();
        ftpBrowser.connect();
        assertTrue(ftpBrowser.listDir().length > 0);
        ftpBrowser.disconnect();
    }

    @Test
    void jshellTest() throws IOException {
        FTPClient ftp = new FTPClient();
        try {
            String server = "ftp.ncbi.nlm.nih.gov";
            ftp.connect(server, 21);
            boolean login = ftp.login("anonymous", "jmmut@ebi.ac.uk");
            assertTrue(login);
            assertTrue(ftp.listDirectories().length> 0);
        } catch (IOException e) {
            ftp.disconnect();
            throw e;
        }
    }

}
