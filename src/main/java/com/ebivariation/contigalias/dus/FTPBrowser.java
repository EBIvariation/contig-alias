package com.ebivariation.contigalias.dus;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

public class FTPBrowser {

    private FTPClient ftp = null;

    public void connect() {

        ftp = new FTPClient();
        try {
            int reply;
            String server = "ftp.ncbi.nlm.nih.gov";

            ftp.connect(server, 21);
            System.out.println("Connected to " + server + ".");
            System.out.print(ftp.getReplyString());

            // Login as anonymous user
            boolean login = ftp.login("anonymous", "fakeemail@example.com");
            System.out.println("Login " + (login ? "successful" : "unsuccessful"));

            String status = ftp.getStatus();
            System.out.println(status);

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void disconnect() {
        if (ftp != null) {
            if (ftp.isConnected()) {
                try {
                    ftp.logout();
                    ftp.disconnect();
                    ftp = null;
                } catch (IOException ioe) {
                    // do nothing
                }
            }
        }
    }

    public void listDir() {
        if (ftp == null) {
            return;
        }

        FTPFile[] ftpFiles = null;
        try {
            ftpFiles = ftp.listFiles();
        } catch (IOException e) {
            // Do something --_(-_-)_--
        }

        if (ftpFiles != null) {
            System.out.println(ftpFiles.length);

            for (FTPFile ftpFile : ftpFiles) {
                System.out.println(ftpFile.getRawListing());
            }
        }

    }

}
