package com.ebivariation.contigalias;

import com.ebivariation.contigalias.dus.FTPBrowser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class FTPBrowserTest {

    @Test
    void connectToServer() {
        FTPBrowser ftpBrowser = new FTPBrowser();
        try {
            ftpBrowser.connect();
            System.out.println(ftpBrowser.listDir().length);
            ftpBrowser.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void jshellTest(){
        org.apache.commons.net.ftp.FTPClient ftp = new org.apache.commons.net.ftp.FTPClient();
        String server = "ftp.ncbi.nlm.nih.gov";
        try {
            ftp.connect(server, 21);

        boolean login = ftp.login("anonymous", "jmmut@ebi.ac.uk(opens in new tab)");
        System.out.println(login);
        System.out.println(ftp.listDirectories().length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
