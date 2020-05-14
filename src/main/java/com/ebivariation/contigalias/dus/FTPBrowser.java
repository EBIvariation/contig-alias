package com.ebivariation.contigalias.dus;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class FTPBrowser {

    private static final int FTP_PORT = 21;

    private static final String NCBI_FTP_SERVER = "ftp.ncbi.nlm.nih.gov";

    private final Logger logger = LoggerFactory.getLogger(FTPBrowser.class);

    private FTPClient ftp = new FTPClient();

    public void connect() throws IOException {
        try {
            ftp.connect(NCBI_FTP_SERVER, FTP_PORT);
            // After connection attempt, you should check the reply code to verify
            // success.
            int reply = ftp.getReplyCode();
            logger.debug("Connected to {} with reply code {}.", NCBI_FTP_SERVER, reply);
            if(!FTPReply.isPositiveCompletion(reply)) {
                throw new RuntimeException("FTP refused connection");
            }

            // Login as anonymous user
            ftp.enterLocalPassiveMode();
            boolean login = ftp.login("anonymous", "anonymous");
            logger.debug("Login {}.", (login ? "successful" : "unsuccessful"));
            if (!login) {
                throw new RuntimeException("FTP refused login");
            }

            String status = ftp.getStatus();
            logger.debug("FTP connection status: {}", status);
            logger.info("Connected successfully to {}", NCBI_FTP_SERVER);
        } catch (Exception e) {
            logger.error("Could not connect to FTP server '{}'. FTP status was: {}. Reply code: {}. Reply string: {}",
                         NCBI_FTP_SERVER, ftp.getStatus(), ftp.getReply(), ftp.getReplyString());
            ftp.disconnect();
            throw e;
        }
    }

    public void disconnect() throws IOException {
        if (ftp.isConnected()) {
            ftp.logout();
            ftp.disconnect();
        }
    }

    public FTPFile[] listFiles() throws IOException {
        FTPFile[] ftpFiles = ftp.listFiles();
        return ftpFiles;
    }

    public void changeToDirectory(String directory) throws IOException {
        if (!ftp.changeWorkingDirectory(directory)) {
            throw new RuntimeException("Unable to change to directory '" + directory + "'");
        }
    }
}
