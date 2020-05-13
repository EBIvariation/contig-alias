package com.ebivariation.contigalias;

import com.ebivariation.contigalias.dus.FTPBrowser;
import org.junit.jupiter.api.Test;

public class FTPBrowserTest {

    @Test
    void connectToServer(){
        FTPBrowser ftpBrowser = new FTPBrowser();
        ftpBrowser.connect();
        ftpBrowser.listDir();
        ftpBrowser.disconnect();
    }

}
