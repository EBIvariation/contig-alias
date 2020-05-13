package com.ebivariation.contigalias.dus;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

public class FTPBrowser {

    private FTPClient ftp = new FTPClient();

    public void connect() throws IOException {

        int reply;
        String server = "ftp.ncbi.nlm.nih.gov";

        ftp.connect(server, 21);

        System.out.println("Connected to " + server + ".");

        // Login as anonymous user
        boolean login = ftp.login("anonymous", "jmmut@ebi.ac.uk");
        System.out.println("Login " + (login ? "successful" : "unsuccessful"));

        System.out.println("Directory count:\t" + ftp.listDirectories().length);

        String status = ftp.getStatus();
        System.out.println(status);

        // After connection attempt, you should check the reply code to verify
        // success.
        reply = ftp.getReplyCode();

        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            System.err.println("FTP server refused connection.");
        }
    }

    public void disconnect() throws IOException {
        if (ftp != null) {
            if (ftp.isConnected()) {
                ftp.logout();
                ftp.disconnect();
                ftp = null;
            }
        }
    }

    public FTPFile[] listDir() throws IOException {

        FTPFile[] ftpFiles = null;
        ftpFiles = ftp.listFiles();

//        if (ftpFiles != null) {
//            System.out.println(ftpFiles.length);
//
//            for (FTPFile ftpFile : ftpFiles) {
//                System.out.println(ftpFile.getRawListing());
//            }
//        }

        return ftpFiles;

    }

}
