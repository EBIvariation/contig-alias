/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.contigalias.dus;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import uk.ac.ebi.eva.contigalias.exception.DownloadFailedException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PassiveAnonymousFTPClient extends FTPClient {

    public static final int DEFAULT_FTP_PORT = 21;

    private final Logger logger = LoggerFactory.getLogger(PassiveAnonymousFTPClient.class);

    public void connect(String address) throws IOException {
        this.connect(address, DEFAULT_FTP_PORT);
    }

    @Override
    public void connect(String address, int port) throws IOException {

        try {
            logger.debug("Attempting to connect to {}:{}.", address, port);
            super.connect(address, port);
            // After connection attempt, you should check the reply code to verify
            // success.
            int reply = super.getReplyCode();
            logger.debug("Connected to {} with reply code {}.", address, reply);
            if (!FTPReply.isPositiveCompletion(reply)) {
                throw new RuntimeException("FTP refused connection");
            }

            // Login as anonymous user
            super.enterLocalPassiveMode();
            boolean login = super.login("anonymous", "anonymous");
            logger.debug("Login {}.", (login ? "successful" : "unsuccessful"));
            if (!login) {
                throw new RuntimeException("FTP refused login");
            }

            String status = super.getStatus();
            logger.debug("FTP connection status: {}", status);
            logger.info("Connected successfully to {}", address);
        } catch (Exception e) {
            logger.error("Could not connect to FTP server '{}'. {}.", address, getStatusString());
            try {
                this.disconnect();
            } catch (IOException ex) {
                logger.warn("Error while trying to disconnect : " + ex);
            }
            throw e;
        }
    }

    private String getStatusString() {
        try {
            return String.format("FTP status was: %s. Reply code: %s. Reply string: %s", super.getStatus(),
                                 super.getReply(), super.getReplyString());
        } catch (Exception e) {
            return "Unable to get status information";
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (super.isConnected()) {
            super.logout();
            super.disconnect();
        }
    }

    @Retryable(value = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 100, maxDelay = 500))
    public boolean downloadFTPFile(String ftpFilePath, Path downloadFilePath, long ftpFileSize) throws IOException {
        super.setFileType(FTP.BINARY_FILE_TYPE);
        super.setFileTransferMode(FTP.BINARY_FILE_TYPE);
        Files.deleteIfExists(downloadFilePath);

        boolean success = super.retrieveFile(ftpFilePath, new FileOutputStream(downloadFilePath.toFile()));

        if (success && Files.exists(downloadFilePath) && Files.isReadable(downloadFilePath)) {
            long downloadFileSize = Files.size(downloadFilePath);
            if (ftpFileSize == downloadFileSize) {
                logger.info(ftpFilePath + " downloaded successfully.");
                return true;
            } else {
                throw new DownloadFailedException("FTP file and Downloaded file sizes does not match for " + ftpFilePath + ". Trying again.");
            }
        } else {
            throw new DownloadFailedException("Could not download ftp file" + ftpFilePath + "successfully. Trying again");
        }
    }

}
