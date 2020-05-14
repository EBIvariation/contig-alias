package com.ebivariation.contigalias.dus;

import java.io.IOException;

public class NCBIBrowser extends FTPBrowser {

    private static final int FTP_PORT = 21;

    private static final String NCBI_FTP_SERVER = "ftp.ncbi.nlm.nih.gov";

    public void connect() throws IOException {
        super.connect(NCBI_FTP_SERVER, FTP_PORT);
    }
}
